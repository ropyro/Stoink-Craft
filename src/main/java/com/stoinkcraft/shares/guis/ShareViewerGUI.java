package com.stoinkcraft.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.market.values.EntityValue;
import com.stoinkcraft.shares.Share;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

import static com.stoinkcraft.market.MarketManager.JobType.FISHING;
import static com.stoinkcraft.market.MarketManager.JobType.HUNTING;

public class ShareViewerGUI {
    private Player opener;

    public ShareViewerGUI(Player opener){
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
                        .setDisplayName(" Stonk Menu")
                        .addLoreLines(" ")
                        .addLoreLines("Your currently owned stonks")
                        .addLoreLines("can be viewed & managed in")
                        .addLoreLines("this menu!")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("Buy Shares");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new ShareMarketGUI(opener).openWindow();
                    }
                })
                .build();

        for(Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()){
            List<Share> shares = ShareManager.getInstance().getPlayersShares(opener, enterprise);
            for(Share share : shares){
                gui.addItems(new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("Share of: " + enterprise.getName())
                                .addLoreLines(" ")
                                .addLoreLines("Purchase Price: $" + ChatUtils.formatMoney(share.getPurchasePrice()))
                                .addLoreLines("Current Value: $" + ChatUtils.formatMoney(enterprise.getShareValue()))
                                .addLoreLines("Date of Purchase: " + share.getPurchaseDate().toString())
                                .addLoreLines(" ")
                                .addLoreLines("(!) Click to sell for: $" + ChatUtils.formatMoney(enterprise.getShareValue()) + " (!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        ShareManager.getInstance().sellShare(player, share);
                        player.closeInventory();
                    }
                });
            }
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Stonk Portfolio")
                .setGui(gui)
                .build();
        window.open();
    }
}
