package de.medienDresden.illumina.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Device;

public class DeviceAdapter extends ArrayAdapter<Device> {

    private DimLevelListener mDimLevelListener;

    public interface DimLevelListener {
        void onDimLevelChanged(Device device);
    }

    public DeviceAdapter(Context context, List<Device> objects, DimLevelListener dimLevelListener) {
        super(context, 0, objects);
        mDimLevelListener = dimLevelListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final Device device = getItem(position);
        final int type = device.getType();

        View view = convertView;
        DeviceViewHolder viewHolder = null;

        if (view == null) {
            switch (type) {
                case Device.TYPE_SWITCH:
                    view = inflater.inflate(R.layout.device_list_item_switch, parent, false);
                    viewHolder = new SwitchViewHolder(view);
                    break;

                case Device.TYPE_DIMMER:
                    view = inflater.inflate(R.layout.device_list_item_dimmer, parent, false);
                    viewHolder = new DimmerViewHolder(view);
                    break;
            }

            assert view != null;
            view.setTag(viewHolder);

        } else {
            viewHolder = (DeviceViewHolder) view.getTag();
        }

        assert viewHolder != null;

        viewHolder.setDevice(device);
        viewHolder.setDimLevelListener(mDimLevelListener);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static abstract class DeviceViewHolder {

        private Device mDevice;

        private TextView mName;

        private DimLevelListener mDimLevelListener;

        DeviceViewHolder(View view) {
            mName = (TextView) view.findViewById(android.R.id.text1);
        }

        void setDevice(Device device) {
            mDevice = device;
            mName.setText(device.getName());
        }

        Device getDevice() {
            return mDevice;
        }

        void setDimLevelListener(DimLevelListener dimLevelListener) {
            mDimLevelListener = dimLevelListener;
        }

        DimLevelListener getDimLevelListener() {
            return mDimLevelListener;
        }

    }

    private static class SwitchViewHolder extends DeviceViewHolder {

        private CheckBox mCheckBox;

        SwitchViewHolder(View view) {
            super(view);
            mCheckBox = (CheckBox) view.findViewById(android.R.id.checkbox);
        }

        void setDevice(Device device) {
            super.setDevice(device);
            mCheckBox.setChecked(device.isOn());
        }

    }

    private static class DimmerViewHolder extends SwitchViewHolder {

        private SeekBar mSeekBar;

        DimmerViewHolder(View view) {
            super(view);
            mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    getDevice().setDimLevel(seekBar.getProgress());
                    getDimLevelListener().onDimLevelChanged(getDevice());
                }
            });
        }

        void setDevice(Device device) {
            super.setDevice(device);
            mSeekBar.setProgress(device.getDimLevel());
        }

    }

}
