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

package de.medienDresden.illumina.pilight;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Device implements Parcelable {

    public static final String VALUE_ON = "on";

    public static final String VALUE_OFF = "off";

    public static final String VALUE_UP = "up";

    public static final String VALUE_DOWN = "down";

    public static final String VALUE_OPENED = "opened";

    public static final String VALUE_CLOSED = "closed";

    public static final int TYPE_SWITCH = 0;

    public static final int TYPE_DIMMER = 1;

    public static final int TYPE_SCREEN = 2;

    public static final int TYPE_WEATHER = 3;

    public static final int TYPE_CONTACT = 4;

    public static final int PROPERTY_DIM_LEVEL = 1;

    public static final int PROPERTY_VALUE = 2;

    private String mId;

    private String mLocationId;

    private String mName;

    private int mOrder;

    private String mValue;

    private int mType = TYPE_SWITCH;

    private int mDimLevel;

    private int mTemperature;

    private boolean mShowTemperature;

    private int mHumidity;

    private boolean mShowHumidity;

    private boolean mShowBattery;

    private int mDecimals;

    private boolean mHasHealthyBattery;

    private boolean mHasBatteryValue = false;

    private boolean mHasTemperatureValue = false;

    private boolean mHasHumidityValue = false;

    private boolean mIsReadOnly = false;

    public Device() {}

    public String getLocationId() {
        return mLocationId;
    }

    public void setLocationId(String locationId) {
        mLocationId = locationId;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDimLevel(int dimLevel) {
        mDimLevel = dimLevel;
    }

    public int getDimLevel() {
        return mDimLevel;
    }

    public int getTemperature() {
        return mTemperature;
    }

    public void setReadOnly(boolean readOnly) {
        mIsReadOnly = readOnly;
    }

    public void setTemperature(int temperature) {
        mHasTemperatureValue = true;
        mTemperature = temperature;
    }

    public boolean isShowTemperature() {
        return mShowTemperature;
    }

    public boolean isShowBattery() {
        return mShowBattery;
    }

    public boolean hasHealthyBattery() {
        return mHasHealthyBattery;
    }

    public boolean hasTemperatureValue() {
        return mHasTemperatureValue;
    }

    public boolean hasHumidityValue() {
        return mHasHumidityValue;
    }

    public boolean hasBatteryValue() {
        return mHasBatteryValue;
    }

    public void setHealthyBattery(boolean hasHealthyBattery) {
        mHasBatteryValue = true;
        mHasHealthyBattery = hasHealthyBattery;
    }

    public void setShowTemperature(boolean showTemperature) {
        mShowTemperature = showTemperature;
    }

    public int getHumidity() {
        return mHumidity;
    }

    public void setHumidity(int humidity) {
        mHasHumidityValue = true;
        mHumidity = humidity;
    }

    public boolean isShowHumidity() {
        return mShowHumidity;
    }

    public void setShowHumidity(boolean showHumidity) {
        mShowHumidity = showHumidity;
    }

    public void setShowBattery(boolean showBattery) {
        mShowBattery = showBattery;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public void setDecimals(int decimals) {
        mDecimals = decimals;
    }

    public boolean isOn() {
        return TextUtils.equals(mValue, VALUE_ON);
    }

    public boolean isUp() {
        return TextUtils.equals(mValue, VALUE_UP);
    }

    public boolean isOpened() {
        return TextUtils.equals(mValue, VALUE_OPENED);
    }

    public boolean isClosed() {
        return TextUtils.equals(mValue, VALUE_CLOSED);
    }

    public static final Parcelable.Creator<Device> CREATOR
            = new Parcelable.Creator<Device>() {

        @Override
        public Device createFromParcel(Parcel parcel) {
            return new Device(parcel);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }

    };

    public Device(Parcel parcel) {
        mId = parcel.readString();
        mLocationId = parcel.readString();
        mName = parcel.readString();
        mOrder = parcel.readInt();
        mValue = parcel.readString();
        mType = parcel.readInt();
        mDimLevel = parcel.readInt();
        mTemperature = parcel.readInt();
        mHumidity = parcel.readInt();
        mDecimals = parcel.readInt();
        mHasHealthyBattery = Boolean.parseBoolean(parcel.readString());
        mShowTemperature = Boolean.parseBoolean(parcel.readString());
        mShowHumidity = Boolean.parseBoolean(parcel.readString());
        mShowBattery = Boolean.parseBoolean(parcel.readString());
        mHasBatteryValue = Boolean.parseBoolean(parcel.readString());
        mHasHumidityValue = Boolean.parseBoolean(parcel.readString());
        mHasTemperatureValue = Boolean.parseBoolean(parcel.readString());
        mIsReadOnly = Boolean.parseBoolean(parcel.readString());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mLocationId);
        parcel.writeString(mName);
        parcel.writeInt(mOrder);
        parcel.writeString(mValue);
        parcel.writeInt(mType);
        parcel.writeInt(mDimLevel);
        parcel.writeInt(mTemperature);
        parcel.writeInt(mHumidity);
        parcel.writeInt(mDecimals);
        parcel.writeString(mHasHealthyBattery ? "true" : "false");
        parcel.writeString(mShowTemperature ? "true" : "false");
        parcel.writeString(mShowHumidity ? "true" : "false");
        parcel.writeString(mShowBattery ? "true" : "false");
        parcel.writeString(mHasBatteryValue ? "true" : "false");
        parcel.writeString(mHasHumidityValue ? "true" : "false");
        parcel.writeString(mHasTemperatureValue ? "true" : "false");
        parcel.writeString(mIsReadOnly ? "true" : "false");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return mName;
    }

    public boolean isWritable() {
        switch (mType) {
            case TYPE_DIMMER:
            case TYPE_SCREEN:
            case TYPE_SWITCH:
                return !mIsReadOnly;

            case TYPE_WEATHER:
            case TYPE_CONTACT:
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Device device = (Device) o;
        return TextUtils.equals(mId, device.mId)
                && TextUtils.equals(mLocationId, device.mLocationId);

    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mLocationId.hashCode();
        return result;
    }

}
