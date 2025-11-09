package com.stoinkcraft.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import com.stoinkcraft.utils.guis.ConfirmationGUI;
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

public class ShareMarketGUI {
    private Player opener;

    public ShareMarketGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # $ # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§6Stonks Marketplace")
                        .addLoreLines(" ")
                        .addLoreLines("§7Browse enterprises currently offering")
                        .addLoreLines("§7public shares on the open market.")
                        .addLoreLines(" ")
                        .addLoreLines("§eCompare values, spot trends, and invest wisely!")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("§6Your Portfolio")
                                .addLoreLines(" ")
                                .addLoreLines("§7View and manage all the shares")
                                .addLoreLines("§7you currently own.")
                                .addLoreLines(" ")
                                .addLoreLines("§eClick to open your portfolio.");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new SharePortfolioGUI(opener).openWindow();
                    }
                })
                .build();

        for (Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            if(enterprise.getAvailableShares() == 0) continue;
            if (enterprise.getOutstandingShares() < SCConstants.MAX_SHARES) {
                gui.addItems(new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("§e" + enterprise.getName() + " §7Stock")
                                .addLoreLines(" ")
                                .addLoreLines("§7Current Price: §a$" + ChatUtils.formatMoney(enterprise.getShareValue()))
                                .addLoreLines("§7Available Shares: §f" + (SCConstants.MAX_SHARES - enterprise.getOutstandingShares()))
                                .addLoreLines(" ")
                                .addLoreLines("§f(!) §bLeft §fclick to purchase one share §f(!)")
                                .addLoreLines("§f(!) §bRight §fclick to see stock history for (!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(clickType.isRightClick()){
                            new ShareGraphGUI(player, enterprise.getID()).openWindow();
                        }else if(clickType.isLeftClick()){
                            new ConfirmationGUI(player, "Buy Share of, " + enterprise.getName(),
                                    () -> ShareManager.getInstance().buyShare(opener, enterprise, 1),
                                    () -> new SharePortfolioGUI(opener).openWindow()).openWindow();
                        }
                    }
                });
            }
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Stonks Marketplace")
                .setGui(gui)
                .build();
        window.open();
    }

}
