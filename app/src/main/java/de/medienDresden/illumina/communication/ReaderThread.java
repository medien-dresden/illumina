package de.medienDresden.illumina.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ReaderThread extends Thread {

    private static final String TAG = ReaderThread.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "message";

    public static final String EXTRA_INTERRUPTED = "interrupted";

    private Handler mHandler;

    private BufferedReader mBufferedReader;

    public ReaderThread(InputStream inputStream, Handler handler) {
        super("SOCKET READER");

        mHandler = handler;

        try {
            mBufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            Log.e(TAG, "cannot create socket reader", exception);
        }
    }

    @Override
    public void run() {
        String message;

        while (!isInterrupted()) {
            final Message msg = mHandler.obtainMessage();
            final Bundle bundle = new Bundle();

            msg.setData(bundle);

            try {
                while (!mBufferedReader.ready()) {
                    sleep(10);
                }

                message = mBufferedReader.readLine().trim();
            } catch (InterruptedException | IOException exception) {
                // happens even when disconnected on purpose
                Log.i(TAG, "reading was interrupted");
                bundle.putBoolean(EXTRA_INTERRUPTED, true);
                mHandler.sendMessage(msg);
                break;
            }

            if (TextUtils.isEmpty(message)) {
                continue;
            }

            if (TextUtils.equals("BEAT", message)) {
                Log.v(TAG, "RAW read: " + message);
            } else {
                Log.d(TAG, "RAW read: " + message);
            }

            bundle.putString(EXTRA_MESSAGE, message);
            mHandler.sendMessage(msg);
        }
    }

}
