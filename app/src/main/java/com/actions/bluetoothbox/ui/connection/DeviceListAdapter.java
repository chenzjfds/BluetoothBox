package com.actions.bluetoothbox.ui.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;

import java.util.List;


public class DeviceListAdapter extends BaseAdapter {

    private List<DeviceEntry> mItems;
    private LayoutInflater mInflater;
    private BrowserActivity mActivity;
    private boolean isConnecting = false;

    public DeviceListAdapter(Context context, List<DeviceEntry> devices) {
        mItems = devices;
        mActivity = (BrowserActivity) context;
        mInflater = LayoutInflater.from(context);
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder;

        if (convertView == null) {
            holder = new ItemHolder();
            convertView = mInflater.inflate(R.layout.device_list_item_tmp, null);
            holder.image = (ImageView) convertView.findViewById(R.id.connectionStateImage);
            holder.name = (TextView) convertView.findViewById(R.id.deviceName);
            holder.state = (TextView) convertView.findViewById(R.id.deviceState);
            holder.shutdown = (ImageButton) convertView.findViewById(R.id.disconncectBtn);
            holder.shutdown.setVisibility(View.GONE);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        DeviceEntry entry = mItems.get(position);
        if (entry.device == null) {
            /**
             * FIXME device null only in the case of BluzDeviceStub
             */
            holder.name.setText("Audio_SPP_Stub");
        } else {
            holder.name.setText(entry.device.getName());
        }

        switch (entry.state) {
            case ConnectionState.SPP_CONNECTED:
                holder.image.setImageResource(R.drawable.ic_device_connected);
                holder.state.setText(R.string.notice_device_connected);
                holder.shutdown.setVisibility(View.VISIBLE);
                holder.shutdown.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mActivity.showDisconnectDialog();
                    }
                });
                break;

            case ConnectionState.SPP_CONNECTING:
                holder.image.setImageResource(R.drawable.ic_device_media_connected);
                holder.state.setText(R.string.notice_device_connecting);
                holder.shutdown.setVisibility(View.GONE);
                break;

            case ConnectionState.SPP_FAILURE:
                holder.image.setImageResource(R.drawable.ic_device_disconnected);
                holder.state.setText(R.string.notice_device_disconnected);
                holder.shutdown.setVisibility(View.GONE);
                break;

            case ConnectionState.A2DP_CONNECTING:
                holder.image.setImageResource(R.drawable.ic_device_disconnected);
                holder.state.setText(R.string.notice_device_connecting);
                holder.shutdown.setVisibility(View.GONE);
                break;

            case ConnectionState.A2DP_CONNECTED:
                holder.image.setImageResource(R.drawable.ic_device_media_connected);
                holder.state.setText(R.string.notice_device_connecting);
                holder.shutdown.setVisibility(View.GONE);
                break;

            case ConnectionState.A2DP_DISCONNECTED:
                holder.image.setImageResource(R.drawable.ic_device_disconnected);
                holder.state.setText(R.string.notice_device_disconnected);
                holder.shutdown.setVisibility(View.GONE);
                break;

            case ConnectionState.A2DP_PAIRING:
                holder.image.setImageResource(R.drawable.ic_device_disconnected);
                holder.state.setText(R.string.notice_device_media_pairing);
                holder.shutdown.setVisibility(View.GONE);
                break;
        }
        holder.name.setEllipsize(TruncateAt.END);
        convertView.setTag(holder);
        return convertView;
    }

    public static class DeviceEntry {
        public BluetoothDevice device;
        public int state;

        public DeviceEntry(BluetoothDevice device, int state) {
            this.device = device;
            this.state = state;
        }
    }

    public final class ItemHolder {
        public ImageView image = null;
        public TextView name = null;
        public TextView state = null;
        public ImageButton shutdown = null;
    }
}