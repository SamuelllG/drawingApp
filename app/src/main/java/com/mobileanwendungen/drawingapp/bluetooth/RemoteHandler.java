package com.mobileanwendungen.drawingapp.bluetooth;

import android.util.Log;
import android.view.MotionEvent;

import com.mobileanwendungen.drawingapp.CustomMotionEvent;
import com.mobileanwendungen.drawingapp.DrawingController;
import com.mobileanwendungen.drawingapp.utilities.PathsData;
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

    public void prepareAndSend(MotionEvent motionEvent) {
        if (isConnected())
            sendEvent(makeCustomMotionEvent(motionEvent));
    }

    private void sendEvent(CustomMotionEvent motionEvent) {
        //Log.d(TAG, "sendEvent: notify data");
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_EVENT.getBytes());
        //Log.d(TAG, "sendEvent: sendEvent data");
        bluetoothConnectionService.write(getBytes(motionEvent));
    }

    public void sendMyMap(PathsData pathsData) {
        //Log.d(TAG, "sendEvent: notify data");
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_MAPDATA.getBytes());
        //Log.d(TAG, "sendEvent: sendEvent data");
        bluetoothConnectionService.write(getBytes(pathsData));
    }

    private byte[] getBytes(Object object) {
        byte[] bytes = null;
        try (   ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos); ){
            out.writeObject(object);
            out.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private Object readObject(byte[] bytes) {
        Object object = null;
        try (   ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis); ){
            object = in.readObject();
        } catch (ClassNotFoundException | IOException e) {
                Log.d(TAG, "ERROR");
            e.printStackTrace();
        }
        return object;
    }

    public void receivedRemoteEvent(byte[] bytes) {
        CustomMotionEvent motionEvent = (CustomMotionEvent) readObject(bytes);
        if (motionEvent != null)
            drawingView.onRemoteTouchEvent(motionEvent);
        else
            Log.d(TAG, "ERROR: read object == null");
    }

    public void readRemotePaths(byte[] bytes) {
        PathsData pathsData = (PathsData) readObject(bytes);
        if (pathsData != null)
            DrawingController.getDrawingController().loadRemotePaths(pathsData.getPaths());
        else
            Log.d(TAG, "ERROR: read object == null");
    }

    public void sendMyLineWidth() {
        if (isConnected()) { // be safe
            bluetoothConnectionService.write(BluetoothConstants.NOTIFY_LINEWIDTH.getBytes());
            bluetoothConnectionService.write(String.valueOf(drawingView.getLineWidth(0)).getBytes());
        }
    }

    public void setRemoteLineWidth(String received) {
        int width = Integer.parseInt(received);
        drawingView.setLineWidth(1, width);
    }

    public void notifyClear() {
        if (isConnected()) { // be safe
            bluetoothConnectionService.write(BluetoothConstants.NOTIFY_CLEAR.getBytes());
            bluetoothConnectionService.write(String.valueOf(1).getBytes());
        }
    }

    public void clearRemote() {
        drawingView.clear(1, 0);
    }

    private CustomMotionEvent makeCustomMotionEvent (MotionEvent motionEvent) {
        CustomMotionEvent customMotionEvent = new CustomMotionEvent();
        customMotionEvent.setAction(motionEvent.getActionMasked());
        int actionIndex = motionEvent.getActionIndex();
        customMotionEvent.setX(motionEvent.getX(actionIndex));
        customMotionEvent.setY(motionEvent.getY(actionIndex));
        customMotionEvent.setNewX(motionEvent.getX(0));
        customMotionEvent.setNewY(motionEvent.getY(0));
        return customMotionEvent;
    }

    private boolean isConnected() {
        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        return bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION;
    }
}
