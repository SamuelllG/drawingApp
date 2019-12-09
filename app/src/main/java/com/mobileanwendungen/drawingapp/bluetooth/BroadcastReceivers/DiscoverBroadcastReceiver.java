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

    public static final String TAG = "cust.DiscoverableBR";
    /*private BluetoothActivity bluetoothActivity;*/

    public DiscoverBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        super(bluetoothActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            Log.d(TAG, "onReceive: ACTION FOUND");

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BluetoothDevices bluetoothDevices = BluetoothController.getBluetoothController().getBluetoothDevices();
            //TODO: clean that up
            if (!bluetoothDevices.getDevices().contains(device)) {
                bluetoothDevices.addDevice(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                bluetoothDevices.addDevice(device);
                BluetoothController.getBluetoothController().updateUI();
            }
            else {
                Log.d(TAG, "onReceive: duplicate device " + device.getName() + ": " + device.getAddress());
                int index = bluetoothDevices.getDevices().indexOf(device);
                BluetoothDevice existing = bluetoothDevices.getDevice(index);
                // TODO implement correctly
                if (existing.getName() == null) {
                    //bluetoothActivity.newDevices.remove(index);
                    bluetoothDevices.removeDevice(device);
                    bluetoothDevices.addDevice(device);
                    Log.d(TAG, "onReceive: updated device " + device.getName() + ": " + device.getAddress());
                    BluetoothController.getBluetoothController().updateUI();
                }
            }


        }
    }
}
