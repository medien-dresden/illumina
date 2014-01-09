package de.medienDresden.illumina;

import android.app.Application;
import android.content.Intent;

import de.medienDresden.illumina.impl.PilightServiceImpl;

public class Illumina extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /* If this service isn't started explicitly, it would be
         * destroyed if no more clients are bound */
        startService(new Intent(this, PilightServiceImpl.class));
    }

}
