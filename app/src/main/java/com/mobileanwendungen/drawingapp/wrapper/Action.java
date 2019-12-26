package com.mobileanwendungen.drawingapp.wrapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Move.class, name = "Move"),
        @JsonSubTypes.Type(value = Quad.class, name = "Quad"),
        @JsonSubTypes.Type(value = Line.class, name = "Line") }
)
public interface Action extends Serializable {

    void perform(SerializablePath path);
}