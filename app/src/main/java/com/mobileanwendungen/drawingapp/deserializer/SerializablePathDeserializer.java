package com.mobileanwendungen.drawingapp.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobileanwendungen.drawingapp.wrapper.Action;
import com.mobileanwendungen.drawingapp.wrapper.PathPaint;
import com.mobileanwendungen.drawingapp.wrapper.SerializablePath;

import java.io.IOException;
import java.util.Iterator;

public class SerializablePathDeserializer extends JsonDeserializer<SerializablePath> {
    @Override
    public SerializablePath deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        SerializablePath path = new SerializablePath();
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode paint = node.get("paint");
        //PathPaint pathPaint = ctxt.readValue(paint.traverse(), PathPaint.class);
        path.setPaint(new PathPaint());
        JsonNode actions = node.get("actions");
        Iterator it = actions.elements();
        while(it.hasNext()) {
            path.addAction(ctxt.readValue(((JsonNode) it.next()).traverse(), Action.class));
        }
        return path;
    }
}