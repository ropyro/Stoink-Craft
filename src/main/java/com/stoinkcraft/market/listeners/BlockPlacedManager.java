package com.stoinkcraft.market.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

public class BlockPlacedManager {
    private static BlockPlacedManager instance;
    private final NamespacedKey key;
    private final JavaPlugin plugin;

    public BlockPlacedManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "placed_blocks");
        instance = this;
    }

    public static BlockPlacedManager getInstance() {
        return instance;
    }

    // Mark a block as player placed
    public void markPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        String data = container.getOrDefault(key, PersistentDataType.STRING, "");
        String entry = serialize(block.getX(), block.getY(), block.getZ());

        if (!data.contains(entry + ";")) {
            data += entry + ";";
            container.set(key, PersistentDataType.STRING, data);
            chunk.addPluginChunkTicket(plugin); // keeps chunk loaded long enough to persist
            chunk.setForceLoaded(true);
            chunk.getWorld().save();
            plugin.getLogger().info("[DEBUG] Marked placed at " + entry);
        }
    }

    public void unmarkPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer container = chunk.getPersistentDataContainer();

        String data = container.getOrDefault(key, PersistentDataType.STRING, "");
        String entry = serialize(block.getX(), block.getY(), block.getZ()) + ";";

        if (data.contains(entry)) {
            data = data.replace(entry, ""); // remove this block's entry
            container.set(key, PersistentDataType.STRING, data);

            chunk.addPluginChunkTicket(plugin); // ensure persistence
            chunk.setForceLoaded(true);
            chunk.getWorld().save();

            plugin.getLogger().info("[DEBUG] Unmarked placed at " + entry);
        }
    }


    public boolean isPlayerPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        String data = container.getOrDefault(key, PersistentDataType.STRING, "");
        boolean result = data.contains(serialize(block.getX(), block.getY(), block.getZ()) + ";");
        if (result) plugin.getLogger().info("[DEBUG] Found placed: " + block.getType() + " at " + block.getLocation());
        return result;
    }


    private String serialize(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
