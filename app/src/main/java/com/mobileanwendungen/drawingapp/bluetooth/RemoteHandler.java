package com.mobileanwendungen.drawingapp.bluetooth;

import android.util.Log;
import android.view.MotionEvent;

import com.mobileanwendungen.drawingapp.CustomMotionEvent;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;
import com.mobileanwendungen.drawingapp.view.DrawingView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

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

    public void process (MotionEvent motionEvent) {
        CustomMotionEvent customMotionEvent = new CustomMotionEvent();
        customMotionEvent.setAction(motionEvent.getActionMasked());
        int actionIndex = motionEvent.getActionIndex();
        customMotionEvent.setActionIndex(actionIndex);
        customMotionEvent.setActionPointerId(motionEvent.getPointerId(actionIndex));
        customMotionEvent.setPointerId(motionEvent.getPointerId(0));
        customMotionEvent.setX(motionEvent.getX(actionIndex));
        customMotionEvent.setY(motionEvent.getY(actionIndex));
        customMotionEvent.setNewX(motionEvent.getX(0));
        customMotionEvent.setNewY(motionEvent.getY(0));

        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION)
            write(customMotionEvent);
    }

    public void write (CustomMotionEvent motionEvent) {
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(motionEvent);
            out.flush();
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        Log.d(TAG, "write: notify data");
        byte[] b = BluetoothConstants.NOTIFY_DATA.getBytes();
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_DATA.getBytes());
        Log.d(TAG, "write: send data");
        bluetoothConnectionService.write(data);
    }

    public void receivedData(byte[] buffer, int numBytes) {
        CustomMotionEvent motionEvent = null;
        // cut null data
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < numBytes; i++)
            bytes[i] = buffer[i];

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            motionEvent = (CustomMotionEvent) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        if (motionEvent != null)
            drawingView.onRemoteTouchEvent(motionEvent);
        else
            Log.d(TAG, "ERROR: read object == null");
    }

}
