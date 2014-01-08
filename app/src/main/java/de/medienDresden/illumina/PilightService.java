package de.medienDresden.illumina;

import de.medienDresden.illumina.pilight.Setting;

public interface PilightService {

    enum Error {
        Unknown,
        ConnectionFailed,
        RemoteClosedConnection,
        HandshakeFailed
    }

    interface ServiceHandler {

        void onPilightError(Error type);

        void onPilightConnected(Setting setting);

        void onPilightDisconnected();
    }

    void connect(String host, int port);

    void disconnect();

}
