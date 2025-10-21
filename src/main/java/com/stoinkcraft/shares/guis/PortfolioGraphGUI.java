package com.stoinkcraft.shares.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.PriceSnapshot;
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
import java.util.*;
import java.util.stream.Collectors;

public class PortfolioGraphGUI {

    private final Player opener;

    public PortfolioGraphGUI(Player opener) {
        this.opener = opener;
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
                        .setDisplayName("§6Stock Graph")
                        .addLoreLines(" ")
                        .addLoreLines("§7Visualize your portfolio’s performance")
                        .addLoreLines("§7over time across all enterprises.")
                        .addLoreLines(" ")
                        .addLoreLines("§eGreen = growth")
                        .addLoreLines("§cRed = decline")
                        .addLoreLines("§7Yellow = stable")))
                .addIngredient('$', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CHEST)
                                .setDisplayName("§6Back to Portfolio")
                                .addLoreLines(" ")
                                .addLoreLines("§7Return to your list of")
                                .addLoreLines("§7owned stonks and shares.");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                        new SharePortfolioGUI(opener).openWindow();
                    }
                })
                .build();

        // Generate graph data for the opener
        double[] recentValues = PortfolioGraphUtils.getRecentPortfolioValues(opener.getUniqueId(), 9);

        // Add all graph blocks
        for (int col = 0; col < 9; col++) {
            Material blockType = PortfolioGraphUtils.getGraphBlock(recentValues, col);
            double value = recentValues[col];
            double change = PortfolioGraphUtils.getChangeSincePrev(recentValues, col);

            gui.addItems(new SimpleItem(new ItemBuilder(blockType)
                    .setDisplayName("§aPortfolio Value")
                    .addLoreLines("§7Time: " + PortfolioGraphUtils.formatTimeAgo(col))
                    .addLoreLines("§7Value: §f$" + ChatUtils.formatMoney(value))
                    .addLoreLines("§7Change: " + PortfolioGraphUtils.formatPercent(change))));
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Portfolio Graph")
                .setGui(gui)
                .build();

        window.open();
    }

    // ────────────────────────────────────────────────────────────────
    // 📊 Utility class for graph generation
    // ────────────────────────────────────────────────────────────────
    public static class PortfolioGraphUtils {

        public static double[] getRecentPortfolioValues(UUID playerId, int columns) {
            List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager().getEnterpriseList()
                    .stream()
                    .filter(e -> e.getMembers().containsKey(playerId))
                    .collect(Collectors.toList());

            if (enterprises.isEmpty()) return new double[columns];

            long now = System.currentTimeMillis();
            long interval = Duration.ofHours(1).toMillis(); // each column = 1 hour
            double[] values = new double[columns];

            for (int i = 0; i < columns; i++) {
                long timestamp = now - (interval * (columns - i - 1));

                double avg = enterprises.stream()
                        .mapToDouble(e -> getPriceAtTime(e, timestamp))
                        .average()
                        .orElse(0);

                values[i] = avg;
            }

            return values;
        }

        private static double getPriceAtTime(Enterprise enterprise, long timestamp) {
            List<PriceSnapshot> history = enterprise.getPriceHistory();
            if (history == null || history.isEmpty()) return enterprise.getShareValue();

            PriceSnapshot closest = history.stream()
                    .min(Comparator.comparingLong(s -> Math.abs(s.getTimestamp() - timestamp)))
                    .orElse(null);

            return closest != null ? closest.getSharePrice() : enterprise.getShareValue();
        }

        public static Material getGraphBlock(double[] all, int index) {
            if (index == 0) return Material.GRAY_STAINED_GLASS_PANE;
            double prev = all[index - 1];
            double curr = all[index];

            if (curr > prev) return Material.LIME_STAINED_GLASS_PANE;
            if (curr < prev) return Material.RED_STAINED_GLASS_PANE;
            return Material.YELLOW_STAINED_GLASS_PANE;
        }

        public static double getChangeSincePrev(double[] all, int index) {
            if (index == 0) return 0;
            double prev = all[index - 1];
            if (prev == 0) return 0;
            return ((all[index] - prev) / prev) * 100.0;
        }

        public static String formatTimeAgo(int index) {
            return (9 - index) + "h ago";
        }

        public static String formatPercent(double value) {
            String color = value > 0 ? "§a" : (value < 0 ? "§c" : "§7");
            return color + String.format("%.1f%%", value);
        }
    }
}
