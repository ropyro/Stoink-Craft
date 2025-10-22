package com.stoinkcraft.playerupgrades;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.market.values.EntityValue;
import com.stoinkcraft.shares.guis.ConfirmationGUI;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.PlayerUtils;
import com.stoinkcraft.utils.SCConstants;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;

import static com.stoinkcraft.market.MarketManager.JobType.*;

public class PMenuGUI {

    private Player opener;

    public PMenuGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName(" §a§lPlayer Upgrades")
                        .addLoreLines(" ")
                        .addLoreLines("§7The options below do cost money!")
                        .addLoreLines("§7Unlock these upgrades to improve")
                        .addLoreLines("§7your experience or customize your")
                        .addLoreLines("§7personal look in chat and in game!")))
                .build();

        //Fly Permission
        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.ELYTRA);
                if(opener.hasPermission("essentials.fly")){
                    guiButton.setDisplayName("§a§l/fly");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/fly Cost: $250k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to be able to fly?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /fly!");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /fly for $250k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.fly")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 250000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /fly costs $250k");
                        return;
                    }

                    new ConfirmationGUI(player, "Buy /fly for $250k",
                            () -> {
                            StoinkCore.getEconomy().withdrawPlayer(player, 250000);
                            PlayerUtils.givePermission(player, "essentials.fly");
                    }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });
        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.ENDER_PEARL);
                if(opener.hasPermission("essentials.jump")){
                    guiButton.setDisplayName("§a§l/jump");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/jump Cost: $120k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to be able to teleport?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /jump");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /jump for $120k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.jump")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 120000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /jump costs $120k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /jump for $120k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 120000);
                                PlayerUtils.givePermission(player, "essentials.jump");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });

        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.CRAFTING_TABLE);
                if(opener.hasPermission("essentials.craft")){
                    guiButton.setDisplayName("§a§l/craft");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/craft Cost: $100k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to craft from anywhere?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /craft");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /craft for $100k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.craft")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 100000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /craft costs $100k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /craft for $100k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 100000);
                                PlayerUtils.givePermission(player, "essentials.craft");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });

        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.GOLDEN_APPLE);
                if(opener.hasPermission("essentials.heal")){
                    guiButton.setDisplayName("§a§l/heal");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/heal Cost: $180k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to be able to heal yourself?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /heal");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /heal for $180k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.heal")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 180000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /heal costs $180k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /heal for $180k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 180000);
                                PlayerUtils.givePermission(player, "essentials.heal");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });


        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.COOKED_BEEF);
                if(opener.hasPermission("essentials.feed")){
                    guiButton.setDisplayName("§a§l/feed");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/feed Cost: $90k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to be able to feed yourself?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /feed");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /feed for $90k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.feed")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 90000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /feed costs $90k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /feed for $90k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 90000);
                                PlayerUtils.givePermission(player, "essentials.feed");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });

        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE);
                if(opener.hasPermission("essentials.god")){
                    guiButton.setDisplayName("§a§l/god");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/god Cost: $500k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to be invincible?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /god");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /god for $500k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.god")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 500000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /god costs $500k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /god for $500k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 500000);
                                PlayerUtils.givePermission(player, "essentials.god");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });

        gui.addItems(new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder guiButton = new ItemBuilder(Material.ANVIL);
                if(opener.hasPermission("essentials.fix")){
                    guiButton.setDisplayName("§a§l/fix");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§a§l(!) This upgrade is already unlocked! (!)");
                }else{
                    guiButton.setDisplayName("§c§l/fix Cost: $350k");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§7Want to fix your items?");
                    guiButton.addLoreLines("§7Unlock this upgrade to start");
                    guiButton.addLoreLines("§7using /fix");
                    guiButton.addLoreLines(" ");
                    guiButton.addLoreLines("§c§l(!) Click here to buy /fix for $350k (!)");
                }
                return guiButton;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (!player.hasPermission("essentials.fix")) {
                    if(StoinkCore.getEconomy().getBalance(player) < 350000){
                        player.closeInventory();
                        ChatUtils.sendMessage(player, "§cInsufficient funds, /fix costs $350k");
                        return;
                    }
                    new ConfirmationGUI(player, "Buy /fix for $350k",
                            () -> {
                                StoinkCore.getEconomy().withdrawPlayer(player, 350000);
                                PlayerUtils.givePermission(player, "essentials.fix");
                            }, () -> new PMenuGUI(player).openWindow()).openWindow();
                }
            }
        });
        gui.addItems(new MoreHomesItem());

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Player Upgrades")
                .setGui(gui)
                .build();
        window.open();
    }

    public class MoreHomesItem extends AbstractItem {

        @Override
        public ItemProvider getItemProvider(Player player) {
            ItemBuilder item = new ItemBuilder(Material.BOOK);

            // Determine current tier
            int tier = getHomeTier(player);
            int nextTier = tier + 1;

            if (tier >= 5) {
                item.setDisplayName("§a§lMore Homes (MAXED)");
                item.addLoreLines(" ");
                item.addLoreLines("§a§l(!) You already have the maximum number of homes! (!)");
                //item.addLoreLines("§7You can now set up to §a5 homes§7.");
            } else {
                int cost = getCostForTier(nextTier);

                item.setDisplayName("§c§lMore Homes §7(Tier " + nextTier + ")");
                item.addLoreLines(" ");
                item.addLoreLines("§7Want to set more homes?");
                item.addLoreLines("§7Upgrade to unlock §a" + nextTier + " total homes§7.");
                item.addLoreLines(" ");
                item.addLoreLines("§c§l(!) Click to buy for $" + ChatUtils.formatMoney(cost) + " (!)");
            }

            return item;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            int tier = getHomeTier(player);
            int nextTier = tier + 1;

            if (tier >= 5) {
                player.closeInventory();
                player.sendMessage("§cYou already have the maximum number of homes!");
                return;
            }

            int cost = getCostForTier(nextTier);
            double balance = StoinkCore.getEconomy().getBalance(player);

            if (balance < cost) {
                player.closeInventory();
                ChatUtils.sendMessage(player, "§cInsufficient funds, next home upgrade costs $" + ChatUtils.formatMoney(cost));
                return;
            }

            new ConfirmationGUI(player, "Buy More Homes (" + nextTier + " total) for $" + ChatUtils.formatMoney(cost),
                    () -> {
                        StoinkCore.getEconomy().withdrawPlayer(player, cost);
                        grantNextHomePermission(player, nextTier);
                        ChatUtils.sendMessage(player, "§aYou can now set up to " + nextTier + " homes!");
                    },
                    () -> new PMenuGUI(player).openWindow()
            ).openWindow();
        }

        private int getHomeTier(Player player) {
            if (player.hasPermission("essentials.sethome.multiple.five")) return 5;
            if (player.hasPermission("essentials.sethome.multiple.four")) return 4;
            if (player.hasPermission("essentials.sethome.multiple.three")) return 3;
            if (player.hasPermission("essentials.sethome.multiple.two")) return 2;
            return 1;
        }

        private void grantNextHomePermission(Player player, int nextTier) {
            removePermissions(player);
            switch (nextTier) {
                //case 1 -> PlayerUtils.givePermission(player, "essentials.sethome.multiple");
                case 2 -> PlayerUtils.givePermission(player, "essentials.sethome.multiple.two");
                case 3 -> PlayerUtils.givePermission(player, "essentials.sethome.multiple.three");
                case 4 -> PlayerUtils.givePermission(player, "essentials.sethome.multiple.four");
                case 5 -> PlayerUtils.givePermission(player, "essentials.sethome.multiple.five");
            }
        }

        private void removePermissions(Player player){
            PlayerUtils.removePermission(player, "essentials.sethome.multiple.two");
            PlayerUtils.removePermission(player, "essentials.sethome.multiple.three");
            PlayerUtils.removePermission(player, "essentials.sethome.multiple.four");
            PlayerUtils.removePermission(player, "essentials.sethome.multiple.five");
        }

        private int getCostForTier(int tier) {
            // Example progression curve
            return switch (tier) {
                //case 1 -> 10000;
                case 2 -> 50000;
                case 3 -> 150000;
                case 4 -> 300000;
                case 5 -> 500000;
                default -> 0;
            };
        }
    }

}
