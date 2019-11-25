package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;

public class ScanModeChangedBroadcastReceiver extends BluetoothBroadcastReceiver {

    public static final String TAG = "cust.ScanModeChangedBR";

    public ScanModeChangedBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        super(bluetoothActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

            switch (mode) {
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                    Log.d(TAG, "onReceive: Discoverability enabled");
                    Toast.makeText(bluetoothActivity, context.getText(R.string.BT_DISCOVERABILITY_ON), Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                    Log.d(TAG, "onReceive: Discoverability is disabled, connectable for bonded devices");
                    break;
                case BluetoothAdapter.SCAN_MODE_NONE:
                    Log.d(TAG, "onReceive:  Discoverability disabled, not receiving connections");
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    Log.d(TAG, "onReceive: Connecting....");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.d(TAG, "onReceive: Connected");
                    break;
            }
        }
    }
}
