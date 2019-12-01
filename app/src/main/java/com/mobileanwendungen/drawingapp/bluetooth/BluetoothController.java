package com.mobileanwendungen.drawingapp.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.BluetoothActivity;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.BluetoothBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.BluetoothStateChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.BondStateChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.DiscoverBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.ScanModeChangedBroadcastReceiver;
import com.mobileanwendungen.drawingapp.bluetooth.BroadcastReceivers.StateChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class BluetoothController {
    private static final String TAG = "cust.BTController";
    private static BluetoothController BLUETOOTH_CONTROLLER;

    private BluetoothActivity bluetoothActivity;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcastReceiver stateChangedBroadcastReceiver;
    private BluetoothBroadcastReceiver scanModeChangedBroadcastReceiver;
    private BluetoothBroadcastReceiver discoverBroadcastReceiver;
    private BluetoothBroadcastReceiver bondStateChangedBroadcastReceiver;
    //private BluetoothBroadcastReceiver bluetoothStateChangedBroadcastReceiver;


    private BluetoothConnectionService bluetoothConnectionService;

    private BluetoothDevices bluetoothDevices;
    private boolean waitingForBluetoothDisable;

    private BluetoothController() {
        //
    }

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
        bluetoothDevices = new BluetoothDevices();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //IntentFilter Intent = new IntentFilter(BluetoothConstants.BLUETOOTH_STATE_CHANGED);
        //bluetoothActivity.registerReceiver(bluetoothStateChangedBroadcastReceiver, Intent);

        discoverBroadcastReceiver = new DiscoverBroadcastReceiver(bluetoothActivity);
        scanModeChangedBroadcastReceiver = new ScanModeChangedBroadcastReceiver(bluetoothActivity);
        stateChangedBroadcastReceiver = new StateChangedBroadcastReceiver(bluetoothActivity);
        bondStateChangedBroadcastReceiver = new BondStateChangedBroadcastReceiver(bluetoothActivity);
        //bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver(bluetoothActivity, bluetoothConnectionService);

        // if bluetooth is already on when entering the bluetooth activity
        if (bluetoothAdapter.isEnabled()) {
            newBluetoothConnectionService();
            // register receiver in case of error with bluetoothAdapter
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);
        }
    }

    public void newBluetoothConnectionService() {
        // prevent btConnectionService thread from "restarting" itself, should only be started from a UI thread (because of handler)
        bluetoothActivity.runOnUiThread(() -> {
            bluetoothConnectionService = new BluetoothConnectionService();
            bluetoothConnectionService.start();
        });
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
            getBondedDevices();
            bluetoothActivity.listViewDevices.setVisibility(View.VISIBLE);
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivity.startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);
        }
        else {
            Log.d(TAG, "toggleBluetooth: stop connection service");
            waitingForBluetoothDisable = true;
            stopConnection();
        }

    }

    private void disableBluetooth() {
        waitingForBluetoothDisable = false;
        Log.d(TAG, "disableBluetooth: disable bluetooth");
        bluetoothActivity.listViewDevices.setVisibility(View.INVISIBLE);
        bluetoothAdapter.disable();
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothActivity.registerReceiver(stateChangedBroadcastReceiver, BTIntent);
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

            //check BT permissions in manifest
            checkPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            bluetoothActivity.registerReceiver(discoverBroadcastReceiver, discoverDevicesIntent);
        }
        if (!bluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "discover: looking for unpaired devices");

            //check BT permissions in manifest
            checkPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            bluetoothActivity.registerReceiver(discoverBroadcastReceiver, discoverDevicesIntent);
        }
    }

    public void createBond(BluetoothDevice device) {
        // TODO: FRAGE: receivers get register multiple times ... unregister it, if it's already existing? or skip it?
        cancelDiscovery();
        IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        bluetoothActivity.registerReceiver(bondStateChangedBroadcastReceiver, filter);

        // create bond
        // requires API 19+
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // TODO not just call when clicked on device ?
            /*if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "createBond: already bonded with " + device.getName());
                pairedDevice = device;
                initConnectionService();
            } else {*/
                Log.d(TAG, "createBond: trying to pair with " + device.getName());
                device.createBond();
            //}
        }
    }

    // TODO: clear list if bluetooth disabled, remove devices from list that are not found anymore
    // this is called in every method, but although permission is granted, android asks for it again (default behaviour)
    // method isn't even needed, works without
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

    public void cleanup(Activity bluetoothActivity) {
        bluetoothActivity.unregisterReceiver(stateChangedBroadcastReceiver);
        bluetoothActivity.unregisterReceiver(scanModeChangedBroadcastReceiver);
        bluetoothActivity.unregisterReceiver(discoverBroadcastReceiver);
        bluetoothActivity.unregisterReceiver(bondStateChangedBroadcastReceiver);
    }
/*
    public void onIsBonded(BluetoothDevice device) {
        try {
            bluetoothDevices.setPaired(device);
        } catch (BluetoothConnectionException e) {
            e.printStackTrace();
        }
        bluetoothActivity.deviceListAdapter = new DeviceListAdapter(bluetoothActivity, R.layout.device_adapter_view, bluetoothDevices);
        bluetoothActivity.listViewDevices.setAdapter(bluetoothActivity.deviceListAdapter);
        bluetoothConnectionService.startListening();
    }
*/
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
        }
    }

    public BluetoothConnectionService getBluetoothConnectionService() {
        return bluetoothConnectionService;
    }

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
        bluetoothConnectionService.setState(BluetoothConstants.STATE_CLOSE_REQUEST);
    }

    public void onConnectionClosed() {
        bluetoothDevices.clearConnected();
        bluetoothActivity.runOnUiThread(() -> {
            bluetoothActivity.deviceListAdapter = new DeviceListAdapter(bluetoothActivity, R.layout.device_adapter_view, bluetoothDevices);
            bluetoothActivity.listViewDevices.setAdapter(bluetoothActivity.deviceListAdapter);
        });


        if (waitingForBluetoothDisable) {
            Log.d(TAG, "connection closed");
            bluetoothConnectionService = null;
            disableBluetooth();
        } else {
            newBluetoothConnectionService();
        }
    }

/*
    private void startBluetoothConnection(BluetoothDevice device) {
        Log.d(TAG, "startBluetoothConnection: ");
        bluetoothConnectionService.connectTo(device);
    }
*/
    public List<BluetoothDevice> getBondedDevices() {
        List<BluetoothDevice> list = new ArrayList<>();
        list.addAll(bluetoothAdapter.getBondedDevices());
        return list;
    }

    public void onState(int connectionState) {
        // TODO: refactor this
        switch (connectionState) {
            case BluetoothConstants.STATE_CONNECTED:
                // update UI
                try {
                    bluetoothDevices.setConnected(bluetoothConnectionService.getRemoteDevice());
                } catch (BluetoothConnectionException e) {
                    e.printStackTrace();
                }
                bluetoothActivity.runOnUiThread(() -> {
                    bluetoothActivity.deviceListAdapter = new DeviceListAdapter(bluetoothActivity, R.layout.device_adapter_view, bluetoothDevices);
                    bluetoothActivity.listViewDevices.setAdapter(bluetoothActivity.deviceListAdapter);
                });
                break;
        }
    }

}
