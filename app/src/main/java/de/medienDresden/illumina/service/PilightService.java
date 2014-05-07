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

package de.medienDresden.illumina.service;

public interface PilightService {

    interface Request {

        /** Command to the service to register a client, receiving callbacks
         * from the service. The Message's replyTo field must be a Messenger of
         * the client where callbacks should be sent. */
        public static final int REGISTER = 10;

        /** Command to the service to unregister a client, ot stop receiving callbacks
         * from the service. The Message's replyTo field must be a Messenger of
         * the client as previously given with MSG_REGISTER. */
        public static final int UNREGISTER = 20;

        /** Connection demand message */
        public static final int PILIGHT_CONNECT = 30;

        /** Disconnect request message */
        public static final int PILIGHT_DISCONNECT = 40;

        public static final int LOCATION_LIST = 50;

        public static final int LOCATION = 130;

        public static final int DEVICE_CHANGE = 100;

        public static final int STATE = 140;

    }

    interface News {

        /** Established connection indication message */
        public static final int CONNECTED = 60;

        /** Closed connection indication message */
        public static final int DISCONNECTED = 70;

        /** Error message */
        public static final int ERROR = 80;

        public static final int LOCATION_LIST = 90;

        public static final int DEVICE_CHANGE = 110;

        public static final int LOCATION = 120;

    }

    interface Extra {

        public static final String LOCATION_LIST = "location_list";

        public static final String DEVICE = "device";

        public static final String LOCATION = "location";

        public static final String LOCATION_ID = "location_id";

        public static final String CHANGED_PROPERTY = "changed_property";

    }

    interface Error {
        public static final int UNKNOWN = 1;
        public static final int CONNECTION_FAILED = 2;
        public static final int REMOTE_CLOSED = 3;
        public static final int HANDSHAKE_FAILED = 4;
    }

}
