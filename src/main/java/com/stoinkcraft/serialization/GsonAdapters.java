package com.stoinkcraft.serialization;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;
import java.util.UUID;

public class GsonAdapters {

    public static class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
        @Override
        public JsonElement serialize(Location loc, Type type, JsonSerializationContext context) {
            if (loc == null) return JsonNull.INSTANCE;

            JsonObject obj = new JsonObject();
            obj.addProperty("world", loc.getWorld().getName());
            obj.addProperty("x", loc.getX());
            obj.addProperty("y", loc.getY());
            obj.addProperty("z", loc.getZ());
            obj.addProperty("yaw", loc.getYaw());
            obj.addProperty("pitch", loc.getPitch());
            return obj;
        }

        @Override
        public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) return null;

            JsonObject obj = json.getAsJsonObject();
            World world = Bukkit.getWorld(obj.get("world").getAsString());

            if (world == null) {
                throw new JsonParseException("World not found: " + obj.get("world").getAsString());
            }

            return new Location(
                    world,
                    obj.get("x").getAsDouble(),
                    obj.get("y").getAsDouble(),
                    obj.get("z").getAsDouble(),
                    obj.get("yaw").getAsFloat(),
                    obj.get("pitch").getAsFloat()
            );
        }
    }

    public static class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
        @Override
        public JsonElement serialize(UUID uuid, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(uuid.toString());
        }

        @Override
        public UUID deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return UUID.fromString(json.getAsString());
        }
    }
}