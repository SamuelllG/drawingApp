package com.mobileanwendungen.drawingapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.mobileanwendungen.drawingapp.bluetooth.BluetoothController;
import com.mobileanwendungen.drawingapp.utilities.WidthSeekBarChangeListener;
import com.mobileanwendungen.drawingapp.view.DrawingView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DrawingController {
    private static final String TAG = "cust.DrawingController";
    private static DrawingController DRAWING_CONTROLLER;

    private Activity activity;
    private DrawingView drawingView;
    private WidthSeekBarChangeListener widthSeekBarChangeListener;
    //private AlertDialog.Builder currentDialogBuilder; //TODO not necessary as field?
    private AlertDialog currentAlertDialog;
    private String storagePath; // final
    private String fileName; // final

    private DrawingController() {
        //
    }

    /**
     * Singleton
     */
    public static DrawingController getDrawingController() {
        if (DRAWING_CONTROLLER == null) {
            DRAWING_CONTROLLER = new DrawingController();
        }
        return DRAWING_CONTROLLER;
    }

    public void init(Activity activity) {
        this.activity = activity;
        drawingView = activity.findViewById(R.id.view);
        widthSeekBarChangeListener = new WidthSeekBarChangeListener(drawingView);
        storagePath = activity.getResources().getString(R.string.STORAGE_PATH);
        fileName = activity.getResources().getString(R.string.FILENAME);
    }


    public void showLineWidthDialog() {
        AlertDialog.Builder currentDialogBuilder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.width_dialog, null);
        SeekBar widthSeekBar = view.findViewById(R.id.widthSeekBar);
        //Button setLineWidthButton = view.findViewById(R.id.widthDialogButton);
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChangeListener);
        // setup visuals before first use
        widthSeekBar.setProgress(drawingView.getLineWidth());
        widthSeekBarChangeListener.onProgressChanged(widthSeekBar, drawingView.getLineWidth(), false);

        Log.d(TAG, "showLineWidthDialog: show dialog");
        currentDialogBuilder.setView(view);
        currentAlertDialog = currentDialogBuilder.create();
        currentAlertDialog.show();
    }


    public void setLineWidth(View view) {
        // get dialog from button
        View dialog = view.getRootView();
        SeekBar seekBar = dialog.findViewById(R.id.widthSeekBar);
        drawingView.setLineWidth(seekBar.getProgress());
        Log.d(TAG, "setLineWidth: set line width");
        currentAlertDialog.dismiss();
    }



    public void clearDrawingView() {
        drawingView.clear();
    }

    /**
     * https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
     *
     * @return
     */
    public void saveToInternalStorage() {
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File path = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show();

            String storagePath = directory.getAbsolutePath();
            String pathConstant = activity.getResources().getString(R.string.STORAGE_PATH);
            if(!storagePath.equals(pathConstant))
                Log.d(TAG, "saveToInternalStorage: error while saving, path is different from declared constant");
            else
                Log.d(TAG, "saveToInternalStorage: saved to " + storagePath);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "saveToInternalStorage: error while saving");
            Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    // TODO: all toast strings to res

    public void loadFromStorage() {
        try {
            Log.d(TAG, "loadFromStorage: load from " + storagePath);
            File f = new File(storagePath, fileName);

            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            drawingView.setBitmap(b);

            Toast.makeText(activity, "Loaded", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(activity, "Error! Could not be loaded", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

}
