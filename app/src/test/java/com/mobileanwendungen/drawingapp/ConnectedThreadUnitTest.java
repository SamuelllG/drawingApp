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
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionService bluetoothConnectionService;
    private Handler handler;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Message message;

    @Before
    public void setup() throws IOException {
        PowerMockito.mockStatic(Log.class);

        bluetoothController = PowerMockito.mock(BluetoothController.class);
        bluetoothSocket = PowerMockito.mock(BluetoothSocket.class);
        bluetoothDevice = PowerMockito.mock(BluetoothDevice.class);
        bluetoothAdapter = PowerMockito.mock(BluetoothAdapter.class);
        bluetoothConnectionService = PowerMockito.mock(BluetoothConnectionService.class);
        handler = Mockito.mock(MessageHandler.class);
        inputStream = Mockito.mock(InputStream.class);
        outputStream = Mockito.mock(OutputStream.class);
        message = Mockito.mock(Message.class);


        // mock singleton with PowerMock
        PowerMock.reset();
        PowerMock.mockStatic(BluetoothController.class);
        EasyMock.expect(BluetoothController.getBluetoothController()).andReturn(bluetoothController);
        PowerMock.replay(BluetoothController.class);

        Mockito.when(bluetoothController.getBluetoothConnectionService()).thenReturn(bluetoothConnectionService);
        createThread();


        // Mockito.verify(bluetoothSocket, Mockito.never()).connect();
        // Mockito.verify(bluetoothConnectionService, Mockito.times(3)).getConnectionState();
        // Mockito.verify(bluetoothConnectionService, Mockito.never()).setConnectionSocket(bluetoothSocket);
        // Mockito.doThrow(IOException.class).when(bluetoothSocket).connect();
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
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_CONNECTED);
        connectedThread.start();
        waitForThread();

        // mockito.times --> pay attention to OR in if
        Mockito.verify(bluetoothConnectionService, Mockito.times(2)).getConnectionState();
        byte[] buffer = new byte[1024];
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_VERIFICATION);
        Mockito.verify(inputStream).read(buffer);
    }

    @Test
    public void testVerified() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_VERIFIED_CONNECTION); // or VERIFICATION
        connectedThread.start();
        waitForThread();

        Mockito.verify(inputStream, Mockito.never()).read();
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setState(BluetoothConstants.STATE_VERIFICATION);
    }

    @Test
    public void testRead() throws IOException, InterruptedException {
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_CONNECTED);

        byte[] buffer = new byte[1024];
        int numBytes = 42;
        Mockito.when(inputStream.read(buffer)).thenReturn(numBytes);
        Mockito.when(handler.obtainMessage(BluetoothConstants.MESSAGE_READ, numBytes, -1, buffer)).thenReturn(message);
        connectedThread.start();
        waitForThread();

        Mockito.verify(inputStream).read(buffer);
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_VERIFICATION);
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
