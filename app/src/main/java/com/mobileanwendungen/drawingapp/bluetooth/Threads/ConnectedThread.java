package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_DISCONNECTING;
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
        BluetoothConnectionService.mState = STATE_CONNECTED;
        bluetoothController.onState(STATE_CONNECTED, mmSocket.getRemoteDevice());
    }

    @Override
    public void run() {
        Log.d(TAG, "run: begin connected thread");
        byte[] buffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (BluetoothConnectionService.mState == STATE_CONNECTED) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(buffer);
                checkForRequest(buffer, numBytes);
                // Send the obtained bytes to the UI activity.
                //Message readMsg = handler.obtainMessage(BluetoothConstants.MESSAGE_READ, numBytes, -1, buffer);
                //readMsg.sendToTarget();
            } catch (IOException e) {
                if (BluetoothConnectionService.mState == STATE_DISCONNECTING) {
                    // all good, do nothing (unfortunately exception is thrown because read is a blocking call
                    Log.d(TAG, "run: input stream was closed, stop reading");
                    break;
                } else {
                    Log.d(TAG, "run: input stream was disconnected", e);
                    //e.printStackTrace();
                    //bluetoothController.getBluetoothConnectionService().connectionLost();
                    break;
                }
            }
        }
    }

    private boolean checkForRequest (byte[] buffer, int numBytes) {
        String received = new String(Arrays.copyOfRange(buffer, 0, numBytes));
        if (BluetoothConnectionService.connectRequestReceived) {
            // since this is the number thread is waiting for, reset connectRequestReceived
            BluetoothConnectionService.connectRequestReceived = false;
            // next message is a number
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            int remoteRoll = bb.getInt(0);
            Log.d(TAG, "checkForRequest: requestConnectReceived active");
            Log.d(TAG, "checkForRequest: own: " + BluetoothConnectionService.roll + " remote: " + remoteRoll);
            if (BluetoothConnectionService.roll > remoteRoll) {
                // won the "game"
                // cancel this thread
                cancel();
                bluetoothController.getBluetoothConnectionService().resetConnectedThread(this);
            } else {
                // lost --> use this as main connectedThread
                bluetoothController.getBluetoothConnectionService().useConnection(this);
            }
            return true;
        }

        switch (received) {
            case BluetoothConstants.REQUEST_CLOSE_CONNECTION:
                Log.d(TAG, "checkForRequest: close connection was requested");
                Log.d(TAG, "checkForRequest: immediately confirm close connection");
                write(BluetoothConstants.CONFIRMED_CLOSE_CONNECTION.getBytes());
                return true;
            case BluetoothConstants.CONFIRMED_CLOSE_CONNECTION:
                Log.d(TAG, "checkForRequest: close connection was confirmed");
                write(BluetoothConstants.CLOSE_CONNECTION.getBytes());
                bluetoothController.stopConnection(true);
                return true;
            case BluetoothConstants.CLOSE_CONNECTION:
                Log.d(TAG, "checkForRequest: close connection was confirmed");
                bluetoothController.stopConnection(true);
                return true;
            case BluetoothConstants.REQUEST_CONNECT:
                Log.d(TAG, "checkForRequest: connect request received");
                BluetoothConnectionService.connectRequestReceived = true;
                Log.d(TAG, "checkForRequest: confirm connect request");
                write(BluetoothConstants.CONFIRMED_CONNECT_REQUEST.getBytes());
                // roll yourself
                Random random = new Random();
                BluetoothConnectionService.roll = random.nextInt();
                Log.d(TAG, "checkForRequest: roll: " + BluetoothConnectionService.roll);
                byte[] bytes = ByteBuffer.allocate(4).putInt(BluetoothConnectionService.roll).array();
                write(bytes);
                break;
            case BluetoothConstants.CONFIRMED_CONNECT_REQUEST:
                Log.d(TAG, "checkForRequest: connect request was confirmed");
                BluetoothConnectionService.connectRequestReceived = true;
                random = new Random();
                BluetoothConnectionService.roll = random.nextInt();
                Log.d(TAG, "checkForRequest: roll: " + BluetoothConnectionService.roll);
                bytes = ByteBuffer.allocate(4).putInt(BluetoothConnectionService.roll).array();
                write(bytes);
                break;
                //TODO reset roll after successful connection
            case BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION:
                Log.d(TAG, "checkForRequest: established connection was requested");
                Log.d(TAG, "checkForRequest: immediately confirm established connection");
                write(BluetoothConstants.CONFIRMED_ESTABLISHED_CONNECTION.getBytes());
                BluetoothConnectionService.mState = STATE_VERIFIED_CONNECTION;
                break;
            case BluetoothConstants.CONFIRMED_ESTABLISHED_CONNECTION:
                Log.d(TAG, "checkForRequest: established connection was confirmed");
                BluetoothConnectionService.mState = STATE_VERIFIED_CONNECTION;
                break;
            default:
                return false;

        }
        return false;
    } // TODO: request closed --> wenn keine antwort closen
    // TODO: request() method
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
            Log.e(TAG, "write: exception during write", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            Log.d(TAG, "cancel: close socket and streams");
            BluetoothConnectionService.mState = BluetoothConstants.STATE_DISCONNECTING;
            mmSocket.close();
            mmInStream.close();
            mmOutStream.close();
            BluetoothConnectionService.mState = BluetoothConstants.STATE_NONE;
        } catch (IOException e) {
            Log.e(TAG, "cancel: could not close the connect socket", e);
        }
    }
}
