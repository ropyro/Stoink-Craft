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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicUtils {

    public static Region getRegionFromSchematic(File file, Location pasteLoc) {
        if (!file.exists()) {
            Bukkit.getLogger().warning("Schematic file not found: " + file);
            return null;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Bukkit.getLogger().warning("Unknown schematic format for file: " + file.getName());
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();

            World bukkitWorld = pasteLoc.getWorld();
            if (bukkitWorld == null) {
                Bukkit.getLogger().warning("World is null for location: " + pasteLoc);
                return null;
            }

            // Get schematic dimensions
            Region schematicRegion = clipboard.getRegion();
            BlockVector3 origin = clipboard.getOrigin();
            BlockVector3 offset = BlockVector3.at(pasteLoc.getBlockX(), pasteLoc.getBlockY(), pasteLoc.getBlockZ()).subtract(origin);

            // Translate schematic region to world coordinates
            BlockVector3 min = schematicRegion.getMinimumPoint().add(offset);
            BlockVector3 max = schematicRegion.getMaximumPoint().add(offset);

            // Return a new region representing where the schematic *will* be pasted
            com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());
            return new CuboidRegion(weWorld, min, max);

        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to read schematic: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
