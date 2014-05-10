/*
 * illumina, a pilight remote
 *
 * Copyright (c) 2014 Peter Heisig <http://google.com/+PeterHeisig>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
