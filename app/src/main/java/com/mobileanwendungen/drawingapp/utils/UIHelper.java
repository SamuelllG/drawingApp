package com.mobileanwendungen.drawingapp.utils;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.adapters.DeviceListAdapter;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;
import com.mobileanwendungen.drawingapp.views.BluetoothActivity;

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

    public void makeToast(int textId, int duration) {
        Log.d(TAG, "making toast");
        bluetoothActivity.runOnUiThread(() -> {
            Toast.makeText(bluetoothActivity, textId, duration).show();
        });
    }

}
