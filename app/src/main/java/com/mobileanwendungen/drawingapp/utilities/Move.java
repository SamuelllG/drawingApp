package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;

public final class Move implements Action {

    @JsonProperty
    private float x;
    @JsonProperty
    private float y;

    public Move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Move() {}

    @Override
    public void perform(SerializablePath path) {
        path.superMoveTo(x, y);
    }
}
