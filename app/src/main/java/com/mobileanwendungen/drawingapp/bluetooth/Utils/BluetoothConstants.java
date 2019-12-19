package com.mobileanwendungen.drawingapp.bluetooth.Utils;

public class BluetoothConstants {

    //TODO: put from strings.xml here
    public static final String APP_NAME = "DrawingApp";
    public static final String UUID = "fb34bc1f-6f69-496d-a9e5-dea2af62f538";

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;


    public static final String CONNECTED_THREAD = "CONNECTED_THREAD";
    public static final String CONNECT_THREAD = "CONNECT_THREAD";
    public static final String ACCEPT_THREAD = "ACCEPT_THREAD";

    public static final long TIMEOUT = 2000; // in ms



    //public static final String BLUETOOTH_STATE_CHANGED = "drawingapp.BluetoothStateChanged";
    //public static final String BlUETOOTH_STATE = "bluetoothState";
    //public static final String BLUETOOTH_DEVICES = "bluetoothDevices";
    //public static final String BLUETOOTH_SOCKET = "bluetoothSocket";
    //public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";
    //public static final String REQUEST_CONNECT = "REQUEST_CONNECT";
    //public static final String CONFIRMED_CONNECT_REQUEST = "CONFIRMED_CONNECT_REQUEST";


    public static final String NOTIFY_DATA = "NOTIFY_DATA";

    // Requests:
    public static final String[] REQUESTS = {   "REQUEST_ESTABLISHED_CONNECTION",
                                                "REQUEST_CLOSE_CONNECTION" };

    public static final String REQUEST_ESTABLISHED_CONNECTION = "REQUEST_ESTABLISHED_CONNECTION";
    public static final String REQUEST_CLOSE_CONNECTION = "REQUEST_CLOSE_CONNECTION";


    // Responses:
    public static final String[] RESPONSES = {  "CONFIRM_ESTABLISHED_CONNECTION",
                                                "CONFIRM_CLOSE_CONNECTION" };

    public static final String CONFIRM_ESTABLISHED_CONNECTION = "CONFIRM_ESTABLISHED_CONNECTION";
    public static final String CONFIRM_CLOSE_CONNECTION = "CONFIRM_CLOSE_CONNECTION";


    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_VERIFIED_CONNECTION = 5;
    public static final int STATE_FAILED = 6;
    public static final int STATE_UNABLE_TO_CONNECT = 13;
    public static final int STATE_CONNECTING_VIA_SERVER = 7;
    public static final int STATE_INTERRUPTED = 8;
    public static final int STATE_VERIFICATION = 9;
    public static final int STATE_CLOSING = 10;
    public static final int STATE_CLOSED = 11;
    public static final int STATE_CLOSE_REQUEST = 12;
    public static final int STATE_INIT_RESTART = 14;
    public static final int STATE_TIMEOUT = 15;
    public static final int STATE_SHUT_DOWN = 16;
    public static final int STATE_RESTARTING = 17;
    public static final int STATE_FORCE_CLOSE = 18;
}
