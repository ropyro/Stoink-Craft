package com.stoinkcraft.utils;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicPaster {

    public static void pasteSchematic(File file, Location loc, boolean ignoreAir) {
        if (!file.exists()) {
            Bukkit.getLogger().warning("Schematic file not found: " + file);
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Bukkit.getLogger().warning("Unknown schematic format for file: " + file.getName());
            return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();

            World bukkitWorld = loc.getWorld();
            if (bukkitWorld == null) {
                Bukkit.getLogger().warning("World is null for location: " + loc);
                return;
            }

            com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

            EditSessionBuilder builder = WorldEdit.getInstance().newEditSessionBuilder();
            builder.world(weWorld);
            builder.fastMode(true);

            try (EditSession editSession = builder.build()) {
                new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                        .ignoreAirBlocks(ignoreAir)
                        .build();

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                        .ignoreAirBlocks(ignoreAir)
                        .build();
                Operations.complete(operation);
                editSession.flushQueue();
            }


            Bukkit.getLogger().info("Successfully pasted schematic: " + file.getName());

        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to paste schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
