package com.mobileanwendungen.drawingapp.utilities;

import android.graphics.Paint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class PathPaintDeserializer extends JsonDeserializer<PathPaint> {
    @Override
    public PathPaint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        PathPaint paint = new PathPaint();
        JsonNode node = jp.readValueAsTree();
        paint.setAntiAlias(node.get("antiAlias").asBoolean());
        paint.setStrokeCap(Paint.Cap.valueOf(node.get("cap").asText()));
        paint.setColor(node.get("color").asInt());
        paint.setStrokeWidth(node.get("lineWidth").asInt());
        paint.setStyle(Paint.Style.valueOf(node.get("style").asText()));
        paint.setAlpha(node.get("alpha").asInt());
        return paint;
    }
}
