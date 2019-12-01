package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CLOSING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_VERIFICATION;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_VERIFIED_CONNECTION;

public class ConnectedThread extends Thread {
    private static final String TAG = "cust.ConnectedThread";

    private final BluetoothController bluetoothController;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler handler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        Log.d(TAG, "new ConnectedThread");
        bluetoothController = BluetoothController.getBluetoothController();
        this.handler = handler;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
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
        byte[] buffer = new byte[1024];
        int numBytes; // bytes returned from read()

        try {
            // Keep listening to the InputStream until an exception occurs.
            while (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_CONNECTED || bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_VERIFICATION || bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_VERIFIED_CONNECTION) {
                try {
                    // Read from the InputStream.
                    if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_CONNECTED)
                        bluetoothController.getBluetoothConnectionService().setState(STATE_VERIFICATION);
                    Log.d(TAG, "READING NOW");
                    numBytes = mmInStream.read(buffer);
                    Log.d(TAG, "STOP READING");

                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(BluetoothConstants.MESSAGE_READ, numBytes, -1, buffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_CLOSING) {
                        // everything normal, thread is closed, the loop already finished
                        bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
                    } else {
                        Log.d(TAG, "run: input stream was disconnected or closed");
                        // thread was closed without a request to the other device, or an exception occurred
                        bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
                        bluetoothController.getBluetoothConnectionService().setState(BluetoothConstants.STATE_INTERRUPTED);
                        // break is done indirectly through setState
                        //e.printStackTrace();
                        //bluetoothController.getBluetoothConnectionService().connectionLost();
                        //break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BluetoothDevice getRemoteDevice() {
        return mmSocket.getRemoteDevice();
    }


    // TODO: was tun, wenn kein close response
/*
    private void request(String request) {
        bluetoothController.getBluetoothConnectionService().write(request.getBytes());
    }*/

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);

            // Share the sent message with the UI activity.
            //Message writtenMsg = handler.obtainMessage(
                    //BluetoothConstants.MESSAGE_WRITE, -1, -1, buffer);
            //writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.d(TAG, "write: exception during write");
            e.printStackTrace();
            // Send a failure message back to the activity.
            //Message writeErrorMsg =
                    //handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST);
            //Bundle bundle = new Bundle();
            //bundle.putString("toast",
                    //"couldn't send data to the other device");
            //writeErrorMsg.setData(bundle);
            //handler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            Log.d(TAG, "cancel: close socket and streams");
            mmSocket.close();
            mmInStream.close();
            mmOutStream.close();
            if (bluetoothController.getBluetoothConnectionService().getConnectionState() == STATE_CLOSING) {
                // thread is closed, the loop already finished, because a close request was send from this device
                bluetoothController.getBluetoothConnectionService().onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
            }
        } catch (IOException e) {
            Log.d(TAG, "cancel: could not close the connect socket", e);
        }
    }
}
