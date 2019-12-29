package com.mobileanwendungen.drawingapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.rule.ActivityTestRule;

import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.utils.UIHelper;
import com.mobileanwendungen.drawingapp.views.BluetoothActivity;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


public class BluetoothActivityTest {
    private static final String TAG = "cust.BluetoothActivityTest";

    private BluetoothDevices bluetoothDevices;
    private UIHelper uiHelper;
    private BluetoothActivity bluetoothActivity;
    private BluetoothController bluetoothController;

    @Rule
    public ActivityTestRule<BluetoothActivity> activityRule = new ActivityTestRule<>(BluetoothActivity.class);

    @Before
    public void prepare() {
        bluetoothActivity = activityRule.getActivity();
        /*bluetoothDevices = Mockito.mock(BluetoothDevices.class);
        bluetoothDevice = PowerMockito.mock(BluetoothDevice.class);
        List<BluetoothDevice> devices = new ArrayList<>();
        devices.add(bluetoothDevice);
        devices.add(bluetoothDevice);
        Mockito.when(bluetoothDevices.getDevices()).thenReturn(devices);*/
        bluetoothDevices = new BluetoothDevices();
        bluetoothController = BluetoothController.getBluetoothController();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled())
            bluetoothController.toggleBluetooth();

        List<BluetoothDevice> list = new ArrayList<>();
        list.addAll(bluetoothAdapter.getBondedDevices());
        bluetoothDevices.addAllBonded(list);
        uiHelper = new UIHelper(bluetoothActivity);
        uiHelper.update(bluetoothDevices);
        uiHelper.setVisible(bluetoothActivity.getDevicesView());
    }

    @Test
    public void testStatusBonded() {
        //onView(withId(R.id.deviceStatus)).check(matches(withText("Bonded")));
        int n = bluetoothDevices.getDevices().size();
        for (int i = 0; i < n; i++) {
            onData(anything())
                    .inAdapterView(withId(R.id.listViewDevices))
                    .atPosition(i)
                    .onChildView(withId(R.id.deviceStatus))
                    .check(matches(withText("Bonded")));
        }

                //.perform(typeText(stringToBetyped), closeSoftKeyboard());
        //onView(withId(R.id.changeTextBt)).perform(click());

        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged))
                //.check(matches(withText(stringToBetyped)));
    }

    @Test
    public void testNewDevices() throws Exception {
        int currentSize = bluetoothDevices.getDevices().size();
        int previousSize = currentSize;
        bluetoothController.discover();
        Log.d(TAG, "----------------------------------------- STARTED ----------------------");
        while(true) {
            int previous = currentSize;
            Thread.sleep(2000);
            currentSize = bluetoothController.getBluetoothDevices().getDevices().size();
            if (previous == currentSize)
                break;
        }
        Log.d(TAG, "----------------------------------------- FINISHED ----------------------");
        if (previousSize < currentSize) {
            int index = currentSize - 1;
            while (index >= previousSize) {
                onData(anything())
                        .inAdapterView(withId(R.id.listViewDevices))
                        .atPosition(currentSize - 1)
                        .onChildView(withId(R.id.deviceStatus))
                        .check(matches(withText("")));
                index -= 1;
            }
        } else {
            throw new Exception("No devices found, not able to test");
        }
    }
}
