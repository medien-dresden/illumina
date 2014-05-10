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

package de.medienDresden.illumina.pilight;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Location implements Parcelable {

    private String mName;

    private int mOrder;

    private String mId;

    private Map<String, Device> mDevices = new LinkedHashMap<>();

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Location() {}

    public static final Parcelable.Creator<Location> CREATOR
            = new Parcelable.Creator<Location>() {

        @Override
        public Location createFromParcel(Parcel parcel) {
            return new Location(parcel);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }

    };

    public Location(Parcel parcel) {
        mId = parcel.readString();
        mName = parcel.readString();
        mOrder = parcel.readInt();

        final Bundle bundle = parcel.readBundle();
        assert bundle != null;

        bundle.setClassLoader(getClass().getClassLoader());

        for (String deviceId : bundle.keySet()) {
            mDevices.put(deviceId, (Device) bundle.getParcelable(deviceId));
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeInt(mOrder);

        final Bundle devices = new Bundle();

        for (String deviceId : mDevices.keySet()) {
            devices.putParcelable(deviceId, mDevices.get(deviceId));
        }

        parcel.writeBundle(devices);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void addSorted(Map<String, Device> devices) {
        final List<Map.Entry<String, Device>> entries = new LinkedList<>(devices.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Device>>() {
            @Override
            public int compare(Map.Entry<String, Device> e1,
                               Map.Entry<String, Device> e2) {

                final int o1 = e1.getValue().getOrder();
                final int o2 = e2.getValue().getOrder();

                if (o1 > o2) {
                    return 1;
                } else if (o1 < o2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (Map.Entry<String, Device> entry : entries) {
            mDevices.put(entry.getKey(), entry.getValue());
        }
    }

    public Device get(String deviceId) {
        return mDevices.get(deviceId);
    }

    public int size() {
        return mDevices.size();
    }

    public Collection<Device> values() {
        return mDevices.values();
    }
}
