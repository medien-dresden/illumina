package de.medienDresden;

public interface Illumina {

    public interface ServiceError {

        public static final int UNKNOWN = 0;

        public static final int CONNECTION_FAILED = 1;

        public static final int REMOTE_CLOSED_CONNECTION = 2;

        public static final int HANDSHAKE_FAILED = 3;

    }

    public static final String PACKAGE_NAME = "de.medienDresden.illumina";

    public static final String ACTION_CONNECT_REQUEST
            = PACKAGE_NAME + ".ACTION_CONNECT_REQUEST";

    public static final String ACTION_CONNECTED
            = PACKAGE_NAME + ".ACTION_CONNECTED";

    public static final String ACTION_DISCONNECT
            = PACKAGE_NAME + ".ACTION_DISCONNECT";

    public static final String ACTION_SERVICE_AVAILABILITY_REQUEST
            = PACKAGE_NAME + ".ACTION_SERVICE_AVAILABILITY_REQUEST";

    public static final String ACTION_SERVICE_AVAILABLE
            = PACKAGE_NAME + ".ACTION_SERVICE_AVAILABLE";

    public static final String ACTION_SERVICE_ERROR
            = PACKAGE_NAME + ".ACTION_SERVICE_ERROR";

    public static final String EXTRA_PORT = PACKAGE_NAME + ".EXTRA_PORT";

    public static final String EXTRA_HOST = PACKAGE_NAME + ".EXTRA_HOST";

    public static final String EXTRA_ERROR_CODE = PACKAGE_NAME + ".EXTRA_ERROR_CODE";

}
