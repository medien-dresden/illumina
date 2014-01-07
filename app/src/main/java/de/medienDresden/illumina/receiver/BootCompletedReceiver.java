package de.medienDresden.illumina.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.medienDresden.Illumina;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(Illumina.ACTION_SERVICE_MAKE_AVAILABLE));
    }

}
