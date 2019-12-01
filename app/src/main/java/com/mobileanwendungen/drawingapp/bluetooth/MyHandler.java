package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;

import java.util.Arrays;

import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CLOSING;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_VERIFIED_CONNECTION;

// TODO: make UI handler

public class MyHandler extends Handler {
    private static final String TAG = "cust.MyHandler";

    private enum InputType { REQUEST, RESPONSE, DATA };

    @Override
    public void handleMessage(Message inputMessage) {
        switch (inputMessage.what) {
            case BluetoothConstants.MESSAGE_READ:
                readMessage((byte[]) inputMessage.obj, inputMessage.arg1);
                break;
            case BluetoothConstants.MESSAGE_WRITE:
                // not implemented
                break;
            case BluetoothConstants.MESSAGE_TOAST:
                // not implemented
                //String toast = inputMessage.getData().getString("toast");
                //Log.d(TAG, "handleMessage: error + " + toast);
                //Toast.makeText()
                break;
            default:
                Log.d(TAG, "inputMessage type not found");
        }

    }

    private synchronized void readMessage(byte[] buffer, int numBytes) {
        String received = getReceivedString(buffer, numBytes);
        InputType type = checkInputType(received);
        switch (type) {
            case REQUEST:
                Log.d(TAG, "got a request");
                processRequest(received);
                break;
            case RESPONSE:
                Log.d(TAG, "got a response");
                BluetoothController.getBluetoothController().getBluetoothConnectionService().setReceivedResponse(true);
                processResponse(received);
                break;
            case DATA:
                Log.d(TAG, "got data");
                break;
            default:
                Log.d(TAG, "ERROR: received unidentifiable data");
        }
    }

    private synchronized String getReceivedString (byte[] buffer, int numBytes) {
        return new String(Arrays.copyOfRange(buffer, 0, numBytes));
    }

    private synchronized InputType checkInputType (String received) {
        if (Arrays.asList(BluetoothConstants.REQUESTS).contains(received))
            return InputType.REQUEST;
        else if (Arrays.asList(BluetoothConstants.RESPONSES).contains(received))
            return InputType.RESPONSE;
        else
            return InputType.DATA;
    }

    private synchronized void processRequest (String request) {
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
                BluetoothController.getBluetoothController().getBluetoothConnectionService().write(BluetoothConstants.CONFIRM_ESTABLISHED_CONNECTION.getBytes());
                break;
            case BluetoothConstants.REQUEST_CLOSE_CONNECTION:
                Log.d(TAG, "processRequest: close connection was requested");
                Log.d(TAG, "processRequest: immediately confirm close connection");
                BluetoothController.getBluetoothController().getBluetoothConnectionService().write(BluetoothConstants.CONFIRM_CLOSE_CONNECTION.getBytes());
                BluetoothController.getBluetoothController().getBluetoothConnectionService().setState(STATE_CLOSING);
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

    private synchronized void processResponse (String response) {
        switch (response) {
            case BluetoothConstants.CONFIRM_ESTABLISHED_CONNECTION:
                Log.d(TAG, "response: established connection was confirmed");
                BluetoothController.getBluetoothController().getBluetoothConnectionService().setState(STATE_VERIFIED_CONNECTION);
                BluetoothController.getBluetoothController().onState(STATE_CONNECTED);
                break;
            case BluetoothConstants.CONFIRM_CLOSE_CONNECTION:
                Log.d(TAG, "response: close connection was confirmed");
                BluetoothController.getBluetoothController().getBluetoothConnectionService().setState(STATE_CLOSING);
                break;
            default:
                Log.d(TAG, "ERROR: you forgot to add the response to the handler ;)");
        }
    }

}
