package com.mobileanwendungen.drawingapp.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Quad implements Action {

    @JsonProperty
    private float x1;
    @JsonProperty
    private float y1;
    @JsonProperty
    private float x2;
    @JsonProperty
    private float y2;

    public Quad(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Quad() {}

    @Override
    public void perform(SerializablePath path) {
        path.superQuadTo(x1, y1, x2, y2);
    }
}
