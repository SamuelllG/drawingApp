package com.mobileanwendungen.drawingapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.bluetooth.Threads.AcceptThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectThread;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectedThread;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_DISCONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_LISTEN;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_NONE;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTING;
import static com.mobileanwendungen.drawingapp.bluetooth.BluetoothConstants.STATE_CONNECTED;

public class BluetoothConnectionService {
    public static final String TAG = "cust.BTConnectService";

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler; // handler that gets info from Bluetooth service

    private ConnectedThread connectedThread;
    private ConnectedThread backupThread;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    public static int mState;
    private BluetoothSocket duplicateConnection;
    public static boolean connectRequestReceived;
    public static int roll;


    public BluetoothConnectionService() {
        Log.d(TAG, "new BTConnectionService");
        handler = new MyHandler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    //acceptDevice
    public synchronized void startListening() {
        Log.d(TAG, "startListening");

        if (connectThread != null) {
            Log.d(TAG, "startListening: canceled connect thread");
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            Log.d(TAG, "startListening: canceled connected thread");
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread == null) {
            Log.d(TAG, "startListening: started accept thread");
            acceptThread = new AcceptThread(bluetoothAdapter);
            acceptThread.start();
        } else {
            Log.d(TAG, "startListening: accept thread already running");
            mState = STATE_LISTEN;
        }
    }

    public synchronized void connectTo(BluetoothDevice device) {

        if (/*mState == STATE_CONNECTING && */connectThread != null) {
            Log.d(TAG, "connectTo: canceled connect thread");
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            Log.d(TAG, "connectTo: canceled connected thread");
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device, bluetoothAdapter);
        connectThread.start();
    }


    public synchronized void startCommunication(BluetoothSocket socket, boolean viaServerSocket) {
        mState = STATE_CONNECTING;
        Log.d(TAG, "startCommunication");

        if (connectThread != null) {
            // if started via server --> cancel connectThread (if both users click "Connect" simultaneously,
            // then the socket of the connectThread can still connect to another application which will cause an error)
            if (viaServerSocket) // just for readability... cancel doesn't close the socket if it is use
            {
                Log.d(TAG, "startCommunication: connection via server socket --> canceled connect thread");
                connectThread.cancel();
            }
            connectThread = null;
        }
/*
        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            Log.d(TAG, "startCommunication: canceled connected thread");
            connectedThread.cancel();
            connectedThread = null;
        }
*/
        if (acceptThread != null) {
            Log.d(TAG, "startCommunication: canceled accept thread");
            acceptThread.cancel();
            acceptThread = null;
        }

        Log.d(TAG, "startCommunication: connected thread");
        connectedThread = new ConnectedThread(socket, handler);
        connectedThread.start();
        // wait for connectedThread to start
        try {
            Log.d(TAG, "startCommunication: wait for connectedThread");
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "startCommunication: request verified connection");
        write(BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION.getBytes());
        while (mState != BluetoothConstants.STATE_VERIFIED_CONNECTION) {
            try {
                Log.d(TAG, "startCommunication: wait for verified connection");
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Send the name of the connected device back to the UI Activity
    }

    public synchronized void saveDuplicateConnection(BluetoothSocket socket) {
        Log.d(TAG, "saveDuplicateConnection: remember duplicate connection");
        duplicateConnection = socket;
        if (connectedThread != null) {
            Log.d(TAG, "saveDuplicateConnection: cancel connected thread");
            connectedThread.cancel();
        }

        while (mState == STATE_DISCONNECTING) {
            try {
                Log.d(TAG, "saveDuplicateConnection: wait for cancel");
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        connectedThread = new ConnectedThread(duplicateConnection, handler);
        connectedThread.start();
        // wait for connectedThread to start
        try {
            Log.d(TAG, "saveDuplicateConnection: wait for connectedThread");
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "saveDuplicateConnection: request verified connection");
        write(BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION.getBytes());
        while (mState != BluetoothConstants.STATE_VERIFIED_CONNECTION) {
            try {
                Log.d(TAG, "saveDuplicateConnection: wait for verified connection");
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Log.d(TAG, "saveDuplicateConnection: request established connection"); // THIS WORKS !=!=!=!=!=:D:SD:SD:SD:SD

        /*backupThread = new ConnectedThread(duplicateConnection, handler);
        backupThread.start();
        try {
            Random random = new Random();
            int timeout = random.nextInt(3000);
            Log.d(TAG, "saveDuplicateConnection: wait for some time: " + timeout);

            // TODO: both waiting equally long??
            TimeUnit.MILLISECONDS.sleep(timeout);
            //Toast.makeText(BluetoothController.getBluetoothController)
            // ===============================
            // second try ?????????????????????
            while(connectedThread == null || backupThread == null) {
                TimeUnit.MILLISECONDS.sleep(500);
                Log.d(TAG, "Sleep");
            }
            random = new Random();
            timeout = random.nextInt(3000);
            Log.d(TAG, "saveDuplicateConnection: wait for some time: " + timeout);
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "saveDuplicateConnection: finished sleep, roll: " + roll + " --> don't send connect request");
        if (roll == 0) {
            Log.d(TAG, "saveDuplicateConnection: request connect");
            write(BluetoothConstants.REQUEST_CONNECT.getBytes(), true);
        }*/
    }

    /**
     * Disconnect and start listening again.
     */
    public synchronized void disconnect() {
        Log.d(TAG, "disconnect: ");

        mState = STATE_DISCONNECTING;
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            Log.d(TAG, "disconnect: terminate connectedThread");
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    public void write(byte[] out) {
        write(out, false);
    }

    public void write(byte[] out, boolean backup) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            if (backup)
                r = backupThread;
            else
                r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public synchronized void useConnection (ConnectedThread connection) {
        if (connectedThread == connection) {
            Log.d(TAG, "useConnection: use connectedThread");
            if (backupThread != null)
                backupThread.cancel();
        }
        if (backupThread == connection) {
            Log.d(TAG, "useConnection: use backupThread");
            if(connectedThread != null)
                connectedThread.cancel();
        }
        if (connection == null)
            Log.d(TAG, "useConnection: something wrong, ERROR");

        connectedThread = connection; // same as backup thread
        Log.d(TAG, "useConnection: request established connection");
        write(BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION.getBytes());
    }

    public synchronized void resetConnectedThread (ConnectedThread connection) {
        if (connectedThread == connection) {
            Log.d(TAG, "resetConnectedThread: reset connectedThread to null, use backupThread");
            connectedThread = null;
            connectedThread = backupThread;
        } else if (backupThread == connection) {
            Log.d(TAG, "resetConnectedThread: reset backupThread to null, use connectedThread");
            backupThread = null;
            if (connectedThread == null) {
                Log.d(TAG, "resetConnectedThread: something wrong ERROR: other thread is null as well");
            }
        } else {
            Log.d(TAG, "resetConnectedThread: something wrong ERROR");
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    public void connectionFailed(BluetoothDevice device) {
        // Send a failure message back to the Activity
        /*Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        //mState = STATE_NONE;
        Log.d(TAG, "connection failed");
        disconnect();
        // wait until all threads closed vvvvvvv
        Log.d(TAG, "connectionFailed: wait for connection being shut down");
        try {
            TimeUnit.MILLISECONDS.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "connectionFailed: start listening again");
        startListening();
        while(mState != STATE_LISTEN) {
            try {
                Log.d(TAG, "connectionFailed: wait for listening");
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BluetoothDevices bluetoothDevices = BluetoothController.getBluetoothController().getBluetoothDevices();
        bluetoothDevices.clearConnected();
/*
        // try backup connection
        Log.d(TAG, "connection lost: duplicate connection is connected: " + duplicateConnection.isConnected());
        if (duplicateConnection.isConnected()) {
            Log.d(TAG, "connection lost: try backup connection");
            startCommunication(duplicateConnection, true);
        }*/

        //if(mState != STATE_CONNECTED) {
            // duplicate connection was not successful --> restart listening
            //startListening();
        //}
        Random random = new Random();
        int timeout = random.nextInt(3000);
        Log.d(TAG, "connectionFailed: wait for some time: " + timeout);
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mState == STATE_LISTEN) {
            // only try connect if other device didn't try already
            Log.d(TAG, "connectionFailed: try to connect");
            connectTo(device);
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    public void connectionLost() {
        // Send a failure message back to the Activity
        /*Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        mState = STATE_NONE;
        Log.d(TAG, "connection lost");
        BluetoothController.getBluetoothController().getBluetoothDevices().clearConnected();
/*
        Log.d(TAG, "connection lost: duplicate connection is connected: " + duplicateConnection.isConnected());
        if (duplicateConnection.isConnected()) {
            // try backup connection
            Log.d(TAG, "connection lost: try backup connection");
            startCommunication(duplicateConnection, true);
        }*/
        if(mState != STATE_CONNECTED) {
            // duplicate connection was not successful --> restart listening
            startListening();
        }
    }
}
