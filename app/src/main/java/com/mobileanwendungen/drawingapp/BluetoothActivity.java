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

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "cust.BluetoothActivity";

    private BluetoothController bluetoothController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothController = BluetoothController.getBluetoothController();
        bluetoothController.init(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        bluetoothController.cleanup(this);
        super.onDestroy();
    }


    public ListView getDevicesView() {
        return findViewById(R.id.listViewDevices);
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
}
