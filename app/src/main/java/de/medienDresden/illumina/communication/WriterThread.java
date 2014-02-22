package de.medienDresden.illumina.communication;

import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

public class WriterThread extends Thread {

    public static final Logger log = LoggerFactory.getLogger(WriterThread.class);

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
                log.info("writing was interrupted");
                break;
            }

            if (TextUtils.equals("HEART", message)) {
                log.info("RAW write: " + message);
            } else {
                log.info("RAW write: " + message);
            }

            mStream.write(message + "\n");
            mStream.flush();
        }
    }

}
