package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Path;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SerializablePath extends Path implements Serializable {
    private static final long serialVersionUID = 422910382351392014L;

    @JsonProperty("actions")
    private List<Action> actions = new LinkedList<>();

    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        for (Action action : actions) {
            action.perform(this);
        }
    }

    public void recreate() {
        for (Action action : actions) {
            action.perform(this);
        }
    }

    @Override
    public void lineTo(float x, float y) {
        actions.add(new Line(x, y));
        super.lineTo(x, y);
    }

    @Override
    public void moveTo(float x, float y) {
        actions.add(new Move(x, y));
        super.moveTo(x, y);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        actions.add(new Quad(x1, y1, x2, y2));
        super.quadTo(x1, y1, x2, y2);
    }

    public void superMoveTo(float x, float y) {
        super.moveTo(x, y);
    }
    public void superLineTo(float x, float y) {
        super.moveTo(x, y);
    }
    public void superQuadTo(float x1, float y1, float x2, float y2) {
        super.quadTo(x1, y1, x2, y2);
    }
}
