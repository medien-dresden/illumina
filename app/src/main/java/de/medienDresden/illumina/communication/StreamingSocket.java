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
