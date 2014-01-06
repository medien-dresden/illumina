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
        final Bundle bundle = new Bundle();

        while (!Thread.currentThread().isInterrupted()) {
            final Message msg = mHandler.obtainMessage();

            try {
                bundle.putString(EXTRA_MESSAGE, mScanner.next());
            } catch (NoSuchElementException exception) {
                // happens even when disconnected on purpose
                break;
            }

            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

}
