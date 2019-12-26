package com.mobileanwendungen.drawingapp.bluetooth.Threads;

import android.util.Log;

import com.mobileanwendungen.drawingapp.view.DrawingView;

import java.util.concurrent.TimeUnit;

public class RefresherThread extends Thread {
    private static final String TAG = "cust.Refresher";

    private final DrawingView drawingView;

    public RefresherThread(DrawingView drawingView) {
        this.drawingView = drawingView;
    }

    @Override
    public void run() {
        try {
            while (true) {
                TimeUnit.MILLISECONDS.sleep(1000);
                //Log.d(TAG, "mep");
                drawingView.drawAll(0);
                drawingView.drawAll(1);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
