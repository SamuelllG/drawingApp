package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Arrays;

public class MessageHandler extends Handler {
    private static final String TAG = "cust.MessageHandler";

    private enum InputType { REQUEST, RESPONSE, DATA, NONE };
    private BluetoothConnectionService bluetoothConnectionService;
    private Communicator communicator;
    private boolean notifiedData;
    private String dataType;

    public MessageHandler (BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
        communicator = new Communicator(bluetoothConnectionService);
    }

    @Override
    public void handleMessage(Message inputMessage) {
        switch (inputMessage.what) {
            case BluetoothConstants.MESSAGE_READ:
                readMessage((byte[]) inputMessage.obj);
                break;
            case BluetoothConstants.MESSAGE_WRITE:
                // not implemented
                break;
            case BluetoothConstants.MESSAGE_TOAST:
                // not implemented
                break;
            default:
                Log.d(TAG, "inputMessage type not found");
        }
    }

    private synchronized void readData(byte[] bytes) {
        String received = new String(bytes);
        switch (dataType) {
            case BluetoothConstants.NOTIFY_LINEWIDTH:
                RemoteHandler.getRemoteHandler().setRemoteLineWidth(received);
                break;
            case BluetoothConstants.NOTIFY_EVENT:
                RemoteHandler.getRemoteHandler().receivedRemoteEvent(bytes);
                break;
            case BluetoothConstants.NOTIFY_CLEAR:
                RemoteHandler.getRemoteHandler().clearRemote();
                break;
            case BluetoothConstants.NOTIFY_MAPDATA:
                RemoteHandler.getRemoteHandler().readRemotePaths(bytes);
                break;
            default:
                Log.d(TAG, "ERROR: received unidentifiable data: " + received);
        }
    }

    private synchronized void readMessage(byte[] bytes) {
        if (notifiedData) {
            readData(bytes);
            notifiedData = false;
            return;
        }
        String received = new String(bytes);
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
                notifiedData = true;
                dataType = received;
                break;
            default:
                Log.d(TAG, "ERROR: received unidentifiable message: " + received);
        }
    }

    private synchronized InputType checkInputType (String received) {
        if (Arrays.asList(BluetoothConstants.REQUESTS).contains(received))
            return InputType.REQUEST;
        else if (Arrays.asList(BluetoothConstants.RESPONSES).contains(received))
            return InputType.RESPONSE;
        else if (Arrays.asList(BluetoothConstants.DATA).contains(received))
            return InputType.DATA;
        return InputType.NONE;
    }
}
