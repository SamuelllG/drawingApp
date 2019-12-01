package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.IOException;
import java.util.UUID;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_LISTEN;

// https://developer.android.com/guide/topics/connectivity/bluetooth#java
public class AcceptThread extends Thread {

    private static final String TAG = "cust.AcceptThread";

    private static final String NAME = BluetoothConstants.APP_NAME;
    private static final UUID MY_UUID = UUID.fromString(BluetoothConstants.UUID);
    private final BluetoothController bluetoothController;
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread(BluetoothAdapter bluetoothAdapter) {
        Log.d(TAG, "new AcceptThread");
        this.bluetoothController = BluetoothController.getBluetoothController();
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, UUID.fromString(BluetoothConstants.UUID)); //COMMENT:INSECURE? NO!
        } catch (IOException e) {
            Log.d(TAG, "AcceptThread: socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        try {
            bluetoothController.getBluetoothConnectionService().setState(STATE_LISTEN);
            socket = mmServerSocket.accept();
            Log.d(TAG, "run: socket accepted");
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_LISTEN) {
                // only establish connection over acceptThread, if connectThread has not connected yet
                bluetoothController.getBluetoothConnectionService().setConnectionSocket(socket);
                bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
            } else {
                Log.d(TAG, "run: acceptThread ignored");
            }
        } catch (IOException e) {
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_LISTEN) {
                Log.d(TAG, "run: socket's accept() method failed");
                bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_FAILED);
            } else {
                // thread was closed because connectThread returned socket and acceptThread didn't
                // since accept() is blocking --> exception is thrown
                bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.ACCEPT_THREAD);
            }

        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTING_VIA_SERVER || bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTING) {
                // this thread is being closed because accept() already returned or accept was ignored
                bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.ACCEPT_THREAD);
            }
        } catch (IOException e) {
            Log.e(TAG, "cancel: could not close the connect socket", e);
        }
    }

}
