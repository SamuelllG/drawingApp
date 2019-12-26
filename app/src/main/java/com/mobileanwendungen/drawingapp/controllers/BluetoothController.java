package com.mobileanwendungen.drawingapp.controllers;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.views.BluetoothActivity;
import com.mobileanwendungen.drawingapp.listener.DeviceClickListener;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.utils.UIHelper;
import com.mobileanwendungen.drawingapp.broadcastReceivers.BluetoothBroadcastReceiver;
import com.mobileanwendungen.drawingapp.broadcastReceivers.BondStateChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.broadcastReceivers.DiscoverBroadcastReceiver;
import com.mobileanwendungen.drawingapp.broadcastReceivers.ScanModeChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.broadcastReceivers.StateChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionException;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

import java.util.ArrayList;
import java.util.List;

public class BluetoothController {
    private static final String TAG = "cust.BTController";
    private static BluetoothController BLUETOOTH_CONTROLLER;

    private BluetoothActivity bluetoothActivity;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isRunning;

    private UIHelper uiHelper;

    // BroadcastReceivers
    private BluetoothBroadcastReceiver stateChangedBroadcastReceiver;
    private BluetoothBroadcastReceiver scanModeChangedBroadcastReceiver;
    private BluetoothBroadcastReceiver discoverBroadcastReceiver;
    private BluetoothBroadcastReceiver bondStateChangedBroadcastReceiver;

    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothDevices bluetoothDevices;
    private boolean bluetoothWasDisabled;
    private boolean pause;


    private BluetoothController() {    }

    /**
     * Singleton
     */
    public static BluetoothController getBluetoothController() {
        if (BLUETOOTH_CONTROLLER == null) {
            BLUETOOTH_CONTROLLER = new BluetoothController();
        }
        return BLUETOOTH_CONTROLLER;
    }

    public void init(BluetoothActivity bluetoothActivity) {
        this.bluetoothActivity = bluetoothActivity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        uiHelper = new UIHelper(bluetoothActivity);
        if (!isRunning) {
            bluetoothDevices = new BluetoothDevices();
        }
        bluetoothActivity.getDevicesView().setOnItemClickListener(new DeviceClickListener(this));

        // BRs ---
        discoverBroadcastReceiver = new DiscoverBroadcastReceiver(bluetoothActivity, this);
        scanModeChangedBroadcastReceiver = new ScanModeChangedBroadcastReceiver(bluetoothActivity);
        stateChangedBroadcastReceiver = new StateChangedBroadcastReceiver(bluetoothActivity);
        bondStateChangedBroadcastReceiver = new BondStateChangedBroadcastReceiver(bluetoothActivity);

        // register so bluetooth doesn't have to be turned on via the app
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);

        // register so bonding is possible remotely (without click on this device, but just click on other device)
        IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        bluetoothActivity.registerReceiver(bondStateChangedBroadcastReceiver, filter);

