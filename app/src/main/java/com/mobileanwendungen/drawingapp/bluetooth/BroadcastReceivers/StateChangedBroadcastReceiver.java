package com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

public class StateChangedBroadcastReceiver extends BluetoothBroadcastReceiver {
    private static final String TAG = "cust.StateChangedBR";

    private BluetoothController bluetoothController;

    public StateChangedBroadcastReceiver (BluetoothActivity bluetoothActivity) {
        super(bluetoothActivity);
        this.bluetoothController = BluetoothController.getBluetoothController();
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
                    bluetoothController.cleanup();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "onReceive: STATE TURNING OFF");
                    if (bluetoothController.getBluetoothWasDisabled())
                        // bluetooth was turned off via the app
                        bluetoothController.stopConnection();
                    else if (bluetoothController.getBluetoothConnectionService() != null)
                        // bluetooth was turned off surprisingly
                        bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_FORCE_CLOSE);
                    //else if (bluetoothController.getBluetoothConnectionService() == null)
                        // bluetooth was turned off outside of the activity
                        //Log.d(TAG, "do nothing");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "onReceive: STATE ON");
                    Toast.makeText(bluetoothActivity, context.getText(R.string.BT_STATE_ON), Toast.LENGTH_SHORT).show();
                    if (bluetoothController.getBluetoothConnectionService() != null) {
                        // was an error --> shut down old connection service
                        Log.d(TAG, "onReceive: error detected");
                        bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_CLOSE_REQUEST);
                    }
                    // startListening listening
                    bluetoothController.onBluetoothOn();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "onReceive: STATE TURNING ON");
                    break;
                case BluetoothAdapter.ERROR:
                    Log.d(TAG, "onReceive: ERROR with bluetooth adapter");
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_INIT_RESTART);
                    break;
            }
        }
    }
}
