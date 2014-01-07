package de.medienDresden;

public interface Illumina {

    public static final String PACKAGE_NAME = "de.medienDresden.illumina";

    public static final String ACTION_SERVICE_CONNECT
            = PACKAGE_NAME + ".ACTION_SERVICE_CONNECT";

    public static final String ACTION_SERVICE_DISCONNECT
            = PACKAGE_NAME + ".ACTION_SERVICE_DISCONNECT";

    public static final String ACTION_SERVICE_MAKE_AVAILABLE
            = PACKAGE_NAME + ".ACTION_SERVICE_MAKE_AVAILABLE";

    public static final String ACTION_SERVICE_AVAILABLE
            = PACKAGE_NAME + ".ACTION_SERVICE_AVAILABLE";

    public static final String EXTRA_PORT = PACKAGE_NAME + ".EXTRA_PORT";

    public static final String EXTRA_HOST = PACKAGE_NAME + ".EXTRA_HOST";

}