        // if bluetooth is already on when entering the bluetooth activity
        if (bluetoothAdapter.isEnabled()) {
            onBluetoothOn();
        }
        isRunning = true;
        pause = false;
    }

    public void onBluetoothOn() {
        // necessary because bonded devices can only be gotten when bluetooth is enabled
        setBondedDevices();
        uiHelper.setVisible(bluetoothActivity.getDevicesView());

        if (bluetoothConnectionService == null)
            newBluetoothConnectionService();
    }

    public BluetoothConnectionService getBluetoothConnectionService() {
        return bluetoothConnectionService;
    }

    public BluetoothDevices getBluetoothDevices () {
        return bluetoothDevices;
    }

    public void toggleBluetooth() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "toggleBluetooth: no BT supported");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "toggleBluetooth: enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivity.startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);
        }
        else {
            Log.d(TAG, "toggleBluetooth: disable bluetooth");
            bluetoothWasDisabled = true;
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);
        }

    }

    public void enableDiscoverable() {
        Log.d(TAG, "toggleDiscoverable: making device discoverable");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        bluetoothActivity.startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        bluetoothActivity.registerReceiver(scanModeChangedBroadcastReceiver, intentFilter);
    }

    public void cancelDiscovery() {
        Log.d(TAG, "discover: canceling discovery");
        bluetoothAdapter.cancelDiscovery();
    }

    public void discover() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelDiscovery();
            Log.d(TAG, "discover: restarting discovery, looking for unpaired devices");
        } else
            Log.d(TAG, "discover: looking for unpaired devices");

        checkPermissions();
        bluetoothAdapter.startDiscovery();

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothActivity.registerReceiver(discoverBroadcastReceiver, discoverDevicesIntent);
    }

    public void onDeviceClicked(BluetoothDevice device) {
        if (bluetoothDevices.isConnected(device) && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_CONNECTED) {
            // clicked on connected device --> do nothing
            Log.d(TAG, "onDeviceClicked: clicked on already connected device");
            Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.clicked_connected), Toast.LENGTH_SHORT).show();
        } else if (bluetoothDevices.isBonded(device)) {
            // clicked on bonded device --> do nothing
            Log.d(TAG, "onDeviceClicked: clicked on already bonded device");
            Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.clicked_bonded), Toast.LENGTH_SHORT).show();
        } else {
            // clicked on new device --> pair
            Log.d(TAG, "onDeviceClicked: clicked on new device");
            pair(device);
        }
    }





    // ----- Bluetooth connection ------

    public void startConnection(String deviceAddress) {
        // clicked on "Connect" button of device with deviceAddress
        BluetoothDevice device = bluetoothDevices.getDeviceByAddress(deviceAddress);
        if (bluetoothDevices.isBonded(device)) {
            // clicked on a bonded device
            Log.d(TAG, "onConnectToggle: connect to bonded device");
        } else{
            // device not bonded
            Log.d(TAG, "onConnectToggle: device not bonded, pairing clicked device");
            pair(device);
        }
        bluetoothConnectionService.connectTo(device);
    }

    public void stopConnection() {
        Log.d(TAG, "stopConnection: stop connection service");
        if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectedThread() != null)
            bluetoothConnectionService.setState(BluetoothConstants.STATE_CLOSE_REQUEST);
        else if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectedThread() == null) {
            bluetoothConnectionService.close();
            //onClosed();
        }
        else if (bluetoothAdapter.isEnabled() && bluetoothConnectionService == null) {
            Log.d(TAG, "stopConnection: something went wrong, disabling bluetooth");
            bluetoothAdapter.disable();
        }
        else
            // just to be safe
            Log.d(TAG, "stopConnection: ERROR shouldn't get here");

    }

    public void onClosed() {
        bluetoothWasDisabled = false; // reset this
        bluetoothDevices.clearConnected();
        updateUI();

        if (!bluetoothAdapter.isEnabled() || pause) {
            // bluetooth was turned off or activity paused
            bluetoothConnectionService = null;
            uiHelper.setInvisible(bluetoothActivity.getDevicesView());
            isRunning = false;
        }
        else {
            // start a new connection service
            newBluetoothConnectionService();
        }
    }

    public void onEstablishedConnection() {
        try {
            bluetoothDevices.setConnected(bluetoothConnectionService.getRemoteDevice());
        } catch (BluetoothConnectionException e) {
            e.printStackTrace();
        }
        updateUI();

        RemoteHandler.getRemoteHandler().sendMyLineWidth();
    }


    // --------------


    public void pauseConnectionService() {
        if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_LISTEN) {
            Log.d(TAG, "pauseConnectionService");
            pause = true;
            bluetoothConnectionService.close();
        }
    }

    public void resumeConnectionService() {
        if (pause && bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "resumeConnectionService");
            pause = false;
            uiHelper.setVisible(bluetoothActivity.getDevicesView());
            newBluetoothConnectionService();
        }
    }

    public void unregisterAll() {
        Log.d(TAG, "unregister all");
        unregister(stateChangedBroadcastReceiver);
        unregister(scanModeChangedBroadcastReceiver);
        unregister(discoverBroadcastReceiver);
        unregister(bondStateChangedBroadcastReceiver);
    }

    public void cleanup() {
        //unregister(stateChangedBroadcastReceiver);
        //unregister(scanModeChangedBroadcastReceiver);
        //unregister(discoverBroadcastReceiver);
        //unregister(bondStateChangedBroadcastReceiver);
        if (bluetoothConnectionService != null) {
            bluetoothConnectionService.close();
            //bluetoothConnectionService = null; is set when blueConnServ finished
        }
    }

    public boolean getBluetoothWasDisabled() {
        return bluetoothWasDisabled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void unregister(BroadcastReceiver broadcastReceiver) {
        try {
            bluetoothActivity.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            // wasn't registered, do nothing
            // this is the only solution since you cannot check for registered receivers
        }
    }

    private List<BluetoothDevice> getBondedDevices() {
        List<BluetoothDevice> list = new ArrayList<>();
        list.addAll(bluetoothAdapter.getBondedDevices());
        return list;
    }

    private void setBondedDevices() {
        bluetoothDevices.addAllBonded(getBondedDevices());
        updateUI();
    }

    public void updateUI() {
        uiHelper.update(bluetoothDevices);
    }

    private void newBluetoothConnectionService() {
        // TODO: fix quickfix
        // prevent btConnectionService thread from "restarting" itself, should only be started from a UI thread (because of handler)
        bluetoothActivity.runOnUiThread(() -> {
            bluetoothConnectionService = new BluetoothConnectionService(this, uiHelper);
            bluetoothConnectionService.start();
        });
    }

    private void createBond(BluetoothDevice device) {
        cancelDiscovery();
        // create bond
        // requires API 19+
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "createBond: trying to pair with " + device.getName());
            device.createBond();
        }
    }

    // TODO: notifyClear list if bluetooth disabled, remove devices from list that are not found anymore
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "checkPermissions: checking bluetooth permissions");
            int permissionCheck = bluetoothActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += bluetoothActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != 0) {

                bluetoothActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkPermissions: no need to check permissions, SDK version < LOLLIPOP");
        }
    }

    private void pair(BluetoothDevice device) {
        if (bluetoothAdapter.getBondedDevices().contains(device)) {
            // is a bonded device
            Log.d(TAG, "pair: device is already bonded");
            Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.already_bonded), Toast.LENGTH_SHORT).show();
            //onIsBonded(device);
        } else {
            // is a new device
            Log.d(TAG, "pair: create bond with device");
            Toast.makeText(bluetoothActivity, bluetoothActivity.getResources().getString(R.string.pairing), Toast.LENGTH_SHORT).show();
            createBond(device);

            IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            bluetoothActivity.registerReceiver(bondStateChangedBroadcastReceiver, filter);
        }
    }
}
