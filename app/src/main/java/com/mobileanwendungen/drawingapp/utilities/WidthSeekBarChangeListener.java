package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.mobileanwendungen.drawingapp.R;
import com.mobileanwendungen.drawingapp.view.DrawingView;

public class WidthSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "cust.WidthSeekBarChLs";

    private Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bitmap);
    private DrawingView drawingView;

    public WidthSeekBarChangeListener (DrawingView drawingView) {
        this.drawingView = drawingView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: set line width to " + progress);
        Paint p = new Paint();
        p.setColor(drawingView.getDrawingColor());
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(progress);

        bitmap.eraseColor(Color.TRANSPARENT); // WHITE
        canvas.drawLine(30, 50, 370, 50, p);
        View view = seekBar.getRootView().findViewById(R.id.imageViewId);
        ImageView widthImageView = view.findViewById(R.id.imageViewId);
        widthImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
