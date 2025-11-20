package com.stoinkcraft.serialization;

import com.google.gson.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;

public class VectorAdapter implements JsonSerializer<Vector>, JsonDeserializer<Vector> {

    @Override
    public JsonElement serialize(Vector vector, Type type, JsonSerializationContext context) {
        if (vector == null) return JsonNull.INSTANCE;

        JsonObject obj = new JsonObject();
        obj.addProperty("x", vector.getX());
        obj.addProperty("y", vector.getY());
        obj.addProperty("z", vector.getZ());
        return obj;
    }

    @Override
    public Vector deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonNull()) return null;

        JsonObject obj = json.getAsJsonObject();
        return new Vector(
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble()
        );
    }
}