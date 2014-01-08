package de.medienDresden.illumina;

import de.medienDresden.illumina.pilight.Setting;

public interface PilightService {

    enum Error {
        Unknown,
        ConnectionFailed,
        RemoteClosedConnection,
        HandshakeFailed;
    }
    interface ServiceHandler {

        void onPilightError(Error type);

        void onPilightConnected(Setting setting);

        void onPilightDisconnected();

    }

    boolean isConnected(String host, int port);

    Setting getSetting();

    void connect(String host, int port);

    void disconnect();

}
