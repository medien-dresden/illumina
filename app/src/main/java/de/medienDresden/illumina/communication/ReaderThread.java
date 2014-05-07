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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ReaderThread extends Thread {

    public static final Logger log = LoggerFactory.getLogger(ReaderThread.class);

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
            log.error("cannot create socket reader", exception);
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
                log.info("reading was interrupted");
                bundle.putBoolean(EXTRA_INTERRUPTED, true);
                mHandler.sendMessage(msg);
                break;
            }

            if (TextUtils.isEmpty(message)) {
                continue;
            }

            if (TextUtils.equals("BEAT", message)) {
                log.info("RAW read: " + message);
            } else {
                log.info("RAW read: " + message);
            }

            bundle.putString(EXTRA_MESSAGE, message);
            mHandler.sendMessage(msg);
        }
    }

}
