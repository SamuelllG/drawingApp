package com.mobileanwendungen.drawingapp.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.MotionEvent;

import com.mobileanwendungen.drawingapp.utilities.ParcelableUtil;

public class RemoteHandler {
    private static final String TAG = "cust.RemoteHandler";

    private BluetoothConnectionService bluetoothConnectionService;

    public RemoteHandler() {
        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
    }

    public void write (MotionEvent motionEvent) {
        byte[] data = ParcelableUtil.marshall(motionEvent);
        //bluetoothConnectionService.write  data ankündigen?
        bluetoothConnectionService.write(data);
    }

}
