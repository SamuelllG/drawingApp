package com.mobileanwendungen.drawingapp.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private BluetoothDevices bluetoothDevices;
    private int mViewResourceId;

    public DeviceListAdapter(Context context, int tvResourceId, BluetoothDevices bluetoothDevices){
        super(context, tvResourceId, bluetoothDevices.getDevices());
        this.bluetoothDevices = bluetoothDevices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = bluetoothDevices.getDevice(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAdress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
            TextView bonded = (TextView) convertView.findViewById(R.id.deviceBonded);
            TextView connected = (TextView) convertView.findViewById(R.id.deviceConnected);
            Button connectButton = (Button) convertView.findViewById(R.id.connectButton);

            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAdress != null) {
                deviceAdress.setText(device.getAddress());
            }
            if (bluetoothDevices.isBonded(device)) {
                connected.setTextColor(Color.YELLOW);
                connected.setText(getContext().getResources().getString(R.string.bonded));
                connectButton.setVisibility(View.VISIBLE);
                connectButton.setText(getContext().getResources().getString(R.string.connectButton));
                convertView.setBackgroundColor(Color.WHITE);
            }
            if (bluetoothDevices.isConnected(device)) {
                connected.setTextColor(Color.GREEN);
                connected.setText(getContext().getResources().getString(R.string.connected));
                convertView.setBackgroundColor(Color.LTGRAY);
                connectButton.setText(getContext().getResources().getString(R.string.disconnectButton));
            }
        }

        return convertView;
    }

}
