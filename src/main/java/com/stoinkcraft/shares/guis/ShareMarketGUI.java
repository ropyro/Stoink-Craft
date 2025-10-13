package com.stoinkcraft.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.shares.Share;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
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

import java.util.List;

public class ShareMarketGUI {
    private Player opener;

    public ShareMarketGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # $ # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.CLOCK)
                        .setDisplayName(" Stonks For Sale")
                        .addLoreLines(" ")
                        .addLoreLines("Your currently owned stonks")
                        .addLoreLines("can be viewed & managed in")
                        .addLoreLines("this menu!")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("Your Portfolio");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new ShareViewerGUI(opener).openWindow();
                    }
                })
                .build();

        for(Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()){
            if(enterprise.getOutstandingShares() < SCConstants.MAX_SHARES){
                gui.addItems(new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("Share of: " + enterprise.getName())
                                .addLoreLines(" ")
                                .addLoreLines("Cost: $" + ChatUtils.formatMoney(enterprise.getShareValue()))
                                .addLoreLines(" ")
                                .addLoreLines("(!) Click here to buy this share (!) ");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        ShareManager.getInstance().buyShare(opener, enterprise, 1);
                        new ShareViewerGUI(opener).openWindow();
                    }
                });
            }
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Stonks For Sale")
                .setGui(gui)
                .build();
        window.open();
    }
}
