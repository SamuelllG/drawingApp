package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;
import com.mobileanwendungen.drawingapp.utilities.ParcelableUtil;
import com.mobileanwendungen.drawingapp.view.DrawingView;

public class RemoteHandler {
    private static final String TAG = "cust.RemoteHandler";

    private static RemoteHandler REMOTE_HANDLER;

    private BluetoothConnectionService bluetoothConnectionService;
    private DrawingView drawingView;

    private RemoteHandler() {}

    public static RemoteHandler getRemoteHandler() {
        if (REMOTE_HANDLER == null) {
            REMOTE_HANDLER = new RemoteHandler();
        }
        return REMOTE_HANDLER;
    }

    public void init(DrawingView drawingView) {
        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        this.drawingView = drawingView;
    }

    public void write (MotionEvent motionEvent) {
        byte[] data = ParcelableUtil.marshall(motionEvent);
        Log.d(TAG, "write: notify data");
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_DATA.getBytes());
        Log.d(TAG, "write: send data");
        bluetoothConnectionService.write(data);
    }

    public void receivedData(byte[] buffer, int numBytes) {
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < numBytes; i++)
            bytes[i] = buffer[i];
        MotionEvent event = ParcelableUtil.unmarshall(bytes, MotionEvent.CREATOR);
        drawingView.onRemoteTouchEvent(event);
    }

}
