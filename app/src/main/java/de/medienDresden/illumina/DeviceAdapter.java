package de.medienDresden.illumina;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.util.List;

import de.medienDresden.illumina.pilight.Device;

public class DeviceAdapter extends ArrayAdapter<Device> {

    private final int mLayoutResourceId;

    private final CheckHelper mCheckHelper;

    public interface CheckHelper {
        void setChecked(int position, boolean checked);
    }

    public DeviceAdapter(Context context, int resource, List<Device> objects, CheckHelper helper) {
        super(context, 0, objects);

        mLayoutResourceId = resource;
        mCheckHelper = helper;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());

            view = inflater.inflate(mLayoutResourceId, parent, false);
            viewHolder = new DeviceViewHolder(view);
            view.setTag(viewHolder);

        } else {
            viewHolder = (DeviceViewHolder) view.getTag();
        }

        final Device device = getItem(position);
        viewHolder.update(device);

        mCheckHelper.setChecked(position, TextUtils.equals(device.getValue(), Device.VALUE_ON));

        return view;
    }

    private static class DeviceViewHolder {

        private CheckedTextView mDevice;

        DeviceViewHolder(View view) {
            mDevice = (CheckedTextView) view.findViewById(android.R.id.text1);
        }

        void update(Device device) {
            mDevice.setText(device.getName());
        }

    }

}
