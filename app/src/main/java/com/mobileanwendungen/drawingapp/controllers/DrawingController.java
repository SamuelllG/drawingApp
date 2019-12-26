package com.mobileanwendungen.drawingapp.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.bluetooth.BluetoothConnectionService;
import com.mobileanwendungen.drawingapp.constants.BluetoothConstants;
import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;
import com.mobileanwendungen.drawingapp.wrapper.PathsData;
import com.mobileanwendungen.drawingapp.wrapper.SerializablePath;
import com.mobileanwendungen.drawingapp.listener.WidthSeekBarChangeListener;
import com.mobileanwendungen.drawingapp.views.DrawingView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
        Log.d(TAG, "setPaint: set line width");
        currentAlertDialog.dismiss();

        BluetoothConnectionService bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION)
            RemoteHandler.getRemoteHandler().sendMyLineWidth();
    }

    public void clearDrawingView() {
        drawingView.clear(0, 1);
        BluetoothConnectionService bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
        if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION)
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
        PathsData pathsData = new PathsData();
        pathsData.setPaths(drawingView.getPaths(0));
        //wrapper.addToPaths(drawingView.getPathMap(1));
        String serialized = null;// = gson.toJson(wrapper);





        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            try {
                objectMapper.writeValue(fos, pathsData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Use the compress method on the BitMap object to sendEvent image to the OutputStream
            //drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
            ///////fos.sendEvent(serialized.getBytes());
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
            File stored = new File(storagePath, fileName);
            String filePath = storagePath + "/" + fileName;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //Gson gson = new Gson();
            // TARGETAPI 26 String json = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            FileInputStream fis = new FileInputStream(stored);
            byte[] data = new byte[(int) stored.length()];
            fis.read(data);
            fis.close();

            String json = new String(data, "UTF-8");
            //----
            //PathsData wrapper = gson.fromJson(json, PathsData.class);
            //JsonFactory jsonFactory = new JsonFactory();
            //JsonParser jp = jsonFactory.createParser(json);
            //PathsData pathsData = objectMapper.readValue((JsonParser)objectMapper.readTree(jp), PathsData.class);
            PathsData pathsData = objectMapper.readValue(json, PathsData.class);
            List<SerializablePath> paths = pathsData.getPaths();
            for (SerializablePath path : paths) {
                path.recreate();
                path.getPaint().recreate();
            }
            drawingView.clear(0, 1);
            drawingView.setPathMap(paths, 0);
            BluetoothConnectionService bluetoothConnectionService = BluetoothController.getBluetoothController().getBluetoothConnectionService();
            if (bluetoothConnectionService != null && bluetoothConnectionService.getConnectionState() == BluetoothConstants.STATE_VERIFIED_CONNECTION)
                RemoteHandler.getRemoteHandler().sendMyMap(pathsData);

            //Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            //drawingView.setBitmap(b);
            Log.d(TAG, json);
            Toast.makeText(activity, R.string.loaded, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(activity, R.string.error_loading, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void loadRemotePaths(List<SerializablePath> paths) {
        drawingView.clear(1, 0);
        for (SerializablePath path : paths) {
            path.recreate();
            path.getPaint().recreate();
        }
        drawingView.setPathMap(paths, 1);
    }

}
