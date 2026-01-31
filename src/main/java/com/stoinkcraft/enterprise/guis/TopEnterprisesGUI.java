package com.stoinkcraft.enterprise.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.enterprise.reputation.ReputationCalculator;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TopEnterprisesGUI {

    private final static TopEnterprisesGUI instance;

    public void openWindow(Player player, List<Enterprise> enterprises) {
        // Convert enterprises to Items with their ranks
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < enterprises.size(); i++) {
            Enterprise e = enterprises.get(i);
            int rank = i + 1;
            items.add(new EnterpriseItem(e, rank));
        }

        Item border = new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" "));

        // Create the paged GUI with podium structure
        PagedGui<Item> gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # x # # # #",
                        "# # # x x x # # #",
                        "# # x x x x x # #",
                        "# < # # # # # > #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', border)
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
                .setContent(items)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§8Top Enterprises")
                .setGui(gui)
                .build();
        window.open();
    }

    // Navigation item to go to the previous page
    public static class BackItem extends PageItem {

        public BackItem() {
            super(false);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder;
            if(gui.hasPreviousPage()){
                builder = new ItemBuilder(Material.ARROW);
                builder.setDisplayName("§cPrevious Page")
                        .addLoreLines("§7Go to page §e" + gui.getCurrentPage() + "§7/§e" + gui.getPageAmount());
            }else{
                builder = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE);
                builder.setDisplayName(" ");
            }
            return builder;
        }
    }

    // Navigation item to go to the next page
    public static class ForwardItem extends PageItem {

        public ForwardItem() {
            super(true);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder;
            if(gui.hasNextPage()){
                builder = new ItemBuilder(Material.ARROW);
                builder.setDisplayName("§aNext Page")
                        .addLoreLines("§7Go to page §e" + (gui.getCurrentPage() + 2) + "§7/§e" + gui.getPageAmount());
            }else{
                builder = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE);
                builder.setDisplayName(" ");
            }
            return builder;
        }
    }

    public class EnterpriseItem extends AbstractItem {

        Enterprise e;
        int rank;

        private EnterpriseItem(Enterprise e, int rank) {
            this.e = e;
            this.rank = rank;
        }

        @Override
        public ItemProvider getItemProvider() {
            String displayName = "§e#" + rank + " §a" + e.getName() + " §f(§a" + ChatUtils.formatMoney(e.getNetWorth()) + "§f)";
            String netWorth = ChatUtils.formatMoney(e.getNetWorth());
            double reputation = e.getReputation();
            String balance = ChatUtils.formatMoney(e.getBankBalance());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            PlayerProfile profile = e instanceof ServerEnterprise ? Bukkit.createPlayerProfile("CEO") : Bukkit.createPlayerProfile(e.getCeo());
            meta.setOwnerProfile(profile);
            skull.setItemMeta(meta);

            ItemBuilder item = new ItemBuilder(skull)
                    .setDisplayName(displayName);

            if (e instanceof ServerEnterprise) {
                item.addLoreLines(" ")
                        .addLoreLines(" §a• §fEmployees §a" + (e.getMembers().keySet().size() - 1))
                        .addLoreLines(" ")
                        .addLoreLines("§c(!) THIS IS A SERVER OWNED ENTERPRISE (!)");
            } else {
                item.addLoreLines(" ")
                        .addLoreLines(" §a• §fBalance: §a$" + balance)
                        .addLoreLines(" §a• §fNet Worth: §a$" + netWorth)
                        .addLoreLines(" §a• §fRepuation: §a" + reputation)
                        .addLoreLines(" §a• §fNetworth Multiplier: §a" + ReputationCalculator.getMultiplierDisplay(reputation))
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fCEO: §a" + Bukkit.getOfflinePlayer(e.getCeo()).getName())
                        .addLoreLines(" §a• §fEmployees §a" + e.getMembers().keySet().size() + "/" + EnterpriseManager.getEnterpriseManager().getMaximumEmployees() + "§f:");
                for (UUID member : e.getMembers().keySet()) {
                    if (member.equals(e.getCeo())) continue;
                    String name = Bukkit.getOfflinePlayer(member).getName();
                    item.addLoreLines(" §a• §f" + name);
                }
            }
            return item;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

        }
    }

    public static TopEnterprisesGUI getInstance() {
        return instance;
    }

    static {
        instance = new TopEnterprisesGUI();
    }
}