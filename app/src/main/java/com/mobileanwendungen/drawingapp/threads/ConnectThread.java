package com.mobileanwendungen.drawingapp.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;
import com.mobileanwendungen.drawingapp.controllers.BluetoothController;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static final String TAG = "cust.ConnectThread";

    private final BluetoothController bluetoothController;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter bluetoothAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothController = BluetoothController.getBluetoothController();
        mmDevice = device;
        this.bluetoothAdapter = bluetoothAdapter;

        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BluetoothConstants.UUID));
        } catch (IOException e) {
            Log.d(TAG, "ConnectThread: socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: begin connect thread");
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        if (bluetoothController.getBluetoothConnectionService().getConnectionState() != BluetoothConstants.STATE_CONNECTING_VIA_SERVER) {
            // only use this, if accept thread has not been connected yet
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() != BluetoothConstants.STATE_LISTEN)
                Log.d(TAG, "ERROR: should be listening");
            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_LISTEN) {
                    // only establish connection over connectThread, if accept thread has not connected yet
                    bluetoothController.getBluetoothConnectionService().setConnectionSocket(mmSocket);
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_CONNECTING);
                } else {
                    Log.d(TAG, "run: connectThread ignored");
                }
            } catch (IOException e) {
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_LISTEN) {
                    Log.d(TAG, "run: exception during connecting");
                    // the other device is not available
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_UNABLE_TO_CONNECT);
                }
            }
        } else {
            // accept thread already connecting
            Log.d(TAG, "connectThread not needed");
        }
        // if connection was established over the connectThread, then stays open until connection is closed (because of the socket)
        // or if connection was established via the acceptThread, but connectThread returned a socket as well (but was ignored) (or it didn't)
        bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECT_THREAD);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "cancel: could not close the client socket", e);
        }
    }
}