package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectedThread extends Thread {
    private static final String TAG = "cust.ConnectedThread";

    private final BluetoothController bluetoothController;
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;
    private final InputStream mmInStream;
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

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: begin connected thread");
        byte[] buffer = new byte[4096];
        int iCount = 0;

        while (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTED ||
                bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_VERIFICATION ||
                bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION) {
            try {
                if (bluetoothController.getBluetoothConnectionService().getConnectionState() == BluetoothConstants.STATE_CONNECTED)
                    bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_VERIFICATION);
                int num = mmInStream.read(buffer, iCount, 1);
                if (num != 1)
                    Log.d(TAG, "ERROR: EMPTY READ");

                if (buffer.length-1 == iCount) {
                    buffer = resizeArray(buffer);
                }

                // check for separators
                if (iCount < 6) {
                    iCount++;
                    continue;
                }
                byte[] check = Arrays.copyOfRange(buffer, iCount-5, iCount+1);
                if (Arrays.equals(check, BluetoothConstants.SEPARATOR)) {
                    byte[] message = Arrays.copyOfRange(buffer, 0, iCount-5);
                    buffer = new byte[16384];
                    iCount = 0;
                    // send message
                    Message readMsg = handler.obtainMessage(BluetoothConstants.MESSAGE_READ, message);
                    readMsg.sendToTarget();
                    continue;
                }
                iCount++;
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

    private byte[] resizeArray(byte[] old) {
        byte[] newArray = new byte[old.length*2];
        System.arraycopy(old, 0, newArray, 0, old.length);
        return newArray;
    }

    public void write(byte[] buffer) {
        // append separator at end of data to prevent that two writes are read as one read
        byte[] bytes = new byte[buffer.length + 6];
        System.arraycopy(buffer, 0, bytes, 0, buffer.length);
        System.arraycopy(BluetoothConstants.SEPARATOR, 0, bytes, bytes.length-6, 6);

        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, "sendEvent: exception during sendEvent");
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            Log.d(TAG, "cancel: close socket and streams");
            mmSocket.close();
            mmInStream.close();
            mmOutStream.close();
        } catch (IOException e) {
            Log.d(TAG, "cancel: could not close", e);
        }
    }

    public BluetoothDevice getRemoteDevice() {
        return mmSocket.getRemoteDevice();
    }
}
