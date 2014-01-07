package de.medienDresden.illumina.pilight;

import java.util.HashMap;

public class Location extends HashMap<String, Device> {

    private String mName;

    private int mOrder;

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

}
