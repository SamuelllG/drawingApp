package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MyHandler extends Handler {
    public static final String TAG = "cust.MyHandler";

    @Override
    public void handleMessage(Message inputMessage) {
        switch (inputMessage.what) {
            case BluetoothConstants.MESSAGE_READ:
                Log.d(TAG, "handleMessage: read, message: " + ((byte[])inputMessage.obj).toString());
                ByteBuffer bb = ByteBuffer.wrap((byte[])inputMessage.obj);
                int d = bb.getInt(0);
                Log.d(TAG, "handleMessage: read, message: " + d);
                break;
            case BluetoothConstants.MESSAGE_WRITE:
                Log.d(TAG, "handleMessage: wrote message");
                break;
            case BluetoothConstants.MESSAGE_TOAST:
                String toast = inputMessage.getData().getString("toast");
                Log.d(TAG, "handleMessage: error + " + toast);
                //Toast.makeText()
                break;
        }

    }

}
