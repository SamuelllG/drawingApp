package com.mobileanwendungen.drawingapp.bluetooth.Utils;

import android.bluetooth.BluetoothDevice;

import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConnectionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BluetoothDevices {

    private List<BluetoothDevice> devices;
    private List<Integer> bonded;
    private int paired;
    private int connected;

    public BluetoothDevices () {
        devices = new ArrayList<>();
        bonded = new ArrayList<>();
        paired = -1;
        connected = -1;
    }


    public List<BluetoothDevice> getDevices() {
        return devices;
    }

    public BluetoothDevice getDevice(int index) {
        return devices.get(index);
    }

    public void addBonded(BluetoothDevice device) {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            if (bonded.contains(index)) {
                // is already bonded, do nothing
            } else {
                bonded.add(index);
            }
        } else {
            // if not contained, add to devices
            devices.add(device);
            int index = devices.indexOf(device);
            bonded.add(index);
        }
        sort();
    }
    public void addAllBonded(List<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            addBonded(device);
        }
    }

    private void sort() {
        int firstUnbonded = -1;
        for (int i = 0; i < devices.size(); i++) {
            if (isBonded(devices.get(i))) {
                if (firstUnbonded != -1) {
                    // swap firstUnbonded with i
                    BluetoothDevice temp = devices.get(firstUnbonded);
                    devices.set(firstUnbonded, devices.get(i));
                    bonded.remove((Object) i);
                    bonded.add(firstUnbonded);
                    firstUnbonded += 1;
                    devices.set(i, temp);
                }
                // else do nothing, we are still going through bonded devices
            }
            else
                firstUnbonded = i;
        }
    }
/*
    public BluetoothDevice getPaired() {
        if (devices.contains(paired))
            return devices.get(paired);
        return null;
    }

    public void setPaired(BluetoothDevice device) throws BluetoothConnectionException {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            paired = index;
        } else {
            throw new BluetoothConnectionException("No such bluetooth device");
        }
    }
*/
    public int getConnected() {
        return connected;
    }

    public void clearConnected() {
        connected = -1;
    }
/*
    public void setConnected(BluetoothDevice device) throws BluetoothConnectionException {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            if (bonded.contains(index)) {
                connected = index;
            } else {
                throw new BluetoothConnectionException("Device not bonded yet");
            }
        } else {
            throw new BluetoothConnectionException("No such bluetooth device");
        }
    }
*/

    public void setConnected(BluetoothDevice device) throws BluetoothConnectionException {
        if (isBonded(device)) {
            int index = devices.indexOf(device);
            connected = index;
        } else {
            throw new BluetoothConnectionException("Device not bonded");
        }
    }

    public void addDevice(BluetoothDevice device) {
        devices.add(device);
    }

    public void addAllDevices(List<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            addDevice(device);
        }
    }

    public void removeDevice(BluetoothDevice device) {
        devices.remove(device);
    }

    public boolean isBonded(BluetoothDevice device) {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            return bonded.contains(index);
        }
        return false;
    }

    public boolean isConnected(BluetoothDevice device) {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            return connected == index;
        }
        return false;
    }
/*
    public boolean isPaired(BluetoothDevice device) {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            return paired == index;
        }
        return false;
    }*/

    public BluetoothDevice getDeviceByAddress(String address) {
        for (BluetoothDevice d : devices) {
            if (d.getAddress().equals(address))
                return d;
        }
        return null;
    }
}
