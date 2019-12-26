package com.mobileanwendungen.drawingapp.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.controllers.DrawingController;
import com.mobileanwendungen.drawingapp.views.BluetoothActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "cust.MainActivity";

    private DrawingController drawingController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingController = DrawingController.getDrawingController();
        drawingController.init(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clear:
                drawingController.clearDrawingView();
                break;
            case R.id.bluetoothConnect:
                startBluetoothActivity();
                break;
            case R.id.save:
                drawingController.saveToInternalStorage();
                break;
            case R.id.load:
                drawingController.loadFromStorage();
                break;
            case R.id.color:
                break;
            case R.id.lineWidth:
                drawingController.showLineWidthDialog();
                break;
            case R.id.erase:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startBluetoothActivity() {
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    public void onClickLineWidthButton(View view) {
        Log.d(TAG, "onClickLineWidthButton: set clicked");
        drawingController.setLineWidth(view);
    }


}
