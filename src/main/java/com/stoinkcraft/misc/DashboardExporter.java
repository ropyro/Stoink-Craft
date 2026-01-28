package com.stoinkcraft.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.PriceSnapshot;
import com.stoinkcraft.enterprise.shares.Share;
import com.stoinkcraft.enterprise.shares.ShareManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardExporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void exportAllData(File dataFolder) {
        exportEnterprises(new File(dataFolder, "dashboard/enterprises.json"));
        exportShares(new File(dataFolder, "dashboard/shares.json"));
        exportContracts(new File(dataFolder, "dashboard/contracts.json"));
        exportJobSites(new File(dataFolder, "dashboard/jobsites.json"));
    }

    private static void exportEnterprises(File file) {
        List<Map<String, Object>> data = new ArrayList<>();

        for (Enterprise e : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", e.getID().toString());
            entry.put("name", e.getName());
            entry.put("type", e.getEnterpriseType());
            entry.put("ceo", Bukkit.getOfflinePlayer(e.getCeo()).getName());
            entry.put("memberCount", e.getMembers().size());
            entry.put("bankBalance", e.getBankBalance());
            entry.put("netWorth", e.getNetWorth());
            entry.put("shareValue", e.getShareValue());
            entry.put("outstandingShares", e.getOutstandingShares());
            entry.put("exportTime", System.currentTimeMillis());

            // Price history
            List<Map<String, Object>> priceHistory = new ArrayList<>();
            for (PriceSnapshot snap : e.getPriceHistory()) {
                Map<String, Object> point = new HashMap<>();
                point.put("timestamp", snap.getTimestamp());
                point.put("price", snap.getSharePrice());
                priceHistory.add(point);
            }
            entry.put("priceHistory", priceHistory);

            data.add(entry);
        }

        writeJson(file, data);
    }

    private static void exportShares(File file) {
        List<Map<String, Object>> data = new ArrayList<>();

        for (Share share : ShareManager.getInstance().getAllShares()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("owner", Bukkit.getOfflinePlayer(share.getOwner()).getName());
            entry.put("ownerUUID", share.getOwner().toString());
            entry.put("enterpriseId", share.getEnterpriseID().toString());
            entry.put("purchasePrice", share.getPurchasePrice());
            entry.put("purchaseDate", share.getPurchaseDate().getTime());
            data.add(entry);
        }

        writeJson(file, data);
    }

    private static void exportContracts(File file) {
        // Export contract completion data
        // Adapt to your ContractManager structure
        List<Map<String, Object>> data = new ArrayList<>();

        StoinkCore core = StoinkCore.getInstance();
        core.getEnterpriseManager().getEnterpriseList().forEach(e ->
                core.getContractManager().getContracts(e).forEach(c ->
                {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id", c.getContractId());
                    entry.put("definitionId", c.getDefinition().id());
                    entry.put("jobsitetype", c.getDefinition().jobSiteType().getDisplayName());
                    entry.put("targetAmount", c.getDefinition().targetAmount());
                    entry.put("progress", c.getProgress());
                    Map<String, Integer> contributions = new HashMap<>();
                    c.getContributions().keySet().forEach(uuid -> contributions.put(Bukkit.getOfflinePlayer(uuid).getName(), c.getContributions().get(uuid)));
                    entry.put("contributions", contributions);
                    data.add(entry);
                }));

        writeJson(file, data);
    }

    private static void exportJobSites(File file) {
        // Export jobsite stats per enterprise
        // Mob counts, upgrade levels, etc.

    }

    private static void writeJson(File file, Object data) {
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}