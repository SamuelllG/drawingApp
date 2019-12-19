package com.mobileanwendungen.drawingapp.view;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class DrawingView extends View {

    private static final String TAG = "cust.DrawingView";

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path>[] pathMap;
    private HashMap<Integer, Point>[] previousPointMap;

    private RemoteHandler remoteHandler;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    //TODO: understand DrawingView and create DrawingController?

    private void init() {
        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(7);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap[2];
        previousPointMap = new HashMap[2];

        remoteHandler = RemoteHandler.getRemoteHandler();
        remoteHandler.init(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (int i = 0; i < pathMap.length; i++) {
            for (Integer key : pathMap[i].keySet()) {
                canvas.drawPath(pathMap[i].get(key), paintLine);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked(); // event type
        int actionIndex = event.getActionIndex(); // pointer ... finger, mouse,..

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex), 0);
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event.getPointerId(actionIndex), 0);
        }
        else {
            touchMoved(event, 0);
        }

        invalidate(); // redraw screen

        // -------
        remoteHandler.write(event);

        return true;
    }

    public boolean onRemoteTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // event type
        int actionIndex = event.getActionIndex(); // pointer ... finger, mouse,..

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex), 1);
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event.getPointerId(actionIndex), 1);
        }
        else {
            touchMoved(event, 1);
        }
        invalidate(); // redraw screen
        return true;
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    public void clear() {
        // clear just own points although screen is erased (?)
        pathMap[0].clear();
        previousPointMap[0].clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate(); // refresh
    }

    private void touchStarted(float x, float y, int pointerId, int user) {
        Path path; // store the path for given touch
        Point point; //store the last point in path

        if (pathMap[user].containsKey(pointerId)) {
            path = pathMap[user].get(pointerId);
            point = previousPointMap[user].get(pointerId);
        } else {
            path = new Path();
            pathMap[user].put(pointerId, path);
            point = new Point();
            previousPointMap[user].put(pointerId, point);
        }

        // move to coord of the touch
        path.moveTo(x,y);
        point.x = (int) x;
        point.y = (int) y;

    }

    private void touchMoved(MotionEvent event, int user) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            // loop necessary?
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathMap[user].containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap[user].get(pointerId);
                Point point = previousPointMap[user].get(pointerId);

                // calculate how far user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    //if distance is big enough to be considered

                    // move path to new location
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);
                    //path.quadTo(point.x, point.y, newX, newY);

                    // store new coords
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    private void touchEnded(int pointerId, int user) {
        Path path = pathMap[user].get(pointerId); // get corresponding path
        bitmapCanvas.drawPath(path, paintLine); // draw to bitmapCanvas
        path.reset();
    }

    public Bitmap getBitmap () {
        return bitmap;
    }

    public void setBitmap (Bitmap bitmap) {
        clear();
        this.bitmap = Bitmap.createBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        bitmapCanvas = new Canvas(this.bitmap);
        Log.d(TAG, "setBitmap: set bitmap successfully");
    }

}

