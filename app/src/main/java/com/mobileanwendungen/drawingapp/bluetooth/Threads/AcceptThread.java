package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothService;

import java.io.IOException;
import java.util.UUID;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_LISTEN;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_NONE;

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
            Log.e(TAG, "AcceptThread: socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
        BluetoothConnectionService.mState = STATE_LISTEN;
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (BluetoothConnectionService.mState != STATE_CONNECTED) {
            try {
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: socket accepted");
            } catch (IOException e) {
                Log.e(TAG, "run: socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                synchronized (bluetoothController.getBluetoothConnectionService()) {
                    switch (BluetoothConnectionService.mState) {
                        case STATE_LISTEN:
                            // Situation normal. Start the connected thread.
                            bluetoothController.getBluetoothConnectionService().startCommunication(socket, true);
                            break;
                        case STATE_CONNECTING:
                        case STATE_CONNECTED:
                            Log.d(TAG, "run: already in CONNECTING state");
                            bluetoothController.getBluetoothConnectionService().saveDuplicateConnection(socket);
                            //cancel();
                            break;
                        /*case STATE_CONNECTED:
                            Log.d(TAG, "run: already connected --> cancel this");
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            cancel();
                            break;*/
                        case STATE_NONE:
                            Log.d(TAG, "run: oups something went wrong --> cancel");
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            cancel();
                            break;
                    }
                }
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
            }
        }
        Log.i(TAG, "END acceptThread");
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: could not close the connect socket", e);
        }
    }

}
