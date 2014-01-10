package de.medienDresden.illumina.communication.impl;

import android.util.Log;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

class WriterThread extends Thread {

    private static final String TAG = WriterThread.class.getSimpleName();

    private final BlockingQueue<String> mQueue;

    private final PrintWriter mStream;

    public WriterThread(BlockingQueue<String> queue, PrintWriter stream) {
        super("SOCKET WRITER");

        mQueue = queue;
        mStream = stream;
    }

    @Override
    public void run() {
        String message;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                message = mQueue.take();
            } catch (InterruptedException exception) {
                Log.i(TAG, "writing was interrupted");
                break;
            }

            Log.d(TAG, "RAW write: " + message);

            mStream.write(message + "\n");
            mStream.flush();
        }
    }

}
