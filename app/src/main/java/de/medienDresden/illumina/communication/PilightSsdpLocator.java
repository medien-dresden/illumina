package de.medienDresden.illumina.communication;

import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PilightSsdpLocator implements SsdpLocator {

    public static final Logger log = LoggerFactory.getLogger(PilightSsdpLocator.class);

    private final static String DISCOVER_MESSAGE =
                    "M-SEARCH * HTTP/1.1\r\n" +
                    "Host: 239.255.255.250:1900\r\n" +
                    "Man: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: urn:schemas-upnp-org:service:pilight:1\r\n";

    private final Consumer consumer;

    private final Handler handler = new Handler();

    private final Pattern pattern = Pattern.compile(".*Location:([0-9\\.]+):([0-9]+).*");

    private Runnable discoveringRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[8192];
                final DatagramSocket socket = sendDiscoveryBroadcast();
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.setSoTimeout(5000);
                socket.receive(packet);

                final String response = convertStreamToString(
                        new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));

                final Matcher matcher = pattern.matcher(response);

                if (matcher.matches()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PilightSsdpLocator.this.consumer.onSsdpServiceFound(
                                    matcher.group(1), Integer.parseInt(matcher.group(2)));
                        }
                    });

                    return;
                }

            } catch (Exception exception) {
                log.warn("service discovery failed", exception);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    PilightSsdpLocator.this.consumer.onNoSsdpServiceFound();
                }
            });
        }
    };

    public PilightSsdpLocator(Consumer consumer) {
        this.consumer = consumer;
    }

    private DatagramSocket sendDiscoveryBroadcast(
            final InetAddress interfaceAddress) throws Exception {
        final int port = 1900;
        final DatagramSocket socket = new DatagramSocket(
                new InetSocketAddress(interfaceAddress.getHostAddress(), 0));

        final InetAddress address = InetAddress.getByName("239.255.255.250");

        socket.setReuseAddress(true);
        socket.setSoTimeout(130000);

        final byte[] requestMessage = DISCOVER_MESSAGE.getBytes("UTF-8");
        final DatagramPacket datagramPacket = new DatagramPacket(
                requestMessage, requestMessage.length, address, port);

        socket.send(datagramPacket);
        return socket;
    }

    private DatagramSocket sendDiscoveryBroadcast() throws Exception {
        final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface networkInterface : Collections.list(nets)) {
            final Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            for (InetAddress interfaceAddress : Collections.list(addresses)) {
                if (!interfaceAddress.isLoopbackAddress()
                        && interfaceAddress instanceof Inet4Address) {
                    return sendDiscoveryBroadcast(interfaceAddress);
                }
            }
        }

        throw new Exception("no service found");
    }

    private String convertStreamToString(InputStream stream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        final StringBuilder builder = new StringBuilder();
        
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            stream.close();

        } catch (IOException exception) {
            log.warn("converting stream to string failed", exception);
        }

        return builder.toString();
    }

    @Override
    public void discover() {
        Executors.defaultThreadFactory()
                .newThread(discoveringRunnable)
                .start();
    }

}