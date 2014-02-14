package de.medienDresden.illumina.communication;

public interface SsdpLocator {

    public interface Consumer {

        void onSsdpServiceFound(String address, int port);

        void onNoSsdpServiceFound();

    }

    void discover();

}
