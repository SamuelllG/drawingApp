package com.mobileanwendungen.drawingapp.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

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
