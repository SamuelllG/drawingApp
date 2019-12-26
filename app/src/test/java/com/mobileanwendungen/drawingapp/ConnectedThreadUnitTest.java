package com.mobileanwendungen.drawingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.bluetooth.MessageHandler;
import com.mobileanwendungen.drawingapp.threads.ConnectThread;
import com.mobileanwendungen.drawingapp.threads.ConnectedThread;
import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectThread.class, Log.class, BluetoothController.class})
public class ConnectedThreadUnitTest {
    private ConnectedThread connectedThread;
    private BluetoothController bluetoothController;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothConnectionService bluetoothConnectionService;
    private Handler handler;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Before
    public void setup() throws IOException {
        PowerMockito.mockStatic(Log.class);

        bluetoothController = PowerMockito.mock(BluetoothController.class);
        bluetoothSocket = PowerMockito.mock(BluetoothSocket.class);
        bluetoothDevice = PowerMockito.mock(BluetoothDevice.class);
        bluetoothConnectionService = PowerMockito.mock(BluetoothConnectionService.class);
        handler = Mockito.mock(MessageHandler.class);
        inputStream = Mockito.mock(InputStream.class);
        outputStream = Mockito.mock(OutputStream.class);


        // mock singleton with PowerMock
        PowerMock.reset();
        PowerMock.mockStatic(BluetoothController.class);
        EasyMock.expect(BluetoothController.getBluetoothController()).andReturn(bluetoothController);
        PowerMock.replay(BluetoothController.class);

        Mockito.when(bluetoothController.getBluetoothConnectionService()).thenReturn(bluetoothConnectionService);
        createThread();
    }

    @Test
    public void testNotConnected() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_NONE); // different from CONNECTED, VERIFICATION, VERIFIED_CONNECTION
        connectedThread.start();
        waitForThread();

        Mockito.verify(bluetoothConnectionService, Mockito.times(3)).getConnectionState();
        Mockito.verify(inputStream, Mockito.never()).read();
        Mockito.verify(bluetoothConnectionService).onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
    }

    @Test
    public void testVerification() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_CONNECTED).thenReturn(BluetoothConstants.STATE_CONNECTED).thenReturn(BluetoothConstants.STATE_NONE);
        Mockito.when(inputStream.read(new byte[4096], 0, 1)).thenReturn(1);
        connectedThread.start();
        waitForThread();

        // mockito.times --> pay attention to OR in if
        Mockito.verify(bluetoothConnectionService, Mockito.times(5)).getConnectionState();
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_VERIFICATION);
    }

    @Test
    public void testRead() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_CONNECTED).thenReturn(BluetoothConstants.STATE_CONNECTED).thenReturn(BluetoothConstants.STATE_NONE);
        Mockito.when(inputStream.read(new byte[4096], 0, 1)).thenReturn(1);
        connectedThread.start();
        waitForThread();

        Mockito.verify(bluetoothConnectionService, Mockito.times(5)).getConnectionState();
        Mockito.verify(inputStream).read(new byte[4096], 0, 1);
    }

    @Test
    public void testIsSeparator() {
        byte[] bytes = new byte[] { (byte) 120, (byte) 42, (byte) 120, (byte) 42, (byte) 120, (byte) 42 };
        Assert.assertFalse(connectedThread.testIsSeparator(bytes, 5));
        Assert.assertFalse(connectedThread.testIsSeparator(BluetoothConstants.SEPARATOR, 5));
        bytes = new byte[] { (byte) 122, (byte) 120, (byte) 42, (byte) 120, (byte) 42, (byte) 120, (byte) 42 };
        Assert.assertFalse(connectedThread.testIsSeparator(bytes, 6));
        bytes = new byte[] { (byte) 122, 0, 0, 0, 0, 0, 0 };
        System.arraycopy(BluetoothConstants.SEPARATOR, 0, bytes, 1, BluetoothConstants.SEPARATOR.length);
        Assert.assertTrue(connectedThread.testIsSeparator(bytes, 6));
        bytes = new byte[] { (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, 0, 0, 0, 0, 0, 0 };
        System.arraycopy(BluetoothConstants.SEPARATOR, 0, bytes, 5, BluetoothConstants.SEPARATOR.length);
        Assert.assertTrue(connectedThread.testIsSeparator(bytes, 10));
    }

    @Test
    public void testInterrupted() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION);
        Mockito.doThrow(IOException.class).when(inputStream).read(new byte[4096], 0, 1);
        connectedThread.start();
        waitForThread();

        Mockito.verify(bluetoothConnectionService, Mockito.times(6)).getConnectionState();
        Mockito.verify(inputStream).read(new byte[4096], 0, 1);
        Mockito.verify(bluetoothConnectionService).onThreadClosed(BluetoothConstants.CONNECTED_THREAD);
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_INTERRUPTED);
    }

    @Test
    public void testLateClose() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION)
                .thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION)
                .thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION)
                .thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION)
                .thenReturn(BluetoothConstants.STATE_CLOSING);
        Mockito.doThrow(IOException.class).when(inputStream).read(new byte[4096], 0, 1);
        connectedThread.start();
        waitForThread();

        Mockito.verify(bluetoothConnectionService, Mockito.times(8)).getConnectionState();
        Mockito.verify(inputStream).read(new byte[4096], 0, 1);
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setState(BluetoothConstants.STATE_INTERRUPTED);
    }

    @Test
    public void testResizeArray() {
        byte[] array = new byte[4096];
        Assert.assertEquals(array.length*2, connectedThread.testResizeArray(array).length);
    }

    @Test
    public void testGetRemoteDevice() {
        Mockito.when(bluetoothSocket.getRemoteDevice()).thenReturn(bluetoothDevice);
        Assert.assertEquals(bluetoothDevice, connectedThread.getRemoteDevice());
    }

    @Test
    public void testAppendSeparator() {
        byte[] buffer = new byte[] { (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94 };
        byte[] bytes = new byte[] { (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, 0, 0, 0, 0, 0, 0 };
        System.arraycopy(BluetoothConstants.SEPARATOR, 0, bytes, 10, 6);
        byte[] testBytes = connectedThread.testAppendSeparator(buffer);
        Assert.assertArrayEquals(bytes, testBytes);
    }

    @Test
    public void testWrite() throws IOException {
        byte[] buffer = new byte[] { (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94 };
        byte[] bytes = new byte[] { (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, (byte) 122, (byte) 2, (byte) -43, (byte) 55, (byte) 94, 0, 0, 0, 0, 0, 0 };
        System.arraycopy(BluetoothConstants.SEPARATOR, 0, bytes, 10, 6);
        connectedThread.write(buffer);
        Mockito.verify(outputStream).write(bytes);
    }


    private void createThread () throws IOException {
        Mockito.when(bluetoothSocket.getInputStream()).thenReturn(inputStream);
        Mockito.when(bluetoothSocket.getOutputStream()).thenReturn(outputStream);

        connectedThread = new ConnectedThread(bluetoothSocket, handler);
    }

    /**
     * Wait for the thread to be run through / finished
     */
    private void waitForThread() throws InterruptedException {
        while(connectedThread.isAlive()) {
            Thread.sleep(100);
        }
    }
}
