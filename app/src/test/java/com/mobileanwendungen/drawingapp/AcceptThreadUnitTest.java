package com.mobileanwendungen.drawingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.threads.AcceptThread;
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
import java.util.UUID;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AcceptThread.class, Log.class, BluetoothController.class})
public class AcceptThreadUnitTest {

    private AcceptThread acceptThread;
    private BluetoothController bluetoothController;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothSocket bluetoothSocket;
    private BluetoothConnectionService bluetoothConnectionService;
    //private Log log;

    @Before
    public void setup() throws IOException, InterruptedException {
        PowerMockito.mockStatic(Log.class);

        bluetoothController = PowerMockito.mock(BluetoothController.class);
        bluetoothAdapter = PowerMockito.mock(BluetoothAdapter.class);
        bluetoothServerSocket = PowerMockito.mock(BluetoothServerSocket.class);
        bluetoothSocket = PowerMockito.mock(BluetoothSocket.class);
        bluetoothConnectionService = PowerMockito.mock(BluetoothConnectionService.class);

        // mock singleton with PowerMock
        PowerMock.reset();
        PowerMock.mockStatic(BluetoothController.class);
        EasyMock.expect(BluetoothController.getBluetoothController()).andReturn(bluetoothController);
        PowerMock.replay(BluetoothController.class);


        createThread();
/*
        when(bluetoothServerSocket.accept()).thenReturn(new BluetoothSocket());
        when(bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothConstants.APP_NAME, UUID.fromString(BluetoothConstants.UUID))).thenReturn(bluetoothServerSocket);
        acceptThread = Mockito.spy(new AcceptThread(bluetoothAdapter));*/
    }



    /**
     * Should set state to listening at first, then listen on server socket.
     */
    @Test
    public void testListens() throws IOException, InterruptedException {
        Mockito.when(bluetoothServerSocket.accept()).thenReturn(bluetoothSocket);
        acceptThread.start();
        waitForThread();
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_LISTEN);
        Mockito.verify(bluetoothServerSocket).accept();
    }

    @Test
    public void testConnectingViaServer() throws IOException, InterruptedException {
        Mockito.when(bluetoothServerSocket.accept()).thenReturn(bluetoothSocket);
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN);
        acceptThread.start();
        waitForThread();
        Mockito.verify(bluetoothConnectionService).setConnectionSocket(bluetoothSocket);
        Mockito.verify(bluetoothConnectionService).setState(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
    }

    @Test
    public void testIgnore() throws IOException, InterruptedException {
        Mockito.when(bluetoothServerSocket.accept()).thenReturn(bluetoothSocket);
        // return any state != STATE_LISTEN
        Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_NONE);
        acceptThread.start();
        waitForThread();
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setState(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
        Mockito.verify(bluetoothConnectionService, Mockito.never()).setConnectionSocket(any());
        //Mockito.verifyNoMoreInteractions(bluetoothConnectionService);
    }

    @Test
    public void testThreadClosed() throws InterruptedException, IOException {
        //Mockito.when(bluetoothServerSocket.accept()).thenReturn(bluetoothSocket);
        //Mockito.when(bluetoothConnectionService.getConnectionState()).thenReturn(BluetoothConstants.STATE_LISTEN);
        acceptThread.start();
        waitForThread();
        Mockito.verify(bluetoothConnectionService).onThreadClosed(BluetoothConstants.ACCEPT_THREAD);
    }

    @Test
    public void testCancel() throws InterruptedException, IOException {
        acceptThread.start();
        acceptThread.cancel();
        waitForThread();
        Mockito.verify(bluetoothServerSocket).close();
    }

    /**
     * Test exception is caught.
     */
    @Test
    public void testCancelException() throws InterruptedException, IOException {
        Mockito.doThrow(IOException.class).when(bluetoothServerSocket).close();
        acceptThread.start();
        acceptThread.cancel();
        waitForThread();
        Mockito.verify(bluetoothServerSocket).close();
    }


    private void createThread () throws IOException {
        Mockito.when(bluetoothController.getBluetoothConnectionService()).thenReturn(bluetoothConnectionService);
        Mockito.when(bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothConstants.APP_NAME, UUID.fromString(BluetoothConstants.UUID))).thenReturn(bluetoothServerSocket);

        acceptThread = new AcceptThread(bluetoothAdapter);
    }

    /**
     * Wait for the thread to be run through / finished
     */
    private void waitForThread() throws InterruptedException {
        while(acceptThread.isAlive()) {
            Thread.sleep(100);
        }
    }

}
