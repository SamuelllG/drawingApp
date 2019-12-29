package com.mobileanwendungen.drawingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.bluetooth.MessageHandler;
import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;
import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.threads.AcceptThread;
import com.mobileanwendungen.drawingapp.threads.ConnectedThread;
import com.mobileanwendungen.drawingapp.utils.UIHelper;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.verification.PrivateMethodVerification;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BluetoothConnectionService.class, Log.class, BluetoothAdapter.class})
public class BluetoothConnectionServiceTest {

    private ConnectedThread connectedThread;
    private BluetoothController bluetoothController;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionService bluetoothConnectionService;
    private AcceptThread acceptThread;
    private MessageHandler messageHandler;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Message message;
    private UIHelper uiHelper;
    private BluetoothDevices bluetoothDevices;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(Log.class);

        bluetoothController = PowerMockito.mock(BluetoothController.class);
        bluetoothSocket = PowerMockito.mock(BluetoothSocket.class);
        bluetoothDevice = PowerMockito.mock(BluetoothDevice.class);
        bluetoothAdapter = PowerMockito.mock(BluetoothAdapter.class);
        messageHandler = Mockito.mock(MessageHandler.class);
        acceptThread = Mockito.mock(AcceptThread.class);
        inputStream = Mockito.mock(InputStream.class);
        outputStream = Mockito.mock(OutputStream.class);
        uiHelper = Mockito.mock(UIHelper.class);
        connectedThread = Mockito.mock(ConnectedThread.class);
        bluetoothDevices = Mockito.mock(BluetoothDevices.class);


        PowerMockito.whenNew(MessageHandler.class)
                .withAnyArguments()
                .thenReturn(messageHandler);


