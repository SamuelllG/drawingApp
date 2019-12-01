package com.mobileanwendungen.drawingapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.Threads.AcceptThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectedThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.TimeoutThread;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_RESTARTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_SHUT_DOWN;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CLOSED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CLOSE_REQUEST;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CLOSING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING_VIA_SERVER;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_FAILED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_INTERRUPTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_LISTEN;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_NONE;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_INIT_RESTART;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_TIMEOUT;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_UNABLE_TO_CONNECT;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_VERIFICATION;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_VERIFIED_CONNECTION;

public class BluetoothConnectionService extends Thread{
    public static final String TAG = "cust.BTConnectService";

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler; // handler that gets info from Bluetooth service

    private volatile ConnectedThread connectedThread;
    private ConnectedThread backupThread;
    private volatile ConnectThread connectThread;
    private volatile AcceptThread acceptThread;
    private volatile TimeoutThread timeoutThread;
    private volatile int mState;
    private int oldState;
    private BluetoothSocket connectionSocket;
    private BluetoothSocket duplicateConnection;
    public static boolean connectRequestReceived;
    public static int roll;
    private BluetoothDevice remoteDevice;
    private volatile boolean receivedResponse;

    public synchronized void setConnectionSocket(BluetoothSocket socket) {
        connectionSocket = socket;
    }

    public synchronized void setState(int state) {
        String name;
        switch (state) {
            case STATE_NONE:
                name = "STATE_NONE";
                break;
            case STATE_LISTEN:
                name = "STATE_LISTEN";
                break;
            case STATE_CONNECTING:
                name = "STATE_CONNECTING";
                break;
            case STATE_CONNECTED:
                name = "STATE_CONNECTED";
                break;
            case STATE_VERIFIED_CONNECTION:
                name = "STATE_VERIFIED_CONNECTION";
                break;
            case STATE_FAILED:
                name = "STATE_FAILED";
                break;
            case STATE_UNABLE_TO_CONNECT:
                name = "STATE_UNABLE_TO_CONNECT";
                break;
            case STATE_CONNECTING_VIA_SERVER:
                name = "STATE_CONNECTING_VIA_SERVER";
                break;
            case STATE_INTERRUPTED:
                name = "STATE_INTERRUPTED";
                break;
            case STATE_VERIFICATION:
                name = "STATE_VERIFICATION";
                break;
            case STATE_CLOSING:
                name = "STATE_CLOSING";
                break;
            case STATE_CLOSED:
                name = "STATE_CLOSED";
                break;
            case STATE_CLOSE_REQUEST:
                name = "STATE_CLOSE_REQUEST";
                break;
            case STATE_INIT_RESTART:
                name = "STATE_INIT_RESTART";
                break;
            case STATE_TIMEOUT:
                name = "STATE_TIMEOUT";
                break;
            case STATE_SHUT_DOWN:
                name = "STATE_SHUT_DOWN";
                break;
            case STATE_RESTARTING:
                name = "STATE_RESTARTING";
                break;
            default:
                name = "STATE NOT FOUND";
        }
        Log.d(TAG, "mState = " + name);
        mState = state;
        //onStateChanged();
    }

    public synchronized int getConnectionState() {
        return mState;
    }

    public synchronized void onStateChanged (int oldState) {
        switch (mState) {
            case STATE_NONE:
            case STATE_RESTARTING:
                // start listening
                acceptThread = new AcceptThread(bluetoothAdapter);
                acceptThread.start();
                break;
            case STATE_LISTEN:
                if (oldState == STATE_RESTARTING)
                    restartConnection();
                else
                    this.notifyAll();
                break;
            case STATE_CONNECTING:
                if (connectionSocket == null)
                    Log.e(TAG, "ERROR");
                stopAcceptThread();
                startCommunication(connectionSocket);
                break;
            case STATE_CONNECTING_VIA_SERVER:
                if (connectionSocket == null)
                    Log.e(TAG, "ERROR");
                stopAcceptThread();
                if (connectThread != null)
                    stopConnectThread();
                startCommunication(connectionSocket);
                break;
            case STATE_CONNECTED:
                connectedThread.start();
                break;
            case STATE_VERIFICATION:
                Log.d(TAG, "request established connection");
                request(BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION);
                break;
            case STATE_VERIFIED_CONNECTION:
                Log.d(TAG, "running...");
                break;
            case STATE_FAILED:
                Log.d(TAG, "ERROR: FAILED");
                setState(STATE_INIT_RESTART);
                break;
            case STATE_UNABLE_TO_CONNECT:
                Log.d(TAG, "other device is not available");
                // do nothing, keep listening
                setState(STATE_LISTEN);
                break;
            case STATE_INTERRUPTED:
                // TODO: testen
                Log.d(TAG, "ERROR: connection interrupted");
                setState(STATE_INIT_RESTART);
                break;
            case STATE_TIMEOUT:
                Log.d(TAG, " --/ timeout /--");
                if (oldState == STATE_CLOSE_REQUEST) {
                    Log.d(TAG, "force closing...");
                    setState(STATE_CLOSING);
                } else {
                    setState(STATE_INIT_RESTART);
                }
                break;
            case STATE_CLOSE_REQUEST:
                Log.d(TAG, "request close connection");
                request(BluetoothConstants.REQUEST_CLOSE_CONNECTION);
                break;
            case STATE_CLOSING:
                closeAll();
                break;
            case STATE_CLOSED:
                // finish thread
                setState(BluetoothConstants.STATE_SHUT_DOWN);
                BluetoothController.getBluetoothController().onConnectionClosed();
                Log.d(TAG, "-----------------------------------------------------------------");
                break;
            case STATE_INIT_RESTART:
                closeAll();
                initRestart();
                break;
            default:
                Log.e(TAG, "STATE NOT FOUND");
        }
    }

