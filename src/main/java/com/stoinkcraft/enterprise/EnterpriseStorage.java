package com.stoinkcraft.enterprise;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnterpriseStorage {

    public static void saveAllEnterprises(File file) {
        YamlConfiguration config = new YamlConfiguration();
        List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager().getEnterpriseList();

        for (Enterprise e : enterprises) {
            String path = "enterprises." + e.getName();

            config.set(path + ".ceo", e.getCeo().toString());
            if (e.getCfo() != null) config.set(path + ".cfo", e.getCfo().toString());
            if (e.getCOO() != null) config.set(path + ".coo", e.getCOO().toString());

            config.set(path + ".bankBalance", e.getBankBalance());
            config.set(path + ".netWorth", e.getNetWorth());

            Map<String, String> memberMap = new HashMap<>();
            for (UUID uuid : e.getMembers().keySet()) {
                memberMap.put(uuid.toString(), e.getMembers().get(uuid).name());
            }
            config.set(path + ".members", memberMap);

            Map<String, Double> shareMap = new HashMap<>();
            for (UUID uuid : e.getShares().keySet()) {
                shareMap.put(uuid.toString(), e.getShares().get(uuid));
            }
            config.set(path + ".shares", shareMap);
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadAllEnterprises(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("enterprises")) return;

        for (String name : config.getConfigurationSection("enterprises").getKeys(false)) {
            String path = "enterprises." + name;

            UUID ceo = UUID.fromString(config.getString(path + ".ceo"));
            Enterprise e = new Enterprise(name, ceo);

            String cfoStr = config.getString(path + ".cfo");
            String cooStr = config.getString(path + ".coo");
            if (cfoStr != null) e.setCfo(UUID.fromString(cfoStr));
            if (cooStr != null) e.setCOO(UUID.fromString(cooStr));

            e.setBankBalance(config.getDouble(path + ".bankBalance"));
            e.setNetWorth(config.getDouble(path + ".netWorth"));

            Map<String, Object> membersRaw = config.getConfigurationSection(path + ".members").getValues(false);
            for (String uuidStr : membersRaw.keySet()) {
                Role role = Role.valueOf((String) membersRaw.get(uuidStr));
                e.getMembers().put(UUID.fromString(uuidStr), role);
            }

            Map<String, Object> sharesRaw = config.getConfigurationSection(path + ".shares").getValues(false);
            for (String uuidStr : sharesRaw.keySet()) {
                double share = (Double) sharesRaw.get(uuidStr);
                e.getShares().put(UUID.fromString(uuidStr), share);
            }

            EnterpriseManager.getEnterpriseManager().createEnterprise(e);
        }
    }
}
