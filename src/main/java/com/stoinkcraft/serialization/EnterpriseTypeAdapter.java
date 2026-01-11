package com.stoinkcraft.serialization;

import com.google.gson.*;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.earning.boosters.Booster;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.*;

public class EnterpriseTypeAdapter implements JsonSerializer<Enterprise>, JsonDeserializer<Enterprise> {

    @Override
    public JsonElement serialize(Enterprise enterprise, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Add type discriminator
        jsonObject.addProperty("enterpriseType",
                enterprise instanceof ServerEnterprise ? "SERVER" : "PLAYER");

        // Serialize all fields
        jsonObject.addProperty("name", enterprise.getName());
        jsonObject.add("ceo", context.serialize(enterprise.getCeo()));
        jsonObject.addProperty("bankBalance", enterprise.getBankBalance());
        jsonObject.addProperty("reputation", enterprise.getReputation());
        jsonObject.addProperty("outstandingShares", enterprise.getOutstandingShares());
        jsonObject.add("enterpriseID", context.serialize(enterprise.getID()));
        jsonObject.addProperty("plotIndex", enterprise.getPlotIndex());

        // Serialize complex types
        jsonObject.add("members", serializeMembers(enterprise.getMembers(), context));
        jsonObject.add("warp", context.serialize(enterprise.getWarp(), Location.class));
        jsonObject.add("activeBooster", context.serialize(enterprise.getActiveBooster()));
        jsonObject.add("priceHistory", context.serialize(enterprise.getPriceHistory()));

        return jsonObject;
    }

    @Override
    public Enterprise deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Read type discriminator
        String enterpriseType = jsonObject.has("enterpriseType")
                ? jsonObject.get("enterpriseType").getAsString()
                : "PLAYER";

        // Deserialize common fields
        String name = jsonObject.get("name").getAsString();
        UUID ceo = context.deserialize(jsonObject.get("ceo"), UUID.class);
        double bankBalance = jsonObject.get("bankBalance").getAsDouble();
        // Read reputation (defaults to 0 for old data without this field)
        double reputation = jsonObject.has("reputation")
                ? jsonObject.get("reputation").getAsDouble()
                : 0.0;
        int outstandingShares = jsonObject.get("outstandingShares").getAsInt();
        UUID enterpriseID = context.deserialize(jsonObject.get("enterpriseID"), UUID.class);

        Booster activeBooster = null;
        if (jsonObject.has("activeBooster") && !jsonObject.get("activeBooster").isJsonNull()) {
            activeBooster = context.deserialize(jsonObject.get("activeBooster"), Booster.class);
        }

        // Create appropriate instance
        Enterprise enterprise;
        if ("SERVER".equals(enterpriseType)) {
            enterprise = new ServerEnterprise(name, ceo, bankBalance, reputation,
                    outstandingShares, activeBooster, enterpriseID);
        } else {
            enterprise = new Enterprise(name, ceo, bankBalance, reputation,
                    outstandingShares, activeBooster, enterpriseID);
        }

        // Set additional fields
        if (jsonObject.has("plotIndex")) {
            enterprise.setPlotIndex(jsonObject.get("plotIndex").getAsInt());
        }

        if (jsonObject.has("warp") && !jsonObject.get("warp").isJsonNull()) {
            Location warp = context.deserialize(jsonObject.get("warp"), Location.class);
            enterprise.setWarp(warp);
        }

        // Deserialize members
        if (jsonObject.has("members")) {
            deserializeMembers(jsonObject.get("members"), enterprise, context);
        }

        // Deserialize price history
        if (jsonObject.has("priceHistory")) {
            deserializePriceHistory(jsonObject.get("priceHistory"), enterprise, context);
        }

        return enterprise;
    }

    private JsonElement serializeMembers(Map<UUID, com.stoinkcraft.enterprise.Role> members,
                                         JsonSerializationContext context) {
        JsonObject membersObj = new JsonObject();
        for (Map.Entry<UUID, com.stoinkcraft.enterprise.Role> entry : members.entrySet()) {
            membersObj.addProperty(entry.getKey().toString(), entry.getValue().name());
        }
        return membersObj;
    }

    private void deserializeMembers(JsonElement membersElement, Enterprise enterprise,
                                    JsonDeserializationContext context) {
        if (membersElement.isJsonNull()) return;

        JsonObject membersObj = membersElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : membersObj.entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            com.stoinkcraft.enterprise.Role role =
                    com.stoinkcraft.enterprise.Role.valueOf(entry.getValue().getAsString());

            // Only add non-CEO members (CEO is already added in constructor)
            if (role != com.stoinkcraft.enterprise.Role.CEO) {
                enterprise.getMembers().put(uuid, role);
            }
        }
    }

    private void deserializePriceHistory(JsonElement historyElement, Enterprise enterprise,
                                         JsonDeserializationContext context) {
        if (historyElement.isJsonNull()) return;

        JsonArray historyArray = historyElement.getAsJsonArray();
        for (JsonElement element : historyArray) {
            com.stoinkcraft.enterprise.PriceSnapshot snapshot =
                    context.deserialize(element, com.stoinkcraft.enterprise.PriceSnapshot.class);
            enterprise.getPriceHistory().add(snapshot);
        }
    }
}