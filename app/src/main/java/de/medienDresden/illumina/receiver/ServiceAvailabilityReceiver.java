package de.medienDresden.illumina.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.concurrent.Executors;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.PilightService;

public class ServiceAvailabilityReceiver extends BroadcastReceiver {

    private final ServiceAvailabilityRunnable mServiceAvailabilityRunnable
            = new ServiceAvailabilityRunnable();

    private static class ServiceAvailabilityRunnable implements Runnable {

        private Context mContext;

        ServiceAvailabilityRunnable with(Context context) {
            mContext = context;
            return this;
        }

        @Override
        public void run() {
            ActivityManager manager = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);

            final List<ActivityManager.RunningServiceInfo> runningServices
                    = manager.getRunningServices(Integer.MAX_VALUE);

            boolean isAvailable = false;

            if (runningServices != null) {
                for (ActivityManager.RunningServiceInfo service : runningServices) {
                    if (PilightService.class.getName().equals(service.service.getClassName())) {
                        isAvailable = true;
                    }
                }
            }

            if (isAvailable) {
                mContext.sendBroadcast(new Intent(Illumina.ACTION_SERVICE_AVAILABLE));
            } else {
                mContext.startService(new Intent(
                        mContext.getApplicationContext(), PilightService.class));
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Executors.defaultThreadFactory()
                .newThread(mServiceAvailabilityRunnable.with(context))
                .start();
    }

}
