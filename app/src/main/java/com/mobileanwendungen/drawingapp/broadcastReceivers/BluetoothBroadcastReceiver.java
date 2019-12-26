package com.mobileanwendungen.drawingapp.broadcastReceivers;

import android.content.BroadcastReceiver;

import com.mobileanwendungen.drawingapp.views.BluetoothActivity;

public abstract class BluetoothBroadcastReceiver extends BroadcastReceiver {

    protected BluetoothActivity bluetoothActivity;

    public BluetoothBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        this.bluetoothActivity = bluetoothActivity;
    }
}
