package de.medienDresden.illumina.communication.impl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

import de.medienDresden.illumina.communication.StreamingSocket;

public class StreamingSocketImpl implements StreamingSocket {

    private static final String TAG = StreamingSocketImpl.class.getSimpleName();

    public static final int READ_TIMEOUT        = 30 * 1000;
    public static final int CONNECT_TIMEOUT     = 5  * 1000;
    public static final int HEARTBEAT_TIMEOUT   = 2  * 1000;
    public static final long HEARTBEAT_INTERVAL = 5  * 1000;

    private Socket mSocket;

    private Handler mHandler;

    private ReaderThread mReader;

    private PrintWriter mWriter;

    private boolean mIsConnected = false;

    private long mLastHeartBeatRequest;

    private String mHost;

    private int mPort;

    private final Handler mReadHandler = new Handler() {
        @Override
        public void handleMessage(Message msgFromReader) {
            assert msgFromReader.getData() != null;

            final String message = msgFromReader.getData().getString(ReaderThread.EXTRA_MESSAGE);

            if (TextUtils.equals("BEAT", message)) {
                final long currentTime = System.currentTimeMillis();

                Log.d(TAG, "BEAT");

                if (HEARTBEAT_TIMEOUT < currentTime - mLastHeartBeatRequest) {
                    dispatchError();
                    disconnect();
                }

            } else {
                final Message msg = mHandler.obtainMessage(MSG_MESSAGE_RECEIVED);
                final Bundle bundle = new Bundle();

                bundle.putString(EXTRA_MESSAGE, message);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }
    };

    private final Thread mHeartBeat = new Thread() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    // ignore
                }

                send("HEART");

                Log.d(TAG, "HEART");
                mLastHeartBeatRequest = System.currentTimeMillis();
            }
        }
    };

    private final Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(mHost, mPort), CONNECT_TIMEOUT);
                mSocket.setSoTimeout(READ_TIMEOUT);

                mReader = new ReaderThread(mSocket.getInputStream(), mReadHandler);
                mWriter = new PrintWriter(new OutputStreamWriter(
                        mSocket.getOutputStream(), "UTF-8"));

                mReader.start();
                mHeartBeat.start();

                mIsConnected = true;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_CONNECTED));

            } catch (IOException exception) {
                Log.w(TAG, exception.getMessage());
                mIsConnected = false;
                dispatchError();
            }
        }
    };

    public StreamingSocketImpl(Handler handler) {
        mHandler = handler;
    }

    private void dispatchError() {
        final Message msg = mHandler.obtainMessage(MSG_DISCONNECTED);
        final Bundle bundle = new Bundle();

        bundle.putBoolean(EXTRA_ERROR, true);
        msg.setData(bundle);

        mHandler.sendMessage(msg);
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void connect(String host, int port) {
        mHost = host;
        mPort = port;

        if (mIsConnected) {
            disconnect();
        }

        Executors.defaultThreadFactory().newThread(mConnectRunnable).start();
    }

    @Override
    public void disconnect() {
        mIsConnected = false;
        mHeartBeat.interrupt();

        if (mReader != null) {
            mReader.interrupt();
        }

        if (mWriter != null) {
            mWriter.close();
        }

        mSocket = null;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_DISCONNECTED));
    }

    @Override
    public synchronized void send(final String message) {
        mWriter.write(message);
        mWriter.flush();
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public int getPort() {
        return mPort;
    }

}
