package de.medienDresden.illumina;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import de.medienDresden.illumina.pilight.Device;

public class DeviceAdapter extends ArrayAdapter<Device> {

    public interface CheckHelper {

        void setChecked(int position, boolean checked);

    }

    interface ChangeListener {

        void onChanged(Device device);

    }

    private final ChangeListener mChangeListener;

    private final CheckHelper mCheckHelper;

    public DeviceAdapter(Context context, List<Device> objects,
                         CheckHelper helper, ChangeListener listener) {

        super(context, 0, objects);

        mCheckHelper = helper;
        mChangeListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final Device device = getItem(position);
        final Device.Type type = device.getType();

        View view = convertView;
        DeviceViewHolder viewHolder = null;

        if (view == null) {
            switch (type) {
                case Switch:
                    mCheckHelper.setChecked(position,
                            TextUtils.equals(device.getValue(), Device.VALUE_ON));

                    view = inflater.inflate(R.layout.device_list_item_switch, parent, false);
                    viewHolder = new SwitchViewHolder(view);
                    break;

                case Dimmer:
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

        viewHolder.update(device);
        viewHolder.setChangeListener(mChangeListener);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private static abstract class DeviceViewHolder {

        private ChangeListener mListener;

        private Device mDevice;

        void update(Device device) {
            mDevice = device;
        }

        void setChangeListener(ChangeListener listener) {
            mListener = listener;
        }

        Device getDevice() {
            return mDevice;
        }

        ChangeListener getListener() {
            return mListener;
        }

    }

    private static class SwitchViewHolder extends DeviceViewHolder {

        private TextView mName;

        SwitchViewHolder(View view) {
            mName = (TextView) view.findViewById(android.R.id.text1);
        }

        void update(Device device) {
            super.update(device);
            mName.setText(device.getName());
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
                    getListener().onChanged(getDevice());
                }
            });
        }

        void update(Device device) {
            super.update(device);
            mSeekBar.setProgress(device.getDimLevel());
        }

    }

}
