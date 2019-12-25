package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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
