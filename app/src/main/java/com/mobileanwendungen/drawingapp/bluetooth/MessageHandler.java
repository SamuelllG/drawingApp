package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;

import java.util.Arrays;

public class MessageHandler extends Handler {
    private static final String TAG = "cust.MessageHandler";

    private enum InputType { REQUEST, RESPONSE, DATA, NONE };
    private BluetoothConnectionService bluetoothConnectionService;
    private Communicator communicator;
    private boolean notifiedData;

    public MessageHandler (BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
        communicator = new Communicator(bluetoothConnectionService);
    }

    @Override
    public void handleMessage(Message inputMessage) {
        switch (inputMessage.what) {
            case BluetoothConstants.MESSAGE_READ:
                readMessage((String) inputMessage.obj);
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

    private synchronized void readMessage(String received) {
        if (notifiedData) {
            Log.d(TAG, "received data");
            notifiedData = false;
            RemoteHandler.getRemoteHandler().receivedData(received);
            return;
        }
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
                Log.d(TAG, "data notified");
                notifiedData = true;
                break;
            default:
                Log.d(TAG, "ERROR: received unidentifiable data: " + received);
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
        else if (received.equals(BluetoothConstants.NOTIFY_DATA))
            return InputType.DATA;
        return InputType.NONE;
    }
}
