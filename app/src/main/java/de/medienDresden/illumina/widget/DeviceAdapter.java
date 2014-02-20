package de.medienDresden.illumina.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Device;

public class DeviceAdapter extends ArrayAdapter<Device> {

    private DimLevelListener mDimLevelListener;

    private List<Device> mOriginalDeviceList;

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            final FilterResults result = new FilterResults();
            final List<Device> filteredDeviceList = new ArrayList<>();

            for (Device device : mOriginalDeviceList) {
                if (!device.isWritable() && device.getType() == Device.TYPE_SCREEN) {
                    continue;
                }

                filteredDeviceList.add(device);
            }

            result.values = filteredDeviceList;
            result.count = filteredDeviceList.size();

            return result;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();

            //noinspection unchecked
            addAll((ArrayList<Device>) filterResults.values);

            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };

    public interface DimLevelListener {
        void onDimLevelChanged(Device device);
    }

    public DeviceAdapter(Context context, List<Device> objects, DimLevelListener dimLevelListener) {
        super(context, 0, objects);

        mDimLevelListener = dimLevelListener;
        mOriginalDeviceList = objects;

        final TypedArray typedArray = context.obtainStyledAttributes(
                new int[]{R.attr.battery_full, R.attr.battery_empty});

        assert typedArray != null;
        WeatherViewHolder.setBatteryDrawables(
                context.getResources().getDrawable(typedArray.getResourceId(0, 0)),
                context.getResources().getDrawable(typedArray.getResourceId(1, 0))
        );

        typedArray.recycle();
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

                case Device.TYPE_SCREEN:
                    view = inflater.inflate(R.layout.device_list_item_screen, parent, false);
                    viewHolder = new ScreenViewHolder(view);
                    break;

                case Device.TYPE_WEATHER:
                    view = inflater.inflate(R.layout.device_list_item_weather, parent, false);
                    viewHolder = new WeatherViewHolder(view);
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
        return 4;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        final Device device = getItem(position);
        return device != null && device.isWritable();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
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
            mName.setTypeface(mName.getTypeface(), device.isWritable()
                    ? Typeface.NORMAL : Typeface.ITALIC);
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
            mCheckBox.setEnabled(device.isWritable());
        }

    }

    private static class ScreenViewHolder extends DeviceViewHolder {

        private ImageButton mUpButton;
        private ImageButton mDownButton;

        ScreenViewHolder(View view) {
            super(view);

            mUpButton = (ImageButton) view.findViewById(R.id.up_action);
            mDownButton = (ImageButton) view.findViewById(R.id.down_action);
        }

        void setDevice(Device device) {
            super.setDevice(device);
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
            mSeekBar.setEnabled(device.isWritable());
        }

    }

    private static class WeatherViewHolder extends DeviceViewHolder {

        private final ViewGroup mTemperature;
        private final TextView mTemperatureText;
        private final ViewGroup mHumidity;
        private final TextView mHumidityText;
        private final ImageView mBatteryImage;

        private final DecimalFormat temperatureFormat = new DecimalFormat("#°");
        private final DecimalFormat humidityFormat = new DecimalFormat("#%");

        private static Drawable sBatteryFullDrawable;
        private static Drawable sBatteryEmptyDrawable;

        WeatherViewHolder(View view) {
            super(view);

            mTemperature = (ViewGroup) view.findViewById(R.id.temperature);
            mTemperatureText = (TextView) view.findViewById(R.id.temperature_text);

            mHumidity = (ViewGroup) view.findViewById(R.id.humidity);
            mHumidityText = (TextView) view.findViewById(R.id.humidity_text);

            mBatteryImage = (ImageView) view.findViewById(R.id.battery);
        }

        static void setBatteryDrawables(Drawable full, Drawable empty) {
            sBatteryEmptyDrawable = empty;
            sBatteryFullDrawable = full;
        }

        void setDevice(Device device) {
            super.setDevice(device);

            if (device.hasTemperatureValue() && device.isShowTemperature()) {
                mTemperature.setVisibility(View.VISIBLE);
                mTemperatureText.setText(temperatureFormat.format(
                        device.getTemperature() / Math.pow(10, device.getDecimals())));
            } else {
                mTemperature.setVisibility(View.GONE);
            }

            if (device.hasHumidityValue() && device.isShowHumidity()) {
                mHumidity.setVisibility(View.VISIBLE);
                mHumidityText.setText(humidityFormat.format(
                        device.getHumidity() / Math.pow(10, device.getDecimals()) / 100));
            } else {
                mHumidity.setVisibility(View.GONE);
            }

            if (device.hasBatteryValue() && device.isShowBattery()) {
                mBatteryImage.setVisibility(View.VISIBLE);
                mBatteryImage.setImageDrawable(device.hasHealthyBattery()
                        ? sBatteryFullDrawable : sBatteryEmptyDrawable);
            } else {
                mBatteryImage.setVisibility(View.GONE);
            }
        }

    }

}
