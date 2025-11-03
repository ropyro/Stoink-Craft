package com.stoinkcraft.misc.daily;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class DailyGUI {

    private Player opener;

    public DailyGUI(Player player){
        this.opener = player;
    }


    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # # # R # # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.CLOCK)
                        .setDisplayName(" Daily Reward ")
                        .addLoreLines(" ")
                        .addLoreLines("Your daily treat!")))
                .addIngredient('R', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        if(DailyManager.INSTANCE.canClaimDaily(opener)){
                            return new ItemBuilder(Material.CHEST)
                                    .setDisplayName(" Daily Reward ")
                                    .addLoreLines(" ")
                                    .addLoreLines("(!) Click here to claim (!)");
                        }
                        return new ItemBuilder(Material.BARRIER)
                                .setDisplayName(" Can be claimed again tomorrow! ")
                                .addLoreLines(" ");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        DailyManager.INSTANCE.claimDaily(player);
                        player.closeInventory();
                    }
                })
                .build();

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Daily Reward")
                .setGui(gui)
                .build();
        window.open();
    }
}
