package de.medienDresden.illumina.communication.impl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Scanner;

class ReaderThread extends Thread {

    private static final String TAG = ReaderThread.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "message";

    public static final String EXTRA_INTERRUPTED = "interrupted";

    private Handler mHandler;

    private Scanner mScanner;

    public ReaderThread(InputStream inputStream, Handler handler) {
        mHandler = handler;

        try {
            mScanner = new Scanner(new InputStreamReader(inputStream, "UTF-8"));
            mScanner.useDelimiter("\n");

        } catch (UnsupportedEncodingException exception) {
            Log.e(TAG, "cannot create socket reader", exception);
        }
    }

    @Override
    public void run() {
        String message;

        while (!Thread.currentThread().isInterrupted()) {
            final Message msg = mHandler.obtainMessage();
            final Bundle bundle = new Bundle();

            msg.setData(bundle);

            try {
                message = mScanner.next();
                Log.d(TAG, "RAW read:  " + message);
            } catch (NoSuchElementException exception) {
                // happens even when disconnected on purpose
                Log.i(TAG, "reading was interrupted");
                bundle.putBoolean(EXTRA_INTERRUPTED, true);
                mHandler.sendMessage(msg);
                break;
            }

            bundle.putString(EXTRA_MESSAGE, message);
            mHandler.sendMessage(msg);
        }
    }

}
