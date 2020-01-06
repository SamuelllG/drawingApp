package com.mobileanwendungen.drawingapp.threads;

import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;

import java.util.concurrent.TimeUnit;

public class TimeoutThread extends Thread {
    private static final String TAG = "cust.TimeoutThread";

    private BluetoothConnectionService bluetoothConnectionService;

    public TimeoutThread (BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    @Override
    public void run() {
        Log.d(TAG, "waiting for response");
        int sleepMillis = 0;
        while(!bluetoothConnectionService.getReceivedResponse() && sleepMillis < BluetoothConstants.TIMEOUT) {
            try {
                sleepMillis += 50;
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!bluetoothConnectionService.getReceivedResponse() && bluetoothConnectionService.getConnectedThread() != null) {
            // no response
            Log.d(TAG, "no response on request");
            bluetoothConnectionService.setState(BluetoothConstants.STATE_TIMEOUT);
        }
        Log.d(TAG, "timeout thread finished");
        // else --> do nothing, normal procedure continues
        //Log.d(TAG, "got response");
    }
}
