package de.medienDresden.illumina.pilight;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Location extends HashMap<String, Device> implements Parcelable {

    private String mName;

    private int mOrder;

    private String mId;

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
            put(deviceId, (Device) bundle.getParcelable(deviceId));
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeInt(mOrder);

        final Bundle devices = new Bundle();

        for (String deviceId : keySet()) {
            devices.putParcelable(deviceId, get(deviceId));
        }

        parcel.writeBundle(devices);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void addSorted(Map<String, Device> devices) {
        final List<Entry<String, Device>> entries = new LinkedList<>(devices.entrySet());

        Collections.sort(entries, new Comparator<Entry<String, Device>>() {
            @Override
            public int compare(Entry<String, Device> e1,
                               Entry<String, Device> e2) {

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
            put(entry.getKey(), entry.getValue());
        }
    }

}
