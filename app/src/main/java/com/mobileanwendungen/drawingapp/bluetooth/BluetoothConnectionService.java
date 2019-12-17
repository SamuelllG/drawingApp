package com.mobileanwendungen.drawingapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.UIHelper;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.AcceptThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectedThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.TimeoutThread;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_FORCE_CLOSE;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_RESTARTING;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_SHUT_DOWN;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CLOSED;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CLOSE_REQUEST;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CLOSING;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CONNECTING_VIA_SERVER;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_FAILED;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_INTERRUPTED;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_LISTEN;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_NONE;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_CONNECTED;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_INIT_RESTART;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_TIMEOUT;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_UNABLE_TO_CONNECT;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_VERIFICATION;
import static com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants.STATE_VERIFIED_CONNECTION;

public class BluetoothConnectionService extends Thread {
    private static final String TAG = "cust.BTConnectService";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothController bluetoothController;
    private UIHelper uiHelper;
    private Handler handler; // handler that gets info from Bluetooth service

    private volatile int mState;
    private int oldState; // this is only for startup
    private volatile boolean forceClosing;
    private volatile ConnectedThread connectedThread;
    private volatile ConnectThread connectThread;
    private volatile AcceptThread acceptThread;
    private volatile TimeoutThread timeoutThread;

    private volatile boolean receivedResponse;
    private BluetoothSocket connectionSocket;
    private BluetoothDevice remoteDevice;


    public BluetoothConnectionService(BluetoothController bluetoothController, UIHelper uiHelper) {
        Log.d(TAG, "new BTConnectionService");
        handler = new MessageHandler(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothController = bluetoothController;
        this.uiHelper = uiHelper;
        setState(STATE_NONE);
        oldState = Integer.MAX_VALUE;
    }

    @Override
    public void run() {
        Log.d(TAG, "start BTConnectionService");
        // bcs only processes state changes
        while (mState != STATE_SHUT_DOWN) {
            if (oldState != mState) {
                int remember = oldState;
                oldState = mState; // setting this first is important
                if (mState == STATE_FORCE_CLOSE) {
                    forceClosing = true;
                    onStateChanged(remember);
                    break; // break the loop -> ignore incoming states
                }
                onStateChanged(remember);
            }
        }
    }


    private synchronized void onStateChanged(int oldState) {
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
                bluetoothController.onEstablishedConnection();
                Log.d(TAG, "running...");
                break;
            case STATE_FAILED:
                Log.d(TAG, "ERROR: FAILED");
                bluetoothController.getBluetoothDevices().clearConnected();
                bluetoothController.updateUI();
                setState(STATE_INIT_RESTART);
                break;
            case STATE_UNABLE_TO_CONNECT:
                Log.d(TAG, "other device is not available");
                uiHelper.makeToast(R.string.unable_to_connect, Toast.LENGTH_LONG);
                // do nothing, keep listening
                setState(STATE_LISTEN);
                break;
            case STATE_INTERRUPTED:
                Log.d(TAG, "ERROR: connection interrupted");
                bluetoothController.getBluetoothDevices().clearConnected();
                bluetoothController.updateUI();
                setState(STATE_INIT_RESTART);
                break;
            case STATE_TIMEOUT:
                Log.d(TAG, " --/ timeout /--");
                bluetoothController.getBluetoothDevices().clearConnected();
                bluetoothController.updateUI();
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
            case STATE_FORCE_CLOSE:
                // force close by calling onStateChanged directly
                mState = STATE_CLOSING;
                onStateChanged(STATE_FORCE_CLOSE);
                break;
            case STATE_CLOSING:
                closeAll();
                break;
            case STATE_CLOSED:
                setState(BluetoothConstants.STATE_SHUT_DOWN); // if force closing can be ignored because thread loop already finished
                bluetoothController.onClosed();
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

    public synchronized void connectTo(BluetoothDevice device) {
        connectThread = new ConnectThread(device, bluetoothAdapter);
        connectThread.start();
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


    private synchronized void startCommunication(BluetoothSocket socket) {
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

    private synchronized void request(String request) {
        receivedResponse = false;
        write(request.getBytes());
        waitForResponse();
    }

    private synchronized void closeAll() {
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
            if (forceClosing) {
                mState = STATE_CLOSED;
                onStateChanged(STATE_CLOSING);
            } else {
                setState(STATE_CLOSED);
            }
        } else if (connectedThread == null && connectThread == null && acceptThread == null) {
            Log.d(TAG, "no open threads");
        }
    }

    private synchronized void initRestart() {
        Log.d(TAG, "init restart");
        // start listening
        setState(STATE_RESTARTING);
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
            //connection failed, try again
            uiHelper.makeToast(R.string.restart_failed, Toast.LENGTH_LONG);
            setState(STATE_CLOSING);
        }
    }

    private synchronized void waitForResponse() {
        timeoutThread = new TimeoutThread(this);
        timeoutThread.start();
    }


    // Stop threads

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


    // Get, set

    public synchronized void setConnectionSocket(BluetoothSocket socket) {
        connectionSocket = socket;
    }

    public synchronized void setState(int state) {
        if (forceClosing) {
            // don't log incoming states when force closing
            Log.d(TAG, "IGNORING STATE... FORCE CLOSING");
            return;
        }
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
            case STATE_FORCE_CLOSE:
                name = "STATE_FORCE_CLOSE";
                break;
            default:
                name = "STATE NOT FOUND";
        }
        Log.d(TAG, "mState = " + name);
        mState = state;
    }

    public synchronized int getConnectionState() {
        return mState;
    }

    public BluetoothDevice getRemoteDevice() {
        return connectedThread.getRemoteDevice();
    }

    public void setReceivedResponse (boolean b) {
        receivedResponse = b;
    }

    public boolean getReceivedResponse() {
        return receivedResponse;
    }

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }

    public void close() {
        Log.d(TAG, "close");
        setState(BluetoothConstants.STATE_CLOSING);
        if (timeoutThread != null) {
            //timeoutThread.cancel();
            timeoutThread = null;
        }
    }
}
