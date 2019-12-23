package com.mobileanwendungen.drawingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.ConnectThread;
import com.mobileanwendungen.drawingapp.bluetooth.Utils.BluetoothConstants;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectThread.class, Log.class, BluetoothController.class})
public class ConnectThreadUnitTest {

    private ConnectThread connectThread;
    private BluetoothController bluetoothController;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionService bluetoothConnectionService;

    @Before
    public void setup() throws IOException {
        PowerMockito.mockStatic(Log.class);

        bluetoothController = PowerMockito.mock(BluetoothController.class);
        bluetoothSocket = PowerMockito.mock(BluetoothSocket.class);
        bluetoothDevice = PowerMockito.mock(BluetoothDevice.class);
        bluetoothAdapter = PowerMockito.mock(BluetoothAdapter.class);
        bluetoothConnectionService = PowerMockito.mock(BluetoothConnectionService.class);

        // mock singleton with PowerMock
        PowerMock.reset();
        PowerMock.mockStatic(BluetoothController.class);
        EasyMock.expect(BluetoothController.getBluetoothController()).andReturn(bluetoothController);
        PowerMock.replay(BluetoothController.class);

        Mockito.when(bluetoothController.getBluetoothConnectionService()).thenReturn(bluetoothConnectionService);
        createThread();
    }

    /**
     * Discovery is canceled as soon as connect thread starts.
     */
    @Test
    public void testCancelDiscovery() throws InterruptedException {
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothAdapter).cancelDiscovery();
    }

    /**
     * State is already in CONNECTING_VIA_SERVER mode, so connectThread can be ignored completely
     */
    @Test
    public void testIgnoreThread() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothSocket, Mockito.never()).connect();
    }

    /**
     * During connect(), state has changed to something different from LISTEN (probably CONNECTING_VIA_SERVER)
     * so connected socket can be ignored.
     */
    @Test
    public void testIgnoreSocket() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN)
                .thenReturn(BluetoothConstants.STATE_LISTEN)
                .thenReturn(BluetoothConstants.STATE_CONNECTING_VIA_SERVER); // different from STATE_LISTEN
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothSocket).connect();
        Mockito.verify(bluetoothConnectionService, Mockito.times(3)).getConnectionState();
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setConnectionSocket(bluetoothSocket);
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN);
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothSocket).connect();
        Mockito.verify(bluetoothConnectionService).setConnectionSocket(bluetoothSocket);
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_CONNECTING);
    }

    /**
     * Exception is thrown during connect(). State is LISTEN, so connecting to the device wasn't possible.
     */
    @Test
    public void testUnableToConnect() throws InterruptedException, IOException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN);
        Mockito.doThrow(IOException.class).when(bluetoothSocket).connect();
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothSocket).connect();
        Mockito.verify(bluetoothConnectionService, Mockito.times(3)).getConnectionState(); // still returns LISTEN
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_UNABLE_TO_CONNECT);
    }

    /**
     * Exception is thrown during connect(). State is not LISTEN, so exception can be ignored.
     */
    @Test
    public void testExceptionCaught() throws InterruptedException, IOException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN)
                .thenReturn(BluetoothConstants.STATE_LISTEN)
                .thenReturn(BluetoothConstants.STATE_NONE); // different from STATE_LISTEN
        Mockito.doThrow(IOException.class).when(bluetoothSocket).connect();
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothSocket).connect();
        Mockito.verify(bluetoothConnectionService, Mockito.times(3)).getConnectionState();
        // do nothing
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setState(BluetoothConstants.STATE_UNABLE_TO_CONNECT);
    }

    @Test
    public void testThreadClosed() throws InterruptedException {
        connectThread.start();
        waitForThread();
        Mockito.verify(bluetoothConnectionService).onThreadClosed(BluetoothConstants.CONNECT_THREAD);
    }

    @Test
    public void testCancel() throws InterruptedException, IOException {
        connectThread.start();
        connectThread.cancel();
        waitForThread();
        Mockito.verify(bluetoothSocket).close();
    }

    /**
     * Test exception is caught.
     */
    @Test
    public void testCancelException() throws InterruptedException, IOException {
        Mockito.doThrow(IOException.class).when(bluetoothSocket).close();
        connectThread.start();
        connectThread.cancel();
        waitForThread();
        Mockito.verify(bluetoothSocket).close();
    }

    private void createThread () throws IOException {
        Mockito.when(bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BluetoothConstants.UUID))).thenReturn(bluetoothSocket);

        connectThread = new ConnectThread(bluetoothDevice, bluetoothAdapter);
    }

    /**
     * Wait for the thread to be run through / finished
     */
    private void waitForThread() throws InterruptedException {
        while(connectThread.isAlive()) {
            Thread.sleep(100);
        }
    }
}
