package de.medienDresden.illumina.pilight;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;
import java.util.HashMap;

public class Location extends HashMap<String, Device> implements Parcelable {


    private String mName;

    private int mOrder;

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

    public Location() {}

    public Location(Parcel parcel) {
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

    public static class OrderComparator implements Comparator<Location> {

        @Override
        public int compare(Location l1, Location l2) {
            if (l1.getOrder() > l2.getOrder()) {
                return -1;
            } else if (l1.getOrder() < l2.getOrder()) {
                return 1;
            } else {
                return 0;
            }
        }

    }

}
