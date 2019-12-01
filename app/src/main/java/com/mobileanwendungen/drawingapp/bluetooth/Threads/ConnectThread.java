package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.IOException;
import java.util.UUID;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING_VIA_SERVER;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_LISTEN;

// https://developer.android.com/guide/topics/connectivity/bluetooth#java
public class ConnectThread extends Thread {
    public static final String TAG = "cust.ConnectThread";
    private static final UUID MY_UUID = UUID.fromString(BluetoothConstants.UUID);

    private final BluetoothController bluetoothController;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter bluetoothAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothController = BluetoothController.getBluetoothController();
        mmDevice = device;
        this.bluetoothAdapter = bluetoothAdapter;

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BluetoothConstants.UUID));
        } catch (IOException e) {
            Log.d(TAG, "ConnectThread: socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        Log.d(TAG, "run: begin connect thread");
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        if (bluetoothController.getBluetoothConnectionService().getConnectionState() != STATE_CONNECTING_VIA_SERVER) {
            // only use this, if accept thread has not been connected yet
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() != STATE_LISTEN)
                Log.d(TAG, "ERROR: should be listening");
            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_LISTEN) {
                    // only establish connection over connectThread, if accept thread has not connected yet
                    bluetoothController.getBluetoothConnectionService().setConnectionSocket(mmSocket);
                    bluetoothController.getBluetoothConnectionService().setState(STATE_CONNECTING);
                } else {
                    Log.d(TAG, "run: connectThread ignored");
                }
            } catch (IOException e) {
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_LISTEN) {
                    Log.d(TAG, "run: exception during connecting");
                    // the other device is not available
                    //bluetoothController.getBluetoothConnectionService().connectionFailed(mmDevice);
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_UNABLE_TO_CONNECT);
                }/* else {
                    // thread was closed, since .connect() is blocking --> exception is thrown
                    bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECT_THREAD);
                }*/
            }
        } else {
            // accept thread already connecting
            Log.d(TAG, "connectThread not needed");
        }

        //if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CLOSING || bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTING_VIA_SERVER)
            // if connection was established over the connectThread, then stays open until connection is closed (because of the socket)
            // or if connection was established via the acceptThread, but connectThread returned a socket as well (but was ignored)
            // or if """"" and connectThread didn't return a socket
            bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECT_THREAD);

        // Reset the ConnectThread because we're done
        /*synchronized (bluetoothController.getBluetoothConnectionService()) {
            connectThread = null;
        }*/
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();

        } catch (IOException e) {
            Log.d(TAG, "cancel: could not close the client socket", e);
        }
    }

}
