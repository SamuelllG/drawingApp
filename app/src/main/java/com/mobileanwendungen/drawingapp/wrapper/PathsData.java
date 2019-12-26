package com.mobileanwendungen.drawingapp.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mobileanwendungen.drawingapp.wrapper.SerializablePath;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PathsData implements Serializable {
    private static final long serialVersionUID = 789713232142315215L;

    private List<SerializablePath> paths;

    public List<SerializablePath> getPaths() {
        return paths;
    }

    public void setPaths(List<SerializablePath> paths) {
        this.paths = paths;
    }

    public void addToPaths(List<SerializablePath> paths) {
        this.paths.addAll(paths);
    }
}
