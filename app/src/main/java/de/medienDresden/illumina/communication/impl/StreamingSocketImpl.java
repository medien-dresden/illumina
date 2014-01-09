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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import de.medienDresden.illumina.communication.StreamingSocket;

public class StreamingSocketImpl implements StreamingSocket {

    private static final String TAG = StreamingSocketImpl.class.getSimpleName();

    @SuppressWarnings("PointlessArithmeticExpression")
    public static final int HEARTBEAT_INTERVAL = 1  * 1000;
    public static final int HEARTBEAT_TIMEOUT  = 2  * 1000;
    public static final int READ_TIMEOUT       = 30 * 1000;
    public static final int CONNECT_TIMEOUT    = 5  * 1000;

    private Socket mSocket;

    private Handler mHandler;

    private ReaderThread mReader;

    private WriterThread mWriter;

    private boolean mIsConnected = false;

    private long mLastHeartBeatResponse;

    private String mHost;

    private int mPort;

    private BlockingQueue<String> mWriterQueue = new LinkedBlockingQueue<>();

    private final Handler mReadHandler = new Handler() {
        @Override
        public void handleMessage(Message msgFromReader) {
            final Bundle data = msgFromReader.getData();

            assert data != null;

            final String message = data.getString(ReaderThread.EXTRA_MESSAGE);
            final boolean isInterrupted = data.getBoolean(ReaderThread.EXTRA_INTERRUPTED);

            if (isInterrupted) {
                disconnect();

            } else if (TextUtils.equals("BEAT", message)) {
                mLastHeartBeatResponse = System.currentTimeMillis();

            } else {
                final Message msg = mHandler.obtainMessage(MSG_MESSAGE_RECEIVED);
                final Bundle bundle = new Bundle();

                bundle.putString(EXTRA_MESSAGE, message);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }
    };

    private Thread mHeartBeatThread;

    private final Runnable mHeartBeat = new Runnable() {
        @Override
        public void run() {
            mLastHeartBeatResponse = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    // ignore
                }

                send("HEART");

                if (HEARTBEAT_TIMEOUT < System.currentTimeMillis() - mLastHeartBeatResponse) {
                    dispatchError();
                    disconnect();
                }
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
                mWriter = new WriterThread(mWriterQueue, new PrintWriter(
                                new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")));

                mReader.start();
                mWriter.start();

                mHeartBeatThread = new Thread(mHeartBeat);
                mHeartBeatThread.start();

                mIsConnected = true;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_CONNECTED));

            } catch (IOException exception) {
                Log.w(TAG, exception.getMessage());
                mIsConnected = false;
                dispatchError();
                disconnect();
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

        disconnect();
        Executors.defaultThreadFactory().newThread(mConnectRunnable).start();
    }

    @Override
    public void disconnect() {
        if (mHeartBeatThread != null) {
            mHeartBeatThread.interrupt();
            mHeartBeatThread = null;
        }

        if (mReader != null) {
            mReader.interrupt();
            mReader = null;
        }

        if (mWriter != null) {
            mWriter.interrupt();
            mWriter = null;
        }

        mSocket = null;

        if (mIsConnected) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_DISCONNECTED));
        }

        mIsConnected = false;
    }

    @Override
    public void send(final String message) {
        mWriterQueue.add(message);
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
