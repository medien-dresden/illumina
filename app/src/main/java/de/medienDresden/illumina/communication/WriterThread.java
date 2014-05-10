/*
 * illumina, a pilight remote
 *
 * Copyright (c) 2014 Peter Heisig <http://google.com/+PeterHeisig>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
