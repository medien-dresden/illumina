package de.medienDresden.illumina.communication.impl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

import de.medienDresden.illumina.communication.StreamingSocket;

public class StreamingSocketImpl implements StreamingSocket {

    public static final int CONNECT_TIMEOUT = 1000;
    public static final int READ_TIMEOUT = 30000;

    private Socket mSocket;

    private Handler mHandler;

    private ReaderThread mReader;

    private PrintWriter mWriter;

    private boolean mIsConnected = false;

    private String mHost;

    private int mPort;

    private final Handler mReadHandler = new Handler() {
        @Override
        public void handleMessage(Message msgFromReader) {
            assert msgFromReader.getData() != null;

            final String message = msgFromReader.getData().getString(ReaderThread.EXTRA_MESSAGE);
            final Message msg = mHandler.obtainMessage(MSG_MESSAGE_RECEIVED);
            final Bundle bundle = new Bundle();

            bundle.putString(EXTRA_MESSAGE, message);
            mHandler.sendMessage(msg);
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
                mIsConnected = true;

                mHandler.sendMessage(mHandler.obtainMessage(MSG_CONNECTED));

            } catch (IOException exception) {
                mIsConnected = false;

                mHandler.sendMessage(mHandler.obtainMessage(MSG_DISCONNECTED));
            }
        }
    };

    public StreamingSocketImpl(Handler handler) {
        mHandler = handler;
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void connect(String host, int port) {
        mHost = host;
        mPort = port;

        Executors.defaultThreadFactory().newThread(mConnectRunnable).start();
    }

    @Override
    public void disconnect() {
        mIsConnected = false;

        if (mReader != null) {
            mReader.interrupt();
        }

        if (mWriter != null) {
            mWriter.close();
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_DISCONNECTED));
    }

    @Override
    public void send(final String message) {
        mWriter.write(message);
        mWriter.flush();
    }

}
