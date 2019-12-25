package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Path;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Move.class, name = "Move"),
        @JsonSubTypes.Type(value = Quad.class, name = "Quad"),
        @JsonSubTypes.Type(value = Line.class, name = "Line") }
)
public interface Action extends Serializable {

    void perform(SerializablePath path);
}