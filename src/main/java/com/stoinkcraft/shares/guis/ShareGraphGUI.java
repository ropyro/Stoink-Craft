package com.stoinkcraft.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.PriceSnapshot;
import com.stoinkcraft.shares.Share;
import com.stoinkcraft.utils.ChatUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Displays a price graph for either a specific Share (with purchase marker)
 * or a general Enterprise (without marker).
 */
public class ShareGraphGUI {

    private final Player opener;
    private final Share share;
    private final Enterprise enterprise;
    private final boolean specificShare;

    // For a specific share (show diamond marker)
    public ShareGraphGUI(Player opener, Share share) {
        this.opener = opener;
        this.share = share;
        this.enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByID(share.getEnterpriseID());
        this.specificShare = true;
    }

    // For an enterprise in general (no diamond marker)
    public ShareGraphGUI(Player opener, UUID enterpriseID) {
        this.opener = opener;
        this.share = null;
        this.enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByID(enterpriseID);
        this.specificShare = false;
    }

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "# # # ? # $ # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§6" + enterprise.getName() + " Price Graph")
                        .addLoreLines(" ")
                        .addLoreLines("§7Visualizing recent share price changes for")
                        .addLoreLines("§e" + enterprise.getName())
                        .addLoreLines(" ")
                        .addLoreLines("§aGreen §7= rise  §cRed §7= fall  §eYellow §7= stable")
                        .addLoreLines(specificShare ? "§bDiamond = your purchase time" : "")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("§6Back")
                                .addLoreLines(" ")
                                .addLoreLines(specificShare
                                        ? "§7Return to your stonk portfolio."
                                        : "§7Return to the stonk marketplace.");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                        if (specificShare)
                            new SharePortfolioGUI(opener).openWindow();
                        else
                            new ShareMarketGUI(opener).openWindow();
                    }
                })
                .build();

        List<PriceSnapshot> history = enterprise.getPriceHistory();
        if (history == null || history.isEmpty()) {
            gui.addItems(new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName("§cNo price history available.")
                    .addLoreLines("§7This enterprise has not yet recorded prices.")));
        } else {
            long now = System.currentTimeMillis();
            long span = Duration.ofHours(9).toMillis();
            long interval = span / 9;
            double[] prices = new double[9];
            long[] times = new long[9];

            // Sample 9 points across history
            for (int i = 0; i < 9; i++) {
                long target = now - (interval * (9 - i - 1));
                PriceSnapshot nearest = history.stream()
                        .min(Comparator.comparingLong(p -> Math.abs(p.getTimestamp() - target)))
                        .orElse(history.get(history.size() - 1));
                prices[i] = nearest.getSharePrice();
                times[i] = nearest.getTimestamp();
            }

            // Add graph blocks
            for (int i = 0; i < 9; i++) {
                boolean purchasePoint = specificShare &&
                        Math.abs(times[i] - share.getPurchaseDate().getTime()) <= interval / 2;

                Material block = getGraphBlock(prices, i, purchasePoint);
                double change = getChangeSincePrev(prices, i);

                ItemBuilder item = new ItemBuilder(block)
                        .setDisplayName(purchasePoint ? "§bPurchase Time" : "§aPrice Snapshot")
                        .addLoreLines("§7Time: " + formatTimeAgo(times[i], now))
                        .addLoreLines("§7Value: §f$" + ChatUtils.formatMoney(prices[i]))
                        .addLoreLines("§7Change: " + ChatUtils.colorizePercent(change));

                if (purchasePoint)
                    item.addLoreLines(" ").addLoreLines("§bYou bought your share here!");

                gui.addItems(new SimpleItem(item));
            }
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8" + enterprise.getName() + " Price Graph")
                .setGui(gui)
                .build();
        window.open();
    }

    // ───────────── Graph Utilities ─────────────

    private Material getGraphBlock(double[] prices, int index, boolean purchasePoint) {
        if (purchasePoint) return Material.DIAMOND_BLOCK;
        if (index == 0) return Material.GRAY_STAINED_GLASS_PANE;
        double prev = prices[index - 1];
        double curr = prices[index];
        if (curr > prev) return Material.LIME_STAINED_GLASS_PANE;
        if (curr < prev) return Material.RED_STAINED_GLASS_PANE;
        return Material.YELLOW_STAINED_GLASS_PANE;
    }

    private double getChangeSincePrev(double[] prices, int index) {
        if (index == 0) return 0;
        double prev = prices[index - 1];
        if (prev == 0) return 0;
        return ((prices[index] - prev) / prev) * 100.0;
    }

    private String formatTimeAgo(long timestamp, long now) {
        long diff = now - timestamp;
        long minutes = diff / 60000;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}
