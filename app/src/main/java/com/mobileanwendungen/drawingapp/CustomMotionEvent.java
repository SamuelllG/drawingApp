package com.mobileanwendungen.drawingapp;

import java.io.Serializable;

public class CustomMotionEvent implements Serializable {

    private int action;
    private int actionIndex;

    private int actionPointerId;
    private int pointerId;
    private float x;
    private float y;
    private float newX;
    private float newY;


    public float getNewX() {
        return newX;
    }

    public void setNewX(float newX) {
        this.newX = newX;
    }

    public float getNewY() {
        return newY;
    }

    public void setNewY(float newY) {
        this.newY = newY;
    }

    public int getPointerId() {
        return pointerId;
    }

    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }

    public int getActionPointerId() {
        return actionPointerId;
    }

    public void setActionPointerId(int actionPointerId) {
        this.actionPointerId = actionPointerId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getActionIndex() {
        return actionIndex;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }
}
