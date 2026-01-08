package com.stoinkcraft.enterprise.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.shares.Share;
import com.stoinkcraft.enterprise.shares.ShareAnalytics;
import com.stoinkcraft.enterprise.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
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

import java.time.Duration;
import java.util.List;

public class SharePortfolioGUI {
    private Player opener;

    public SharePortfolioGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # G # $ # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§6Stonk Portfolio")
                        .addLoreLines(" ")
                        .addLoreLines("§7View and manage all of your")
                        .addLoreLines("§7current stock holdings here.")
                        .addLoreLines(" ")
                        .addLoreLines("§eTrack performance, growth,")
                        .addLoreLines("§eand sell shares anytime.")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("§6Buy Shares")
                                .addLoreLines(" ")
                                .addLoreLines("§7Browse enterprises currently")
                                .addLoreLines("§7offering public shares for sale.")
                                .addLoreLines(" ")
                                .addLoreLines("§eInvest wisely. Profit steadily.");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new ShareMarketGUI(opener).openWindow();
                    }
                })
                .addIngredient('G', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CLOCK)
                                .setDisplayName("§6Portfolio Graph")
                                .addLoreLines(" ")
                                .addLoreLines("§7Browse enterprises currently")
                                .addLoreLines("§7offering public shares for sale.")
                                .addLoreLines(" ")
                                .addLoreLines("§eInvest wisely. Profit steadily.");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new PortfolioGraphGUI(opener).openWindow();
                    }
                })
                .build();

        for(Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()){
            List<Share> shares = ShareManager.getInstance().getPlayersShares(opener, enterprise);
            for(Share share : shares){
                gui.addItems(new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        double purchasePrice = share.getPurchasePrice();
                        double currentPrice = enterprise.getShareValue();

                        double percentSincePurchase = ShareAnalytics.getPercentChangeSincePurchase(share);
                        double percentLastHour = ShareAnalytics.getPercentChangeOverTime(share, Duration.ofHours(1).toMillis());
                        double percentLastDay = ShareAnalytics.getPercentChangeOverTime(share, Duration.ofDays(1).toMillis());
                        double percentLastWeek = ShareAnalytics.getPercentChangeOverTime(share, Duration.ofDays(7).toMillis());

                        String changeColor = percentSincePurchase > 0 ? "§a" : (percentSincePurchase < 0 ? "§c" : "§7");
                        String percentString = changeColor + ChatUtils.formatPercent(percentSincePurchase);

                        // Adapt to enterprise age — skip "last day" if the enterprise is younger.
                        long ageMillis = System.currentTimeMillis() - enterprise.getPriceHistory().get(0).getTimestamp();
                        boolean showDay = ageMillis >= Duration.ofDays(1).toMillis();
                        boolean showHour = ageMillis >= Duration.ofHours(1).toMillis();
                        boolean showWeek = ageMillis >= Duration.ofDays(7).toMillis();

                        ItemBuilder builder = new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("§6Share of §e" + enterprise.getName())
                                .addLoreLines(" ")
                                .addLoreLines("§7Purchase Price: §f$" + ChatUtils.formatMoney(purchasePrice))
                                .addLoreLines("§7Current Value: §f$" + ChatUtils.formatMoney(currentPrice))
                                .addLoreLines("§7Change Since Purchase: " + percentString)
                                .addLoreLines(" ");

                        if (showHour)
                            builder.addLoreLines("§7Last Hour: " + ChatUtils.colorizePercent(percentLastHour));
                        if (showDay)
                            builder.addLoreLines("§7Last 24h: " + ChatUtils.colorizePercent(percentLastDay));
                        if (showWeek)
                            builder.addLoreLines("§7Last 7d: " + ChatUtils.colorizePercent(percentLastWeek));

                        builder.addLoreLines("§7Date of Purchase: §f" + ChatUtils.formatDate(share.getPurchaseDate()))
                                .addLoreLines(" ")
                                .addLoreLines("§f(!) §bLeft §fclick to sell for: §a$" + ChatUtils.formatMoney(currentPrice) + " §f(!)")
                                .addLoreLines("§f(!) §bRight §fclick to see stock history for (!)");
                        return builder;
                    }


                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(clickType.isRightClick()){
                            new ShareGraphGUI(player, share).openWindow();
                        }else if(clickType.isLeftClick()){
                            new ConfirmationGUI(player,
                                    "Sell share of " + enterprise.getName(),
                                    () -> ShareManager.getInstance().sellShare(player, share),
                                    () -> openWindow()).openWindow();
                        }
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
