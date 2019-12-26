package com.mobileanwendungen.drawingapp.bluetooth;

import android.util.Log;
import android.view.MotionEvent;

import com.mobileanwendungen.drawingapp.CustomMotionEvent;
import com.mobileanwendungen.drawingapp.DrawingController;
import com.mobileanwendungen.drawingapp.utilities.MapWrapper;
import com.mobileanwendungen.drawingapp.view.DrawingView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Base64;

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
            sendEvent(customMotionEvent);
    }

    public void sendEvent(CustomMotionEvent motionEvent) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(motionEvent);
            out.flush();
            bytes = bos.toByteArray();
            //bytes = Base64.getEncoder().encodeToString(bytes).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        //Log.d(TAG, "sendEvent: notify data");
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_EVENT.getBytes());
        //Log.d(TAG, "sendEvent: sendEvent data");
        bluetoothConnectionService.write(bytes);
    }

    public void sendMyMap(MapWrapper mapWrapper) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(mapWrapper);
            out.flush();
            bytes = bos.toByteArray();
            //bytes = Base64.getEncoder().encodeToString(bytes).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        //Log.d(TAG, "sendEvent: notify data");
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_MAPDATA.getBytes());
        //Log.d(TAG, "sendEvent: sendEvent data");
        bluetoothConnectionService.write(bytes);
    }

    public void receivedRemoteEvent(byte[] bytes) {
        CustomMotionEvent motionEvent = null;
        //byte[] bytes = Base64.getDecoder().decode(received);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            motionEvent = (CustomMotionEvent) in.readObject();
            //Log.d(TAG, "read object successfully");
        } catch (ClassNotFoundException | IOException e) {
            //Log.e(TAG, e.getMessage());
            Log.d(TAG, "ERROR");
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

    public void loadRemoteMap(byte[] bytes) {
        MapWrapper mapWrapper = null;
        //byte[] bytes = Base64.getDecoder().decode(received);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            mapWrapper = (MapWrapper) in.readObject();
            //Log.d(TAG, "read object successfully");
        } catch (ClassNotFoundException | IOException e) {
            //Log.e(TAG, e.getMessage());
            Log.d(TAG, "ERROR");
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
        if (mapWrapper != null)
            DrawingController.getDrawingController().loadRemoteMap(mapWrapper.getMap());
        else
            Log.d(TAG, "ERROR: read object == null");
    }

    public void sendMyLineWidth() {
        bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        if (bluetoothConnectionService != null) {
            // be safe
            bluetoothConnectionService.write(BluetoothConstants.NOTIFY_LINEWIDTH.getBytes());
            bluetoothConnectionService.write(String.valueOf(drawingView.getLineWidth(0)).getBytes());
        }
    }

    public void setRemoteLineWidth(String received) {
        int width = Integer.parseInt(received);
        drawingView.setLineWidth(1, width);
    }

    public void notifyClear() {
        bluetoothConnectionService.write(BluetoothConstants.NOTIFY_CLEAR.getBytes());
        bluetoothConnectionService.write(String.valueOf(1).getBytes());
    }

    public void clearRemote() {
        drawingView.clear(1, 0);
    }

    // TODO: clean that shit up and look what is real drawing

    // TODO: BUG AFTER PAIRING on remote device --> doesnt get shown as paired
    // TODO: line width

}
