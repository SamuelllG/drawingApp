package com.mobileanwendungen.drawingapp.bluetooth;

public class BluetoothConstants {

    //TODO: put from strings.xml here
    public static final String APP_NAME = "DrawingApp";
    public static final String UUID = "fb34bc1f-6f69-496d-a9e5-dea2af62f538";

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;

    public static final String BLUETOOTH_STATE_CHANGED = "drawingapp.BluetoothStateChanged";
    public static final String BlUETOOTH_STATE = "bluetoothState";
    public static final String BLUETOOTH_DEVICES = "bluetoothDevices";
    public static final String BLUETOOTH_SOCKET = "bluetoothSocket";
    public static final String REQUEST_CLOSE_CONNECTION = "REQUEST_CLOSE_CONNECTION";
    public static final String CONFIRMED_CLOSE_CONNECTION = "CONFIRM_CLOSE_CONNECTION";
    public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";
    public static final String REQUEST_CONNECT = "REQUEST_CONNECT";
    public static final String CONFIRMED_CONNECT_REQUEST = "CONFIRMED_CONNECT_REQUEST";
    public static final String REQUEST_ESTABLISHED_CONNECTION = "REQUEST_ESTABLISHED_CONNECTION";
    public static final String CONFIRMED_ESTABLISHED_CONNECTION = "CONFIRM_ESTABLISHED_CONNECTION";


    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCONNECTING = 4;
    public static final int STATE_VERIFIED_CONNECTION = 5;
}
