package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Color;
import android.graphics.Paint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@JsonDeserialize(using = PathPaintDeserializer.class)
public class PathPaint extends Paint implements Serializable {
    private static final long serialVersionUID = 5573982934754983L;

    @JsonProperty("lineWidth")
    private int lineWidth;
    @JsonProperty("antiAlias")
    private boolean antiAlias;
    @JsonProperty("color")
    private int color;
    @JsonProperty("style")
    private Style style;
    @JsonProperty("cap")
    private Cap cap;
    @JsonProperty("alpha")
    private int alpha;

    public PathPaint() {
    }

    public PathPaint(PathPaint paint) {
        this.lineWidth = paint.getLineWidth();
        this.antiAlias = paint.isAntiAlias();
        this.color = paint.getColor();
        this.style = paint.getStyle();
        this.cap = paint.getCap();
        this.alpha = paint.getAlpha();
        recreate();
    }

    public void recreate() {
        super.setAntiAlias(antiAlias);
        super.setStrokeWidth(lineWidth);
        super.setColor(color);
        super.setStyle(style);
        super.setStrokeCap(cap);
        super.setAlpha(alpha);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setStrokeWidth(float width) {
        lineWidth = (int) width;
        super.setStrokeWidth(lineWidth);
    }

    @Override
    public void setAntiAlias(boolean b) {
        antiAlias = b;
        super.setAntiAlias(antiAlias);
    }

    @Override
    public void setColor(int color) {
        this.color = color;
        super.setColor(color);
    }

    @Override
    public void setStyle(Style style) {
        this.style = style;
        super.setStyle(style);
    }

    @Override
    public void setStrokeCap(Cap cap) {
        this.cap = cap;
        super.setStrokeCap(cap);
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public Cap getCap() {
        return cap;
    }
}