        // mock with PowerMock
        PowerMock.reset();
        PowerMock.mockStatic(BluetoothAdapter.class);
        EasyMock.expect(BluetoothAdapter.getDefaultAdapter()).andReturn(bluetoothAdapter);
        PowerMock.replay(BluetoothAdapter.class);
        bluetoothConnectionService = Mockito.spy(new BluetoothConnectionService(bluetoothController, uiHelper));
    }

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(AcceptThread.class)
                .withArguments(bluetoothAdapter)
                .thenReturn(acceptThread);
        bluetoothConnectionService.start();
        Thread.sleep(200);
        Assert.assertEquals(BluetoothConstants.STATE_NONE, bluetoothConnectionService.getConnectionState());
        PowerMockito.verifyNew(AcceptThread.class).withArguments(bluetoothAdapter);
        Mockito.verify(acceptThread).start();
    }

    @Test
    public void testListen() throws InterruptedException {
        bluetoothConnectionService.start();
        Thread.sleep(200);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_LISTEN);
        synchronized (bluetoothConnectionService) {
            Mockito.verify(bluetoothConnectionService).notifyAll();
        }
    }

    @Test
    public void testConnecting() throws Exception {
        PowerMockito.whenNew(ConnectedThread.class)
                .withArguments(bluetoothSocket, messageHandler)
                .thenReturn(connectedThread);
        bluetoothConnectionService.start();
        Thread.sleep(200);
        bluetoothConnectionService.setConnectionSocket(bluetoothSocket);
        //bluetoothConnectionService.setState(BluetoothConstants.STATE_NONE);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_CONNECTING);
        //PrivateMethodVerification pmv = PowerMockito.verifyPrivate(bluetoothConnectionService);
        //pmv.invoke("stopAcceptThread");
        //pmv.invoke("startCommunication", bluetoothSocket);
        //PowerMockito.verifyNew(ConnectedThread.class, Mockito.times(2)).withArguments(bluetoothSocket, messageHandler); // 2 because captor and pmv not working together not working as intended (should be as in testFailed)
        Mockito.verify(bluetoothConnectionService, Mockito.times(1)).setState(stateCaptor.capture());
        List<Integer> states = stateCaptor.getAllValues();
        Assert.assertEquals(BluetoothConstants.STATE_CONNECTING, (int) states.get(0));
        //Assert.assertEquals(BluetoothConstants.STATE_CONNECTED, (int) states.get(1));
    }

    @Test
    public void testConnectingViaServer() throws Exception {
        PowerMockito.whenNew(ConnectedThread.class)
                .withArguments(bluetoothSocket, messageHandler)
                .thenReturn(connectedThread);
        Mockito.when(bluetoothSocket.getRemoteDevice()).thenReturn(bluetoothDevice);
        bluetoothConnectionService.start();
        Thread.sleep(200);
        bluetoothConnectionService.setConnectionSocket(bluetoothSocket);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_NONE);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_CONNECTING_VIA_SERVER);
        //PrivateMethodVerification pmv = PowerMockito.verifyPrivate(bluetoothConnectionService);
        //pmv.invoke("stopConnectThread");
        //pmv.invoke("startCommunication", bluetoothSocket);
        //PowerMockito.verifyNew(ConnectedThread.class, Mockito.times(1)).withArguments(bluetoothSocket, messageHandler);
        Mockito.verify(bluetoothConnectionService, Mockito.times(2)).setState(stateCaptor.capture());
        List<Integer> states = stateCaptor.getAllValues();
        Assert.assertEquals(BluetoothConstants.STATE_NONE, (int) states.get(0));
        Assert.assertEquals(BluetoothConstants.STATE_CONNECTING_VIA_SERVER, (int) states.get(1));
        //Assert.assertEquals(BluetoothConstants.STATE_INIT_RESTART, (int) states.get(2));
        //Mockito.verify(connectedThread).start();
    }

    @Test
    public void testVerification() throws Exception {
        bluetoothConnectionService.start();
        Thread.sleep(200);
        //bluetoothConnectionService.setState(BluetoothConstants.STATE_NONE);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_VERIFICATION);
        PrivateMethodVerification pmv = PowerMockito.verifyPrivate(bluetoothConnectionService);
        pmv.invoke("request", BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION);
        pmv.invoke("waitForResponse");
        Mockito.verify(bluetoothConnectionService, Mockito.times(1)).write(BluetoothConstants.REQUEST_ESTABLISHED_CONNECTION.getBytes());
    }

    @Test
    public void testVerifiedConnection() throws InterruptedException {
        bluetoothConnectionService.start();
        Thread.sleep(200);
        //bluetoothConnectionService.setState(BluetoothConstants.STATE_NONE);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_VERIFIED_CONNECTION);
        //Mockito.verify(bluetoothController).onEstablishedConnection();
    }

    @Captor
    ArgumentCaptor<Integer> stateCaptor;

    @Test
    public void testFailed() throws Exception {
        PowerMockito.whenNew(AcceptThread.class)
                .withArguments(bluetoothAdapter)
                .thenReturn(acceptThread);
        Mockito.when(bluetoothController.getBluetoothDevices()).thenReturn(bluetoothDevices);

        bluetoothConnectionService.start();
        Thread.sleep(200);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_NONE);
        bluetoothConnectionService.setState(BluetoothConstants.STATE_FAILED);
        //Mockito.verify(bluetoothController).getBluetoothDevices();
        //Mockito.verify(bluetoothDevices).clearConnected();
        //Mockito.verify(bluetoothController).updateUI();

        //PrivateMethodVerification pmv = PowerMockito.verifyPrivate(bluetoothConnectionService);
        //pmv.invoke("closeAll");
        //pmv.invoke("initRestart");
        Mockito.verify(bluetoothConnectionService, Mockito.times(3)).setState(stateCaptor.capture());
        List<Integer> states = stateCaptor.getAllValues();
        Assert.assertEquals(BluetoothConstants.STATE_NONE, (int) states.get(0));
        Assert.assertEquals(BluetoothConstants.STATE_FAILED, (int) states.get(1));
        Assert.assertEquals(BluetoothConstants.STATE_INIT_RESTART, (int) states.get(2));
        //Assert.assertEquals(BluetoothConstants.STATE_RESTARTING, (int) states.get(3)); // skip private method for now because pmv doesn't work

        PowerMockito.verifyNew(AcceptThread.class, Mockito.times(1)).withArguments(bluetoothAdapter);
        Mockito.verify(acceptThread).start();
    }



    /**
     * Wait for the thread to be run through / finished
     */
    private void waitForThread() throws InterruptedException {
        while(bluetoothConnectionService.isAlive()) {
            Thread.sleep(100);
        }
    }
}
