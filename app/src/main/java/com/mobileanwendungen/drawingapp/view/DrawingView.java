package com.mobileanwendungen.drawingapp.view;

import android.app.Activity;
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
import com.mobileanwendungen.drawingapp.DrawingController;
import com.mobileanwendungen.drawingapp.MainActivity;
import com.mobileanwendungen.drawingapp.bluetooth.RemoteHandler;
import com.mobileanwendungen.drawingapp.bluetooth.Threads.RefresherThread;
import com.mobileanwendungen.drawingapp.utilities.PathPaint;
import com.mobileanwendungen.drawingapp.utilities.SerializablePath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DrawingView extends View {

    private static final String TAG = "cust.DrawingView";

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private PathPaint[] paintLine;
    private List<SerializablePath>[] paths;
    private Point[] previousPoint;
    private boolean[] isDrawing;

    private RemoteHandler remoteHandler;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintScreen = new Paint();
        paintLine = new PathPaint[2];
        isDrawing = new boolean[2];

        for (int i = 0; i < paintLine.length; i++) {
            paintLine[i] = new PathPaint();
            paintLine[i].setAntiAlias(true);
            paintLine[i].setColor(Color.BLACK);
            paintLine[i].setStyle(Paint.Style.STROKE);
            paintLine[i].setStrokeCap(Paint.Cap.ROUND);
        }
        paintLine[0].setStrokeWidth(7);

        paths = new ArrayList[2];
        previousPoint = new Point[2];
        for (int i=0; i < 2; i++) {
            paths[i] = new ArrayList<>();
            previousPoint[i] = new Point();
        }

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
        if (isDrawing[0])
            drawCurrent(0);
        if (isDrawing[1])
            drawCurrent(1);
    }

    private void drawCurrent(int user) {
        Path path = paths[user].get(paths[user].size()-1);
        if (path != null)
            bitmapCanvas.drawPath(path, paintLine[user]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked(); // event type
        int actionIndex = event.getActionIndex(); // pointer ... finger, mouse,..

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex),0);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(0);
        } else if (action == MotionEvent.ACTION_MOVE){
            float newX = event.getX(0);
            float newY = event.getY(0);
            touchMoved(newX, newY, 0);
        } else {
            return true;
        }

        invalidate(); // redraw screen

        // -------
        remoteHandler.process(event);

        return true;
    }

    public boolean onRemoteTouchEvent(CustomMotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(), event.getY(),1);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            isDrawing[1] = false;
        } else {
            touchMoved(event.getNewX(), event.getNewY(), 1);
        }
        invalidate(); // redraw screen
        return true;
    }

    private void touchStarted(float x, float y, int user) {
        SerializablePath path = new SerializablePath( new PathPaint(paintLine[user]) );
        Point point;

        paths[user].add(path);
        path.moveTo(x,y);

        point = new Point((int)x, (int)y);
        previousPoint[user] = point;

        isDrawing[user] = true;
    }

    private void touchMoved(float newX, float newY, int user) {
        SerializablePath path = paths[user].get(paths[user].size()-1);
        Point lastPoint = previousPoint[user];

        float deltaX = Math.abs(newX - lastPoint.x);
        float deltaY = Math.abs(newY - lastPoint.y);

        if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
            path.quadTo(lastPoint.x, lastPoint.y, (newX + lastPoint.x) / 2, (newY + lastPoint.y) / 2);

            lastPoint.x = (int) newX;
            lastPoint.y = (int) newY;
        }
    }

    private void touchEnded(int user) {
        isDrawing[user] = false;
        // draw whole path again, then refresh view again --> bug fix quick drawing
        drawCurrent(user);
        invalidate();
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
        paths[user].clear();
        bitmap.eraseColor(Color.WHITE);
        drawAll(keep);
    }

    public void drawAll(int user) {
        if (paths[user].size() == 0)
            return;
        for (SerializablePath path : paths[user]) {
            bitmapCanvas.drawPath(path, path.getPaint());
        }

        if (mainActivity != null)
            mainActivity.runOnUiThread(() -> { invalidate(); });
    }

    public void setPathMap(List<SerializablePath> paths, int user) {
        this.paths[user] = paths;
        drawAll(user);
    }

    public List<SerializablePath> getPaths(int user) {
        return paths[user];
    }

    public Bitmap getBitmap () {
        return bitmap;
    }

    public PathPaint getPaintLine(int user) {
        return paintLine[user];
    }


    public void setBitmap (Bitmap bitmap) {
        clear(0, 1);
        this.bitmap = Bitmap.createBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        bitmapCanvas = new Canvas(this.bitmap);
        Log.d(TAG, "setBitmap: set bitmap successfully");
    }

}

