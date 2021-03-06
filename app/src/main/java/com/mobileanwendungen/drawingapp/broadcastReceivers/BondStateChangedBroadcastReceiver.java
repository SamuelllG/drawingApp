package com.mobileanwendungen.drawingapp.broadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.views.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.controllers.BluetoothController;

public class BondStateChangedBroadcastReceiver extends BluetoothBroadcastReceiver {

    private static final String TAG = "cust.BondStateChangedBR";

    public BondStateChangedBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        super(bluetoothActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // bonded
            switch(device.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    Log.d(TAG, "onReceive: BOND_BONDED");
                    Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.pairing_successful), Toast.LENGTH_SHORT).show();
                    BluetoothController.getBluetoothController().getBluetoothDevices().addBonded(device);
                    BluetoothController.getBluetoothController().updateUI();
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Log.d(TAG, "onReceive: BOND_BONDING");
                    break;
                case BluetoothDevice.BOND_NONE:
                    Log.d(TAG, "onReceive: BOND_NONE");
                    break;
                default:
                    Log.d(TAG, "onReceive: no such state");
            }
        }
    }
}
