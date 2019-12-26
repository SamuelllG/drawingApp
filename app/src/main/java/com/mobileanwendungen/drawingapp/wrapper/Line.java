package com.mobileanwendungen.drawingapp.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Line implements Action {

    @JsonProperty
    private float x;
    @JsonProperty
    private float y;

    public Line(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Line() {}

    @Override
    public void perform(SerializablePath path) {
        path.superLineTo(x, y);
    }
}
