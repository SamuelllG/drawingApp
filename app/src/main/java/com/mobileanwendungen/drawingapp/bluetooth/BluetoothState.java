package com.mobileanwendungen.drawingapp.bluetooth;

import android.bluetooth.BluetoothAdapter;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.BluetoothBroadcastReceiver;

public class BluetoothState {

    private static final String TAG = "cust.BTState";
    private static BluetoothState BLUETOOTH_STATE;

    private BluetoothState() {
        //
    }

    /**
     * Singleton
     */
    public static BluetoothState getBluetoothState() {
        if (BLUETOOTH_STATE == null) {
            BLUETOOTH_STATE = new BluetoothState();
        }
        return BLUETOOTH_STATE;
    }

    private int state;
}
