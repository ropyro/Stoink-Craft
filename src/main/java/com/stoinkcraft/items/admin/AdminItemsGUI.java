package com.stoinkcraft.items.admin;

import com.stoinkcraft.items.StoinkItem;
import com.stoinkcraft.items.StoinkItemRegistry;
import com.stoinkcraft.items.graveyard.SoulVoucherItem;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;

public class AdminItemsGUI {

    private static final String BULLET = " §7• ";
    private static final String ARROW = "§e▶ ";
    private static final String DIVIDER = " ";

    private final Player opener;
    private Window currentWindow;

    public AdminItemsGUI(Player opener) {
        this.opener = opener;
    }

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# 1 2 3 4 5 6 7 #",
                        "# 8 9 # # # # # #",
                        "# # # # # # # # #")
                .addIngredient('#', filler())
                .addIngredient('1', createItemSlot("small_booster"))
                .addIngredient('2', createItemSlot("medium_booster"))
                .addIngredient('3', createItemSlot("large_booster"))
                .addIngredient('4', createItemSlot("small_mine_bomb"))
                .addIngredient('5', createItemSlot("medium_mine_bomb"))
                .addIngredient('6', createItemSlot("large_mine_bomb"))
                .addIngredient('7', createItemSlot("fertilizer_bomb"))
                .addIngredient('8', createSoulVoucherSlot())
                .addIngredient('9', createItemSlot("graveyard_hound"))
                .build();

        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle("§8§lAdmin Items")
                .setGui(gui)
                .build();

        currentWindow.open();
    }

    private SimpleItem filler() {
        return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
    }

    private AbstractItem createItemSlot(String itemId) {
        StoinkItem stoinkItem = StoinkItemRegistry.getById(itemId);
        if (stoinkItem == null) {
            return new AbstractItem() {
                @Override
                public ItemProvider getItemProvider() {
                    return new ItemBuilder(Material.BARRIER)
                            .setDisplayName("§cItem not found: " + itemId);
                }

                @Override
                public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {}
            };
        }

        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder builder = new ItemBuilder(stoinkItem.getMaterial())
                        .setDisplayName(stoinkItem.getDisplayName())
                        .addLoreLines(DIVIDER);

                stoinkItem.getLore().forEach(builder::addLoreLines);

                builder.addLoreLines(DIVIDER);
                builder.addLoreLines("§aID: §7" + itemId);
                builder.addLoreLines(DIVIDER);
                builder.addLoreLines(ARROW + "§fLeft-click §7for 1 item");
                builder.addLoreLines(ARROW + "§fShift-click §7for 64 items");

                return builder;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                int amount = click.isShiftClick() ? 64 : 1;
                ItemStack item = stoinkItem.createItemStack(amount);
                giveItem(p, item);
                ChatUtils.sendMessage(p, "§a✔ Received " + amount + "x " + stoinkItem.getDisplayName());
            }
        };
    }

    private AbstractItem createSoulVoucherSlot() {
        StoinkItem stoinkItem = StoinkItemRegistry.getById("soul_voucher");
        if (stoinkItem == null || !(stoinkItem instanceof SoulVoucherItem soulVoucher)) {
            return createItemSlot("soul_voucher");
        }

        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(stoinkItem.getMaterial())
                        .setDisplayName(stoinkItem.getDisplayName())
                        .addLoreLines(DIVIDER)
                        .addLoreLines(stoinkItem.getLore().toArray(new String[0]))
                        .addLoreLines(DIVIDER)
                        .addLoreLines("§aID: §7soul_voucher")
                        .addLoreLines(DIVIDER)
                        .addLoreLines(ARROW + "§fLeft-click §7for 100 souls")
                        .addLoreLines(ARROW + "§fRight-click §7for 1,000 souls")
                        .addLoreLines(ARROW + "§fShift-click §7for 10,000 souls");
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                int soulAmount;
                if (click.isShiftClick()) {
                    soulAmount = 10000;
                } else if (click.isRightClick()) {
                    soulAmount = 1000;
                } else {
                    soulAmount = 100;
                }

                ItemStack item = soulVoucher.createItemStack(1, soulAmount);
                giveItem(p, item);
                ChatUtils.sendMessage(p, "§a✔ Received Soul Voucher worth §d" + String.format("%,d", soulAmount) + " souls");
            }
        };
    }

    private void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}