    public BluetoothDevice getRemoteDevice() {
        return connectedThread.getRemoteDevice();
    }


    public BluetoothConnectionService() {
        Log.d(TAG, "new BTConnectionService");
        handler = new MyHandler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(STATE_NONE);
        oldState = Integer.MAX_VALUE;
    }

    @Override
    public void run() {
        Log.d(TAG, "start BTConnectionService");
        // kind of event listener for the state
        while (mState != STATE_SHUT_DOWN) {
            if (oldState != mState) {
                //Log.d(TAG, "old state: " + oldState + " new state: " + mState);
                int remember = oldState;
                // setting this first is important
                oldState = mState;
                onStateChanged(remember);
            }
        }
    }


    public synchronized void connectTo(BluetoothDevice device) {
        connectThread = new ConnectThread(device, bluetoothAdapter);
        connectThread.start();
    }

    private synchronized void stopConnectThread() {
        if (connectThread != null)
            connectThread.cancel();
        while (connectThread != null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "connect thread closed");
    }
    private synchronized void stopAcceptThread() {
        if (acceptThread != null)
            acceptThread.cancel();
        while (acceptThread != null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "accept thread closed");
    }
    private synchronized void stopConnectedThread() {
        if (connectedThread != null)
            connectedThread.cancel();
        while (connectedThread != null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "connected thread closed");
    }


    public synchronized void startCommunication(BluetoothSocket socket) {
        if (remoteDevice == socket.getRemoteDevice()) {
            // this is the second try (RESTARTING)
            remoteDevice = null;
        } else {
            // first try, remember remote device in case something goes wrong
            remoteDevice = socket.getRemoteDevice();
        }
        Log.d(TAG, "startCommunication");
        connectedThread = new ConnectedThread(socket, handler);
        setState(STATE_CONNECTED);
    }

    public synchronized void closeAll() {
        Log.d(TAG, "closeAll: ");

        if (connectedThread != null) {
            stopConnectedThread();
        }

        if (connectThread != null) {
            stopConnectThread();
        }

        if (acceptThread != null) {
            stopAcceptThread();
        }

        if (mState == STATE_CLOSING && connectedThread == null && connectThread == null && acceptThread == null) {
            Log.d(TAG, "no open threads");
            setState(STATE_CLOSED);
        } else if (connectedThread == null && connectThread == null && acceptThread == null) {
            Log.d(TAG, "no open threads");
        }
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = connectedThread;
        }
        if (r != null) {
            // Perform the write unsynchronized
            r.write(out);
            Log.d(TAG, "wrote something");
        } else {
            Log.d(TAG, " ERROR: connectedThread was closed before write");
        }

    }


// ---------------------------
    public synchronized void request(String request) {
        //receivedResponse = false;
        //connectedThread.newRequest();
        receivedResponse = false;

        write(request.getBytes());
        //connectedThread.write(request.getBytes());
        waitForResponse();
    }
/*
    private volatile boolean receivedResponse;
    public synchronized void setReceivedResponse(boolean b) {
        receivedResponse = b;
        this.notifyAll();
    }
*/
    public void setReceivedResponse (boolean b) {
        receivedResponse = b;
    }

    private synchronized void waitForResponse() {
        timeoutThread = new TimeoutThread(this);
        timeoutThread.start();
    }

    public synchronized void onThreadClosed (String thread) {
        switch(thread) {
            case BluetoothConstants.CONNECTED_THREAD:
                Log.d(TAG, "onThreadClosed: connectedThread");
                connectedThread = null;
                break;
            case BluetoothConstants.CONNECT_THREAD:
                Log.d(TAG, "onThreadClosed: connectThread");
                connectThread = null;
                break;
            case BluetoothConstants.ACCEPT_THREAD:
                Log.d(TAG, "onThreadClosed: acceptThread");
                acceptThread = null;
                break;
            default:
                Log.e(TAG, "onThreadClosed: no such thread");
        }
        this.notifyAll();
    }

    private synchronized void initRestart() {
        Log.d(TAG, "init restart");
        // start listening
        setState(STATE_RESTARTING);

        // this can be done in order and all in the same thread
        //onStateChanged();
        // wait until listening
        /*while(mState != STATE_LISTEN) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    private synchronized void restartConnection() {
        if (remoteDevice != null) {
            Random random = new Random();
            int timeout = random.nextInt(3000);
            Log.d(TAG, "restarting... sleep for " + timeout + "ms");
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectTo(remoteDevice);
        } else {
            // either something went wrong or more likely this is the second try (RESTARTING)
            // should only try once
            Log.d(TAG, "no remote device");
            //MAKETOAST connection failed, try again
            // close connection (+restart)
            setState(STATE_CLOSING);
        }
    }

    public boolean getReceivedResponse() {
        return receivedResponse;
    }

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }
}
