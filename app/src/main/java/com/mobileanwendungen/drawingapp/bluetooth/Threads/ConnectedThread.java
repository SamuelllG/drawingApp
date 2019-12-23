package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private static final String TAG = "cust.ConnectedThread";

    private final BluetoothController bluetoothController;
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;
    private final BufferedReader bufferedReader;
    private final Handler handler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        Log.d(TAG, "new ConnectedThread");
        bluetoothController = BluetoothController.getBluetoothController();
        this.handler = handler;
        mmSocket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "ConnectedThread: error occurred when creating input stream", e);
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "ConnectedThread: error occurred when creating output stream", e);
        }

        bufferedReader = new BufferedReader(new InputStreamReader(tmpIn));
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: begin connected thread");

        while (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTED ||
                bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_VERIFICATION ||
                bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION) {
            try {
                // Read from the InputStream.
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTED)
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_VERIFICATION);
                //Log.d(TAG, "READING NOW");
                String received = bufferedReader.readLine();
                //Log.d(TAG, "STOP READING");

                // Send the obtained bytes to handler to start listening again
                //Message readMsg = handler.obtainMessage(BluetoothConstants.MESSAGE_READ, numBytes, -1, buffer);
                Message readMsg = handler.obtainMessage(BluetoothConstants.MESSAGE_READ, received);
                readMsg.sendToTarget();
            } catch (IOException e) {
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() != BluetoothConstants.STATE_CLOSING &&
                        bluetoothController.getBluetoothConnectionService().getConnectionState() != BluetoothConstants.STATE_INIT_RESTART) {
                    Log.d(TAG, "run: input stream was disconnected or closed");
                    // thread was closed without a request to the other device, or an exception occurred
                    bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_INTERRUPTED);
                    return;
                }
                // else: thread was closed properly
            }
        }
        bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
    }

    public void write(byte[] buffer) {
        // append line feed at end of data to prevent that two writes are read as one read
        byte[] bytes = new byte[buffer.length + 1];
        for (int i = 0; i < buffer.length; i++)
            bytes[i] = buffer[i];
        bytes[buffer.length] = (byte) 10; // is \n

        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, "write: exception during write");
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            Log.d(TAG, "cancel: close socket and streams");
            mmSocket.close();
            bufferedReader.close();
            mmOutStream.close();
        } catch (IOException e) {
            Log.d(TAG, "cancel: could not close", e);
        }
    }

    public BluetoothDevice getRemoteDevice() {
        return mmSocket.getRemoteDevice();
    }
}
