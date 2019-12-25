package com.mobileanwendungen.drawingapp.utilities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapWrapper implements Serializable {
    private HashMap<Integer, SerializablePath> map;

    public HashMap<Integer, SerializablePath> getMap() {
        return map;
    }

    public void setMap(HashMap<Integer, SerializablePath> map) {
        this.map = map;
    }

    public void addToMap(HashMap<Integer, SerializablePath> map) {
        this.map.putAll(map);
    }
}
