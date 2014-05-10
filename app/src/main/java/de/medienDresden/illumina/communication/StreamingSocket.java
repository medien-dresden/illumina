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

public interface StreamingSocket {

    public static final int MSG_MESSAGE_RECEIVED = 1;
    public static final int MSG_CONNECTED = 2;
    public static final int MSG_DISCONNECTED = 3;
    public static final int MSG_ERROR = 4;

    String EXTRA_MESSAGE = "message";

    boolean isConnected();

    void connect(String host, int port);

    void disconnect();

    void send(String message);

    void startHeartBeat();

    String getHost();

    int getPort();

}
