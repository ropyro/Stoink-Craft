package com.stoinkcraft.commands;

import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MarketCMD implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player)) return true;
        Player player = (Player)commandSender;

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" "))) // Filler
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.PAPER)
                        .setDisplayName("Market Info:")
                        .addLoreLines("§7Current offers available")
                        .addLoreLines("§7for employees to grind!")
                ))
                .build();

        for (String product : MarketManager.values.keySet()) {
            Material mat = Material.getMaterial(product);

            // Default to contextual item if material is null or too generic
            if (mat == null || mat == Material.DIRT) {
                Material contextualMaterial = Material.DIRT;

                // Try to infer more appropriate material
                String lower = product.toLowerCase();

                if (lower.contains("bone") || lower.contains("mob") || lower.contains("flesh") || lower.contains("string") || lower.contains("gunpowder")) {
                    contextualMaterial = Material.SKELETON_SKULL; // Generic mob skull
                } else if (lower.contains("spider")) {
                    contextualMaterial = Material.SPIDER_EYE;
                } else if (lower.contains("zombie")) {
                    contextualMaterial = Material.ZOMBIE_HEAD;
                } else if (lower.contains("creeper")) {
                    contextualMaterial = Material.CREEPER_HEAD;
                } else if (lower.contains("fishing") || lower.contains("fish") || lower.contains("salmon") || lower.contains("cod") || lower.contains("tuna")) {
                    contextualMaterial = Material.COD; // Generic fish
                }

                ItemBuilder contextualItem = new ItemBuilder(contextualMaterial)
                        .setDisplayName(product)
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fValue: §a$" + MarketManager.getValue(product));
                gui.addItems(new SimpleItem(contextualItem));
                continue;
            }

            ItemBuilder item = new ItemBuilder(mat)
                    .setDisplayName(mat.name())
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fValue: §a$" + MarketManager.getValue(product));
            gui.addItems(new SimpleItem(item));
        }

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§7Market Prices")
                .setGui(gui)
                .build();
        window.open();
        return true;
    }
}
