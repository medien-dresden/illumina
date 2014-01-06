package de.medienDresden.illumina;

import android.app.Application;
import android.content.Intent;

public class Illumina extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, PilightService.class));
    }

}
