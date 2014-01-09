package de.medienDresden.illumina;

import de.medienDresden.illumina.pilight.Setting;

public interface PilightService {

    static final String ACTION_REMOTE_CHANGE = "action_remote_change";

    static final String ACTION_LOCAL_CHANGE = "action_local_change";

    static final String EXTRA_DEVICE = "device";

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
