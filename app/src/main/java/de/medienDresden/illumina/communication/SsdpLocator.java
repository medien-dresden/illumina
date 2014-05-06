/*
 * Copyright (c) 2014 Peter Heisig.
 *
 * This work is licensed under a Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 */

package de.medienDresden.illumina.communication;

public interface SsdpLocator {

    public interface Consumer {

        void onSsdpServiceFound(String address, int port);

        void onNoSsdpServiceFound();

    }

    void discover();

}
