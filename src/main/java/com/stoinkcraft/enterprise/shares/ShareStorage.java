package com.stoinkcraft.enterprise.shares;

import com.stoinkcraft.StoinkCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShareStorage {

    private static final File SHARES_FILE = new File(StoinkCore.getInstance().getDataFolder(), "shares.yml");
    public static void saveShares() {
        if (!SHARES_FILE.getParentFile().exists()) SHARES_FILE.getParentFile().mkdirs();

        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Share share : ShareManager.getInstance().getAllShares()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("owner", share.getOwner().toString());
            entry.put("enterpriseID", share.getEnterpriseID().toString());
            entry.put("valueAtPurchase", share.getPurchasePrice());
            entry.put("purchasedate", share.getPurchaseDate().getTime());
            list.add(entry);
        }

        config.set("shares", list);

        try {
            config.save(SHARES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadShares() {
        if (!SHARES_FILE.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(SHARES_FILE);

        List<Map<?, ?>> list = config.getMapList("shares");
        for (Map<?, ?> entry : list) {
            try {
                String ownerStr = (String) entry.get("owner");
                String enterpriseIDStr = (String) entry.get("enterpriseID");
                Object valueObj = entry.get("valueAtPurchase");
                Object purchaseDateObj = entry.get("purchasedate");

                if (ownerStr == null || enterpriseIDStr == null || valueObj == null || purchaseDateObj == null) {
                    Bukkit.getLogger().warning("Skipping invalid share entry: " + entry);
                    continue;
                }

                UUID owner = UUID.fromString(ownerStr);
                UUID enterpriseID = UUID.fromString(enterpriseIDStr);
                double value = ((Number) valueObj).doubleValue();
                long purchaseTime = ((Number) purchaseDateObj).longValue();
                Date purchaseDate = new Date(purchaseTime);

                Share share = new Share(owner, enterpriseID, value, purchaseDate);
                ShareManager.getInstance().getAllShares().add(share);
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Failed to load share: " + entry + " - " + ex.getMessage());
            }
        }
    }


}
