package de.medienDresden.illumina.pilight;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Setting extends HashMap<String, Location> {

    private static final String TAG = Setting.class.getSimpleName();

    public static Setting create(JSONObject json) throws JSONException {
        return new Setting(json);
    }

    private Setting(JSONObject locationsJson) throws JSONException {
        final Iterator locationsJsonIterator = locationsJson.keys();

        while (locationsJsonIterator.hasNext()) {
            final String currentLocation = (String) locationsJsonIterator.next();
            final Location location = parseLocation(locationsJson.getJSONObject(currentLocation));

            put(currentLocation, location);
        }
    }

    private Location parseLocation(JSONObject jsonLocation) throws JSONException {
        final Location location = new Location();
        final Iterator locationJsonIterator = jsonLocation.keys();

        while (locationJsonIterator.hasNext()) {
            final String currentLocationAttribute = (String) locationJsonIterator.next();

            switch (currentLocationAttribute) {
                case "name":
                    location.setName(jsonLocation.optString(currentLocationAttribute));
                    break;

                case "order":
                    location.setOrder(jsonLocation.optInt(currentLocationAttribute));
                    break;

                default:
                    final JSONObject device = jsonLocation.optJSONObject(currentLocationAttribute);

                    if (device != null) {
                        location.put(currentLocationAttribute, parseDevice(device));
                    } else {
                        Log.d(TAG, "unhandled device parameter " + currentLocationAttribute
                                + ":" + jsonLocation.optString(currentLocationAttribute));
                    }

                    break;
            }
        }

        return location;
    }

    private Device parseDevice(JSONObject jsonDevice) throws JSONException {
        final Device device = new Device();
        final Iterator deviceJsonIterator = jsonDevice.keys();

        while (deviceJsonIterator.hasNext()) {
            final String currentDeviceAttribute = (String) deviceJsonIterator.next();

            switch (currentDeviceAttribute) {
                case "name":
                    device.setName(jsonDevice.optString(currentDeviceAttribute));
                    break;

                case "order":
                    device.setOrder(jsonDevice.optInt(currentDeviceAttribute));
                    break;

                case "state":
                    device.setValue(jsonDevice.optString(currentDeviceAttribute));
                    break;

                case "dimlevel":
                    device.setType(Device.Type.Dimmer); // assuming dimmer device
                    device.setDimLevel(jsonDevice.optInt(currentDeviceAttribute));
                    break;

                case "values":
                    final ArrayList<String> list = new ArrayList<>();
                    final JSONArray jsonArray = jsonDevice.optJSONArray(currentDeviceAttribute);

                    if (jsonArray != null) {
                        final int len = jsonArray.length();
                        for (int i = 0; i < len; i++) {
                            list.add(jsonArray.get(i).toString());
                        }
                    }

                    device.setValues(list);
                    break;

                default:
                    Log.d(TAG, "unhandled device parameter " + currentDeviceAttribute
                            + ":" + jsonDevice.optString(currentDeviceAttribute));
                    break;
            }
        }

        return device;
    }

}
