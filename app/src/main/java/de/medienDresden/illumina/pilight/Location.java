package de.medienDresden.illumina.pilight;

import java.util.Comparator;
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
