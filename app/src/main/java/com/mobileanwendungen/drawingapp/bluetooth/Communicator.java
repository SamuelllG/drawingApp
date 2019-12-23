package com.mobileanwendungen.drawingapp.bluetooth;

import android.util.Log;


public class Communicator {
    private static final String TAG = "cust.Communicator";

    private BluetoothConnectionService bluetoothConnectionService;

    public Communicator (BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    public synchronized void processRequest (String request) {
        /*if (BluetoothConnectionService.connectRequestReceived) {
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
        }*/
        switch (request) {
            case BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION:
                Log.d(TAG, "processRequest: established connection was requested");
                Log.d(TAG, "processRequest: immediately confirm established connection");
                bluetoothConnectionService.write(BluetoothConstants.CONFIRM_ESTABLISHED_CONNECTION.getBytes());
                break;
            case BluetoothConstants.REQUEST_CLOSE_CONNECTION:
                Log.d(TAG, "processRequest: close connection was requested");
                Log.d(TAG, "processRequest: immediately confirm close connection");
                bluetoothConnectionService.write(BluetoothConstants.CONFIRM_CLOSE_CONNECTION.getBytes());
                bluetoothConnectionService.setState(BluetoothConstants.STATE_CLOSING);
                break;
            /*case BluetoothConstants.REQUEST_CONNECT:
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
                return true;
            case BluetoothConstants.CONFIRMED_CONNECT_REQUEST:
                Log.d(TAG, "checkForRequest: connect request was confirmed");
                BluetoothConnectionService.connectRequestReceived = true;
                random = new Random();
                BluetoothConnectionService.roll = random.nextInt();
                Log.d(TAG, "checkForRequest: roll: " + BluetoothConnectionService.roll);
                bytes = ByteBuffer.allocate(4).putInt(BluetoothConnectionService.roll).array();
                write(bytes);
                return true;
                //TODO reset roll after successful connection*/
            default:
                Log.d(TAG, "ERROR: you forgot to add the request to the handler ;)");
        }
    }

    public synchronized void processResponse (String response) {
        switch (response) {
            case BluetoothConstants.CONFIRM_ESTABLISHED_CONNECTION:
                Log.d(TAG, "response: established connection was confirmed");
                bluetoothConnectionService.setState(BluetoothConstants.STATE_VERIFIED_CONNECTION);
                break;
            case BluetoothConstants.CONFIRM_CLOSE_CONNECTION:
                Log.d(TAG, "response: close connection was confirmed");
                bluetoothConnectionService.setState(BluetoothConstants.STATE_CLOSING);
                break;
            default:
                Log.d(TAG, "ERROR: you forgot to add the response to the handler ;)");
        }
    }

}
