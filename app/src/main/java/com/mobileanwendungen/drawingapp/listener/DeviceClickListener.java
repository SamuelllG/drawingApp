package com.mobileanwendungen.drawingapp.listener;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

public class DeviceClickListener implements AdapterView.OnItemClickListener {
    private static final String TAG = "cust.DevClickListener";

    private BluetoothController bluetoothController;

    public DeviceClickListener(BluetoothController bluetoothController) {
        this.bluetoothController = bluetoothController;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevices bluetoothDevices = bluetoothController.getBluetoothDevices();
        BluetoothDevice device = bluetoothDevices.getDevice(position);

        if (bluetoothDevices.isBonded(device)) {
            Log.d(TAG, "onItemClick: clicked on a bonded device");
        } else {
            Log.d(TAG, "onItemClick: clicked on a device");
        }

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        bluetoothController.onDeviceClicked(device);
    }
}
