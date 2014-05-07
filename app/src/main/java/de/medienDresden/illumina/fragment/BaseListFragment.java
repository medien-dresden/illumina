/*
 * Copyright 2014 Peter Heisig
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package de.medienDresden.illumina.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ListFragment;

import org.slf4j.Logger;

import java.util.ArrayList;

import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;
import de.medienDresden.illumina.service.PilightBinder;

public abstract class BaseListFragment extends ListFragment implements
        PilightBinder.ServiceListener {

    private PilightBinder mBinder;

    @Override
    public void onPilightError(int cause) {
        getLogger().info("onPilightError(" + cause + ")");
    }

    @Override
    public void onPilightConnected() {
        getLogger().info("onPilightConnected");
    }

    @Override
    public void onPilightDisconnected() {
        getLogger().info("onPilightDisconnected");
    }

    @Override
    public void onPilightDeviceChange(Device device) {
        getLogger().info("onPilightDeviceChange(" + device.getId() + ")");
    }

    @Override
    public void onServiceConnected() {
        getLogger().info("onServiceConnected");
    }

    @Override
    public void onServiceDisconnected() {
        getLogger().info("onServiceDisconnected");
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        getLogger().info("onLocationListResponse, #locations = " + locations.size());
    }

    @Override
    public void onLocationResponse(Location location) {
        getLogger().info("onLocationResponse(" + location.getId() + ")");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinder = new PilightBinder(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinder.bindService(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinder.unbindService(getActivity());
    }

    protected void dispatch(Message message) {
        getLogger().info("dispatch(" + message.what + ")");
        mBinder.send(message);
    }

    abstract protected Logger getLogger();

}
