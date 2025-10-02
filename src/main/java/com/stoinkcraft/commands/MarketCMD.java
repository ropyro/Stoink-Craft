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

        player.sendMessage("Market Values:");
        for (String product : MarketManager.values.keySet()){
            player.sendMessage(product + ": " + MarketManager.getValue(product));
        }

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

        for (String product : MarketManager.values.keySet()){
            if(Material.getMaterial(product) == null) {
                ItemBuilder nonmaterialgood = new ItemBuilder(Material.DIRT)
                        .setDisplayName(product)
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fValue: §a$" + MarketManager.getValue(product));
                gui.addItems(new SimpleItem(nonmaterialgood));
                continue;
            }
            ItemBuilder item = new ItemBuilder(Material.getMaterial(product))
                    .setDisplayName(Material.getMaterial(product).name())
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fValue: §a$" + MarketManager.getValue(product));
            gui.addItems(new SimpleItem(item));
        }

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§a§lTop Enterprises")
                .setGui(gui)
                .build();
        window.open();
        return true;
    }
}
