package com.stoinkcraft.shares;

import com.stoinkcraft.StoinkCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShareStorage {

    private static final File SHARES_FILE = new File(StoinkCore.getInstance().getDataFolder(), "Shares");
    public static void saveShares() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Share share : ShareManager.getInstance().getAllShares()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("owner", share.getOwner().toString());
            entry.put("enterpriseID", share.getEnterpriseID().toString());
            entry.put("valueAtPurchase", share.getPurchasePrice());
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
            UUID owner = UUID.fromString((String) entry.get("owner"));
            UUID enterpriseID = UUID.fromString((String) entry.get("enterpriseID"));
            double value = ((Number) entry.get("valueAtPurchase")).doubleValue();
            long timestamp = ((Number) entry.get("timestamp")).longValue();

            Share share = new Share(owner, enterpriseID, value);
            // override timestamp if you add a constructor that accepts it
            ShareManager.getInstance().getAllShares().add(share);
        }
    }

}
