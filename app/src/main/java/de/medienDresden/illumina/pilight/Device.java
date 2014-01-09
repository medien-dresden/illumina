package de.medienDresden.illumina.pilight;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Device implements Parcelable {

    public enum Type {
        Switch,
        Dimmer
    }

    private String mName;

    private int mOrder;

    private String mValue;

    private ArrayList<String> mValues;

    private Type mType = Type.Switch;

    private int mDimLevel;

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

    public ArrayList<String> getValues() {
        return mValues;
    }

    public void setValues(ArrayList<String> values) {
        mValues = values;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
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

    public Device() {
        mValues = new ArrayList<>();
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
        this();

        mName = parcel.readString();
        mOrder = parcel.readInt();
        mValue = parcel.readString();
        parcel.readStringList(mValues);
        mType = (Type) parcel.readSerializable();
        mDimLevel = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeInt(mOrder);
        parcel.writeString(mValue);
        parcel.writeStringList(mValues);
        parcel.writeSerializable(mType);
        parcel.writeInt(mDimLevel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return mName;
    }
}
