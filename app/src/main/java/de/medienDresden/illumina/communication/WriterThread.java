/*
 * Copyright 2014 Peter Heisig
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

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
