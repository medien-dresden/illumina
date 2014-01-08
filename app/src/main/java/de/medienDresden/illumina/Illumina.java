package de.medienDresden.illumina;

import android.app.Application;
import android.content.Intent;

import de.medienDresden.illumina.impl.PilightServiceImpl;

public class Illumina extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, PilightServiceImpl.class));
    }
}
