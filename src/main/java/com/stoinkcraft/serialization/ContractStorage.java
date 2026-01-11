package com.stoinkcraft.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.contracts.ActiveContract;
import com.stoinkcraft.earning.contracts.ContractDefinition;
import com.stoinkcraft.earning.contracts.ContractPool;
import com.stoinkcraft.earning.contracts.ContractSaveData;
import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ContractStorage {

    private static final File ENTERPRISES_DIR =
            new File(StoinkCore.getInstance().getDataFolder(), "Enterprises");

    private final Gson gson;

    public ContractStorage(Gson gson) {
        this.gson = gson;
    }

    // ===== SAVE =====

    public boolean saveContracts(UUID enterpriseId, List<ActiveContract> contracts) {

        File dir = getContractsDirectory(enterpriseId);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "contracts.json");
        File backup = new File(dir, "contracts.json.backup");

        try {
            if (file.exists()) {
                Files.copy(file.toPath(), backup.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            List<ContractSaveData> data = contracts.stream()
                    .map(ActiveContract::toSaveData)
                    .toList();

            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }

            return true;

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to save contracts for enterprise " + enterpriseId, e);

            if (backup.exists()) {
                try {
                    Files.copy(backup.toPath(), file.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {}
            }
            return false;
        }
    }

    // ===== LOAD =====

    public List<ActiveContract> loadContracts(
            UUID enterpriseId,
            ContractPool pool
    ) {

        File file = new File(getContractsDirectory(enterpriseId), "contracts.json");
        if (!file.exists()) return List.of();

        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {

            Type type = new TypeToken<List<ContractSaveData>>(){}.getType();
            List<ContractSaveData> data = gson.fromJson(reader, type);

            return data.stream()
                    .map(d -> rebuildContract(d, pool))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to load contracts for enterprise " + enterpriseId, e);
            return List.of();
        }
    }

    private ActiveContract rebuildContract(
            ContractSaveData data,
            ContractPool pool
    ) {
        ContractDefinition def = pool.getById(data.definitionId);
        if (def == null) {
            Bukkit.getLogger().warning(
                    "Missing contract definition: " + data.definitionId);
            return null;
        }

        return new ActiveContract(
                data.contractId,
                data.enterpriseId,
                def,
                data.expirationTime,
                data.progress,
                data.completed,
                data.weekly,
                data.bonus,
                data.contributions
        );
    }

    // ===== UTIL =====

    private File getContractsDirectory(UUID enterpriseId) {
        return new File(ENTERPRISES_DIR,
                enterpriseId + "/contracts");
    }
}
