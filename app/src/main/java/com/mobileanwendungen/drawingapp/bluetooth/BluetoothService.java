package com.mobileanwendungen.drawingapp.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.mobileanwendungen.drawingapp.bluetooth.Threads.AcceptThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectedThread;

public class BluetoothService {
    private static final String TAG = "cust.BluetoothService";
    private Handler handler; // handler that gets info from Bluetooth service

    public BluetoothService () {
        handler = new MyHandler();
    }

    public Handler getHandler() {
        return handler;
    }
}
