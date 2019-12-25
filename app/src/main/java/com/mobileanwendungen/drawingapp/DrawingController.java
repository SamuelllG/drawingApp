package com.mobileanwendungen.drawingapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;
import com.mobileanwendungen.drawingapp.utilities.MapWrapper;
import com.mobileanwendungen.drawingapp.utilities.SerializablePath;
import com.mobileanwendungen.drawingapp.utilities.WidthSeekBarChangeListener;
import com.mobileanwendungen.drawingapp.view.DrawingView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DrawingController {
    private static final String TAG = "cust.DrawingController";
    private static DrawingController DRAWING_CONTROLLER;

    private Activity activity;
    private DrawingView drawingView;
    private WidthSeekBarChangeListener widthSeekBarChangeListener;
    //private AlertDialog.Builder currentDialogBuilder;
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
        widthSeekBar.setProgress(drawingView.getLineWidth(0));
        widthSeekBarChangeListener.onProgressChanged(widthSeekBar, drawingView.getLineWidth(0), false);

        Log.d(TAG, "showLineWidthDialog: show dialog");
        currentDialogBuilder.setView(view);
        currentAlertDialog = currentDialogBuilder.create();
        currentAlertDialog.show();
    }


    public void setLineWidth(View view) {
        // get dialog from button
        View dialog = view.getRootView();
        SeekBar seekBar = dialog.findViewById(R.id.widthSeekBar);
        int width = seekBar.getProgress();
        drawingView.setLineWidth(0, width);
        Log.d(TAG, "setLineWidth: set line width");
        currentAlertDialog.dismiss();
        RemoteHandler.getRemoteHandler().sendMyLineWidth();
    }

    public void clearDrawingView() {
        drawingView.clear(0, 1);
        RemoteHandler.getRemoteHandler().notifyClear();
    }

    /**
     * https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
     */
    public void saveToInternalStorage() {
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File path = new File(directory, fileName);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //Gson gson = new Gson();
        MapWrapper wrapper = new MapWrapper();
        wrapper.setMap(drawingView.getPathMap(0));
        //wrapper.addToMap(drawingView.getPathMap(1));
        String serialized = null;// = gson.toJson(wrapper);





        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            try {
                objectMapper.writeValue(fos, wrapper);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Use the compress method on the BitMap object to write image to the OutputStream
            //drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
            ///////fos.write(serialized.getBytes());
            ///////fos.flush();
            //Log.d(TAG, serialized);
            Toast.makeText(activity, R.string.saved, Toast.LENGTH_LONG).show();
/*
            String storagePath = directory.getAbsolutePath();
            String pathConstant = activity.getResources().getString(R.string.STORAGE_PATH);
            if(!storagePath.equals(pathConstant))
                Log.d(TAG, "saveToInternalStorage: error while saving, path is different from declared constant");
            else*/
                Log.d(TAG, "saveToInternalStorage: saved to " + storagePath);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "saveToInternalStorage: error while saving");
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void loadFromStorage() {
        try {
            Log.d(TAG, "loadFromStorage: load from " + storagePath);
            //File stored = new File(storagePath, fileName);
            String path = storagePath + "/" + fileName;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //Gson gson = new Gson();
            String json = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            //MapWrapper wrapper = gson.fromJson(json, MapWrapper.class);
            MapWrapper wrapper = objectMapper.readValue(json, MapWrapper.class);
            HashMap<Integer, SerializablePath> pathMap = wrapper.getMap();
            for (int key : pathMap.keySet()) {
                SerializablePath serializablePath = wrapper.getMap().get(key);
                serializablePath.recreate();
            }
            drawingView.setPathMap(pathMap, 0);

            //Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            //drawingView.setBitmap(b);
            Log.d(TAG, json);
            Toast.makeText(activity, R.string.loaded, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(activity, R.string.error_loading, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
