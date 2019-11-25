package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

public class StateChangedBroadcastReceiver extends BluetoothBroadcastReceiver {
    public static final String TAG = "cust.StateChangedBR";

    public StateChangedBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        super(bluetoothActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "onReceive: STATE OFF");
                    Toast.makeText(bluetoothActivity, context.getText(R.string.BT_STATE_OFF), Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "onReceive: STATE TURNING OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "onReceive: STATE ON");
                    Toast.makeText(bluetoothActivity, context.getText(R.string.BT_STATE_ON), Toast.LENGTH_SHORT).show();
                    // startListening listening
                    BluetoothController.getBluetoothController().getBluetoothConnectionService().startListening();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "onReceive: STATE TURNING ON");
                    break;
            }
        }
    }
}
