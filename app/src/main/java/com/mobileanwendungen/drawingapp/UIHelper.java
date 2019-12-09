package com.mobileanwendungen.drawingapp;

import android.view.View;
import android.widget.ListView;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothDevices;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.DeviceListAdapter;

public class UIHelper {
    private static final String TAG = "cust.UIHelper";

    private BluetoothActivity bluetoothActivity;

    public UIHelper(BluetoothActivity bluetoothActivity) {
        this.bluetoothActivity = bluetoothActivity;
    }

    public void update(BluetoothDevices bluetoothDevices) {
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(bluetoothActivity, R.layout.device_adapter_view, bluetoothDevices);

        bluetoothActivity.runOnUiThread(() -> {
            bluetoothActivity.getDevicesView().setAdapter(deviceListAdapter);
        });
    }

    public void setVisible(ListView listView) {
        bluetoothActivity.runOnUiThread(() -> {
            listView.setVisibility(View.VISIBLE);
        });
    }

    public void setInvisible(ListView listView) {
        bluetoothActivity.runOnUiThread(() -> {
            listView.setVisibility(View.INVISIBLE);
        });
    }

}
