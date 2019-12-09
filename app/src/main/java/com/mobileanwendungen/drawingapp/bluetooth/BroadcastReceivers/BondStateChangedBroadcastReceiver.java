package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothDevices;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.DeviceListAdapter;

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
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "onReceive: BOND_BONDED");
                Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.pairing_successful), Toast.LENGTH_SHORT).show();
                BluetoothController.getBluetoothController().getBluetoothDevices().addBonded(device);
                BluetoothController.getBluetoothController().updateUI();
            }
            // creating a bond
            if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                Log.d(TAG, "onReceive: BOND_BONDING");
            }
            // breaking a bond
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Log.d(TAG, "onReceive: BOND_NONE");
            }
        }
    }
}
