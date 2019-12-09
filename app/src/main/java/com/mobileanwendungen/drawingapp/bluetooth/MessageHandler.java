package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;

import java.util.Arrays;

public class MessageHandler extends Handler {
    private static final String TAG = "cust.MessageHandler";

    private enum InputType { REQUEST, RESPONSE, DATA };
    private BluetoothConnectionService bluetoothConnectionService;
    private Communicator communicator;

    public MessageHandler (BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
        communicator = new Communicator(bluetoothConnectionService);
    }

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
                communicator.processRequest(received);
                break;
            case RESPONSE:
                Log.d(TAG, "got a response");
                bluetoothConnectionService.setReceivedResponse(true);
                communicator.processResponse(received);
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
}
