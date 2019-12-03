package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private static final String TAG = "cust.AcceptThread";

    private final BluetoothConnectionService bluetoothConnectionService;
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread(BluetoothAdapter bluetoothAdapter) {
        Log.d(TAG, "new AcceptThread");
        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();

        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothConstants.APP_NAME, UUID.fromString(BluetoothConstants.UUID));
        } catch (IOException e) {
            Log.d(TAG, "AcceptThread: socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothSocket socket;
        try {
            bluetoothConnectionService.setState(BluetoothConstants.STATE_LISTEN);
            socket = mmServerSocket.accept();
            Log.d(TAG, "run: socket accepted");
            if (bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_LISTEN) {
                // only establish connection over acceptThread, if connectThread has not connected yet
                bluetoothConnectionService.setConnectionSocket(socket);
                bluetoothConnectionService.setState(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
            } else {
                Log.d(TAG, "run: acceptThread ignored");
            }
        } catch (IOException e) {
            if (bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_LISTEN) {
                Log.d(TAG, "run: socket's accept() method failed");
                bluetoothConnectionService.setState(BluetoothConstants.STATE_FAILED);
            }
        }
        bluetoothConnectionService.onThreadClosed(BluetoothConstants.ACCEPT_THREAD);
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: could not close the connect socket", e);
        }
    }
}
