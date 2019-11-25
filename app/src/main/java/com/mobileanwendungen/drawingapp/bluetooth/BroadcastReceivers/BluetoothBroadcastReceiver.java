package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.content.BroadcastReceiver;

import com.mobileanwendungen.drawingapp.BluetoothActivity;

public abstract class BluetoothBroadcastReceiver extends BroadcastReceiver {

    protected BluetoothActivity bluetoothActivity;

    public BluetoothBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        this.bluetoothActivity = bluetoothActivity;
    }
}
