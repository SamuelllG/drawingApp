package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothDevices;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.DeviceListAdapter;

public class DiscoverBroadcastReceiver extends BluetoothBroadcastReceiver {
    private static final String TAG = "cust.DiscoverableBR";

    private BluetoothDevices bluetoothDevices;
    private BluetoothController bluetoothController;

    public DiscoverBroadcastReceiver (BluetoothActivity bluetoothActivity, BluetoothController bluetoothController) {
        super(bluetoothActivity);
        this.bluetoothController = bluetoothController;
        this.bluetoothDevices = bluetoothController.getBluetoothDevices();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            Log.d(TAG, "onReceive: ACTION FOUND");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //TODO: clean that up
            if (!bluetoothDevices.getDevices().contains(device)) {
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                bluetoothDevices.addDevice(device);
                bluetoothController.updateUI();
            }
            else {
                Log.d(TAG, "onReceive: duplicate device " + device.getName() + ": " + device.getAddress());
                int index = bluetoothDevices.getDevices().indexOf(device);
                BluetoothDevice existing = bluetoothDevices.getDevice(index);
                // TODO implement correctly
                if (existing.getName() == null || existing.getName().equals("")) {
                    //bluetoothActivity.newDevices.remove(index);
                    bluetoothDevices.removeDevice(device);
                    bluetoothDevices.addDevice(device);
                    Log.d(TAG, "onReceive: updated device " + device.getName() + ": " + device.getAddress());
                    bluetoothController.updateUI();
                }
            }


        }
    }
}
