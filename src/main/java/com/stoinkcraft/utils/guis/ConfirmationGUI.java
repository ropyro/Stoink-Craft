package com.stoinkcraft.utils.guis;

import com.stoinkcraft.StoinkCore;
import org.bukkit.Bukkit;
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


public class ConfirmationGUI {

    private final Player opener;
    private final String actionString;
    private final Runnable confirmationAction;
    private final Runnable cancelAction;
    private final Runnable returnAction;

    public ConfirmationGUI(Player opener, String actionString, Runnable confirmationAction, Runnable returnAction) {
        this(opener, actionString, confirmationAction, null, returnAction);
    }

    public ConfirmationGUI(Player opener, String actionString, Runnable confirmationAction, Runnable cancelAction, Runnable returnAction) {
        this.opener = opener;
        this.actionString = actionString;
        this.confirmationAction = confirmationAction;
        this.cancelAction = cancelAction;
        this.returnAction = returnAction;
    }


    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# Y Y Y # N N N #",
                        "# Y Y Y # N N N #",
                        "# Y Y Y # N N N #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('Y', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                                .setDisplayName("§aConfirm: §f" + actionString)
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to confirm (!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        Bukkit.getScheduler().runTaskLater(StoinkCore.getInstance(), () -> {
                            confirmationAction.run();
                            if (returnAction != null) {
                                returnAction.run();
                            }
                        }, 2L);
                    }
                }).addIngredient('N', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                                .setDisplayName("§cCancel: " + actionString)
                                .addLoreLines(" ")
                                .addLoreLines("§c(!) Click here to cancel (!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        Bukkit.getScheduler().runTaskLater(StoinkCore.getInstance(), () -> {
                            if (returnAction != null) {
                                returnAction.run();
                            }
                            if(cancelAction != null){
                                cancelAction.run();
                            }
                        }, 2L);
                    }
                }).build();

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Confirm Selection: " + actionString)
                .setGui(gui)
                .build();
        window.open();
    }
}
