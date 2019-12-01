package com.mobileanwendungen.drawingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothDevices;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.DeviceListAdapter;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "cust.BluetoothActivity";
    //private static BluetoothActivity bluetoothActivity;



    //public static BluetoothActivity getBluetoothActivity() {
        //return bluetoothActivity;
    //}

    private BluetoothController bluetoothController;

    public DeviceListAdapter deviceListAdapter;
    public ListView listViewDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //TODO: FRAGE: recyclerview
        //bluetoothActivity = this;

        bluetoothController = BluetoothController.getBluetoothController();
        bluetoothController.init(this);

        listViewDevices = (ListView) findViewById(R.id.listViewDevices);
        // set bonded devices
        setBondedDevices();


        listViewDevices.setOnItemClickListener(this);
    }

    private void setBondedDevices() {
        bluetoothController.getBluetoothDevices().addAllBonded(bluetoothController.getBondedDevices());
        deviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, bluetoothController.getBluetoothDevices());
        listViewDevices.setAdapter(deviceListAdapter);
    }


    public void onToggleBluetooth(View view){
        Log.d(TAG, "onClick: toggle bluetooth");
        bluetoothController.toggleBluetooth();
    }

    public void onEnableDiscoverable(View view) {
        Log.d(TAG, "onClick: toggle discoverable");
        bluetoothController.enableDiscoverable();
    }

    public void onDiscover(View view) {
        Log.d(TAG, "onClick: discover devices");
        bluetoothController.discover();
    }

    public void onConnectToggle(View view) {
        View item = (View) view.getParent();
        TextView textView = item.findViewById(R.id.tvDeviceAddress);
        String deviceAdress = (String) textView.getText();

        Button button = view.findViewById(R.id.connectButton);
        String connectString = getResources().getString(R.string.connectButton);
        boolean shouldConnect = button.getText().equals(connectString);
        if (shouldConnect) {
            Log.d(TAG, "onClick: \"Connect\" start connection");
            bluetoothController.startConnection(deviceAdress);
        } else {
            // should disconnect
            Log.d(TAG, "onClick: \"Disconnect\" terminate connection");
            bluetoothController.stopConnection();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        bluetoothController.cleanup(this);
        // TODO: clean up threads as well
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevices bluetoothDevices = bluetoothController.getBluetoothDevices();
        BluetoothDevice device = bluetoothDevices.getDevice(position);

        if (bluetoothDevices.isBonded(device)) {
            Log.d(TAG, "onItemClick: clicked on a bonded device");
        } else {
            Log.d(TAG, "onItemClick: clicked on a device");
        }

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        bluetoothController.onDeviceClicked(device);
    }
}
