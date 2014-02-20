package de.medienDresden.illumina.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamingSocketImpl implements StreamingSocket {

    public static final Logger log = LoggerFactory.getLogger(StreamingSocketImpl.class);

    @SuppressWarnings("PointlessArithmeticExpression")
    public static final int HEARTBEAT_INTERVAL = 1 * 1000;
    public static final int HEARTBEAT_TIMEOUT  = 2 * 1000;
    public static final int READ_TIMEOUT       = 5 * 1000;
    public static final int CONNECT_TIMEOUT    = 5 * 1000;

    private Socket mSocket;

    private Handler mHandler;

    private ReaderThread mReaderThread;

    private WriterThread mWriterThread;

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
                dispatchError();

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
                send("HEART");

                if (HEARTBEAT_TIMEOUT < System.currentTimeMillis() - mLastHeartBeatResponse) {
                    dispatchError();
                }

                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };

    private Thread mConnectThread;

    private final Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(mHost, mPort), CONNECT_TIMEOUT);
                mSocket.setSoTimeout(READ_TIMEOUT);

                mReaderThread = new ReaderThread(mSocket.getInputStream(), mReadHandler);
                mWriterThread = new WriterThread(mWriterQueue, new PrintWriter(
                                new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")));

                mWriterQueue.clear();

                mReaderThread.start();
                mWriterThread.start();

                mIsConnected = true;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_CONNECTED));

            } catch (Exception exception) {
                if (!TextUtils.isEmpty(exception.getMessage())) {
                    log.warn(exception.getMessage(), exception);
                } else {
                    log.warn("connection failed", exception);
                }

                dispatchError();
            }
        }
    };

    private boolean mHasDispatchedError;

    public StreamingSocketImpl(Handler handler) {
        mHandler = handler;
    }

    private void dispatchError() {
        if (!mHasDispatchedError) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ERROR));
            disconnect();

            mHasDispatchedError = true;
            mIsConnected = false;
        }
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

        mHasDispatchedError = false;
        mConnectThread = new Thread(mConnectRunnable, "SOCKET CONNECT");
        mConnectThread.start();
    }

    @Override
    public void disconnect() {
        if (mConnectThread != null) {
            mConnectThread.interrupt();
            mConnectThread = null;
        }

        if (mHeartBeatThread != null) {
            mHeartBeatThread.interrupt();
            mHeartBeatThread = null;
        }

        if (mReaderThread != null) {
            mReaderThread.interrupt();
            mReaderThread = null;
        }

        if (mWriterThread != null) {
            mWriterThread.interrupt();
            mWriterThread = null;
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
    public void startHeartBeat() {
        mHeartBeatThread = new Thread(mHeartBeat, "SOCKET HEARTBEAT");
        mHeartBeatThread.start();
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
