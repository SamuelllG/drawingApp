package com.mobileanwendungen.drawingapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.mobileanwendungen.drawingapp.CustomMotionEvent;
import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;
import com.mobileanwendungen.drawingapp.utilities.SerializablePath;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DrawingView extends View {

    private static final String TAG = "cust.DrawingView";

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint[] paintLine;
    private HashMap<Integer, SerializablePath>[] pathMap;
    private HashMap<Integer, Point>[] previousPointMap;
    private int[] currentPathPointer;

    private RemoteHandler remoteHandler;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //TODO: understand DrawingView and create DrawingController?

    private void init() {
        paintScreen = new Paint();
        paintLine = new Paint[2];

        for (int i = 0; i < paintLine.length; i++) {
            paintLine[i] = new Paint();
            paintLine[i].setAntiAlias(true);
            paintLine[i].setColor(Color.BLACK);
            paintLine[i].setStyle(Paint.Style.STROKE);
            paintLine[i].setStrokeCap(Paint.Cap.ROUND);
        }
        paintLine[0].setStrokeWidth(7);

        pathMap = new HashMap[2];
        previousPointMap = new HashMap[2];
        for (int i=0; i < 2; i++) {
            pathMap[i] = new HashMap<>();
            previousPointMap[i] = new HashMap<>();
        }
        currentPathPointer = new int[2];

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
        // this is temporary (for achieving the "live"-drawing effect
        //drawCurrent(0);
        //drawCurrent(1);
        drawAll(0);
        drawAll(1);
    }

    private void drawCurrent(int user) {
        Path path = pathMap[user].get(currentPathPointer[user]);
        if (path != null)
            bitmapCanvas.drawPath(path, paintLine[user]);
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
            int pointerIndex = 0;
            int pointerId = event.getPointerId(pointerIndex);
            float newX = event.getX(pointerIndex);
            float newY = event.getY(pointerIndex);
            touchMoved(newX, newY, pointerId, 0);
        }

        invalidate(); // redraw screen

        // -------
        remoteHandler.process(event);

        return true;
    }

    public boolean onRemoteTouchEvent(CustomMotionEvent event) {
        int action = event.getAction();
        //int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(), event.getY(), event.getActionPointerId(), 1);
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event.getActionPointerId(), 1);
        }
        else {
            touchMoved(event.getNewX(), event.getNewY(), event.getPointerId(), 1);
        }
        invalidate(); // redraw screen
        return true;
    }

    private void touchStarted(float x, float y, int pointerId, int user) {
        SerializablePath path; // store the path for given touch
        Point point; //store the last point in path

        if (pathMap[user].containsKey(pointerId)) {
            path = pathMap[user].get(pointerId);
            point = previousPointMap[user].get(pointerId);
        } else {
            path = new SerializablePath();
            pathMap[user].put(pointerId, path);
            point = new Point();
            previousPointMap[user].put(pointerId, point);
        }

        // move to coord of the touch
        path.moveTo(x,y);
        point.x = (int) x;
        point.y = (int) y;

        currentPathPointer[user] = pointerId;
    }

    private void touchMoved(float newX, float newY, int pointerId, int user) {

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

    /**
     * Touch ended --> write everything to a persistent canvas
     */
    private void touchEnded(int pointerId, int user) {
        Path path = pathMap[user].get(pointerId); // get corresponding path
        bitmapCanvas.drawPath(path, paintLine[user]); // draw to bitmapCanvas
        //path.reset();
    }

    public void setDrawingColor(int user, int color) {
        paintLine[user].setColor(color);
    }

    public int getDrawingColor(int user) {
        return paintLine[user].getColor();
    }

    public void setLineWidth(int user, int width) {
        paintLine[user].setStrokeWidth(width);
    }

    public int getLineWidth(int user) {
        return (int) paintLine[user].getStrokeWidth();
    }

    public void clear(int user, int keep) {
        pathMap[user].clear();
        previousPointMap[user].clear();
        bitmap.eraseColor(Color.WHITE);
        drawAll(keep);
        invalidate(); // refresh
    }

    private void drawAll(int user) {
        for (int pointer : pathMap[user].keySet()) {
            SerializablePath path = pathMap[user].get(pointer);
            bitmapCanvas.drawPath(path, paintLine[user]);
        }
    }

    public void setPathMap(HashMap<Integer, SerializablePath> map, int user) {
        pathMap[user] = map;
        //pathMap[user].get(0).recreate();
        previousPointMap[0].put(0, new Point());
        //drawAll(user);
        invalidate();
    }

    public Bitmap getBitmap () {
        return bitmap;
    }

    public Paint getPaintLine(int user) {
        return paintLine[user];
    }

    public HashMap<Integer, SerializablePath> getPathMap(int user) {
        return pathMap[user];
    }

    public void setBitmap (Bitmap bitmap) {
        clear(0, 1);
        this.bitmap = Bitmap.createBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        bitmapCanvas = new Canvas(this.bitmap);
        Log.d(TAG, "setBitmap: set bitmap successfully");
    }

}

