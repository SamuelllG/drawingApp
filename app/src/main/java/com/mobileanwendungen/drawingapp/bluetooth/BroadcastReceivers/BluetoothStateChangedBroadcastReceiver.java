package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothDevices;
import com.mobileanwendungen.drawingapp.bluetooth.DeviceListAdapter;

public class BluetoothStateChangedBroadcastReceiver extends BluetoothBroadcastReceiver {
    private static final String TAG = "cust.BTStateBR";

    private BluetoothConnectionService bluetoothConnectionService;

    public BluetoothStateChangedBroadcastReceiver(BluetoothActivity bluetoothActivity, BluetoothConnectionService bluetoothConnectionService) {
        super(bluetoothActivity);
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /*final String action = intent.getAction();

        if(action.equals(BluetoothConstants.BLUETOOTH_STATE_CHANGED)) {
            Integer connectionState = intent.getParcelableExtra(BluetoothConstants.BlUETOOTH_STATE);
            BluetoothDevices bluetoothDevices = intent.getParcelableExtra(BluetoothConstants.BLUETOOTH_DEVICES);

            switch (connectionState) {
                case BluetoothConstants.STATE_NONE:
                    bluetoothConnectionService.setState(connectionState);
                    break;
                case BluetoothConstants.STATE_LISTEN:
                    bluetoothConnectionService.setState(connectionState);
                    break;
                case BluetoothConstants.STATE_PAIRED:
                    bluetoothConnectionService.setState(connectionState);
                    break;
                case BluetoothConstants.STATE_CONNECTING:
                    bluetoothConnectionService.setState(connectionState);
                    break;
                case BluetoothConstants.STATE_CONNECTED:
                    bluetoothConnectionService.setState(connectionState);bluetoothActivity.runOnUiThread(() -> {
                    bluetoothDevices.setConnected();
                    bluetoothActivity.deviceListAdapter = new DeviceListAdapter(bluetoothActivity, R.layout.device_adapter_view, bluetoothDevices);
                    bluetoothActivity.listViewDevices.setAdapter(bluetoothActivity.deviceListAdapter);
                });
                    break;
            }
        }*/
    }
}
