package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING;

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
            Log.e(TAG, "ConnectThread: socket's create() method failed", e);
        }
        mmSocket = tmp;
        BluetoothConnectionService.mState = STATE_CONNECTING;
    }

    public void run() {
        Log.d(TAG, "run: begin connect thread");
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.

            // READ RETURN -1 bt socket is closed because cancel is being called right after connection is made and socket was returned
            mmSocket.connect();
            Log.d(TAG, "run: ConnectThread connected");
            bluetoothController.getBluetoothConnectionService().startCommunication(mmSocket, false);
        } catch (IOException connectException) {
            Log.d(TAG, "run: exception during connecting");
            bluetoothController.getBluetoothConnectionService().connectionFailed(mmDevice);
            //connectException.printStackTrace();
            return;
        }

        // Reset the ConnectThread because we're done
        /*synchronized (bluetoothController.getBluetoothConnectionService()) {
            connectThread = null;
        }*/
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            // only close if not in use
            if (!mmSocket.isConnected())
                mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: could not close the client socket", e);
        }
    }

}
