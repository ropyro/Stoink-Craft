package com.stoinkcraft.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.UUID;

public class TopEnterprisesGUI {

    private final static TopEnterprisesGUI instance;

    public void openWindow(Player player, List<Enterprise> enterprises){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # . # # # #",
                        "# # # . . . # # #",
                        "# # . . . . . # #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" "))) // Filler
                .build();

        // Add top enterprises to slots 1–6
        for (int i = 0; i < enterprises.size(); i++) {
            Enterprise e = enterprises.get(i);
            int rank = i + 1;

            gui.addItems(new EnterpriseItem(e, rank));
        }

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§a§lTop Enterprises")
                .setGui(gui)
                .build();
        window.open();
    }

    public class EnterpriseItem extends AbstractItem {

        Enterprise e;
        int rank;

        private EnterpriseItem(Enterprise e, int rank){
            this.e = e;
            this.rank = rank;
        }

        @Override
        public ItemProvider getItemProvider() {
            String displayName = "§e#" + rank + " §a" + e.getName();
            String netWorth = String.format("%.2f", e.getNetWorth());
            String balance = String.format("%.2f", e.getBankBalance());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            OfflinePlayer ceo = Bukkit.getOfflinePlayer(e.getCeo());
            meta.setOwningPlayer(ceo);
            skull.setItemMeta(meta);

            ItemBuilder item = new ItemBuilder(skull)
                    .setDisplayName(displayName)
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fBalance: §a$" + balance)
                    .addLoreLines(" §a• §fNet Worth: §a$" + netWorth)
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fCEO: §a" + Bukkit.getOfflinePlayer(e.getCeo()).getName())
                    .addLoreLines(" §a• §fEmployees §a" + e.getMembers().keySet().size() + "/" + EnterpriseManager.getEnterpriseManager().getMaximumEmployees() + "§f:");

            for(UUID member : e.getMembers().keySet()){
                if(member.equals(e.getCeo())) continue;
                String name = Bukkit.getOfflinePlayer(member).getPlayer().getName();
                item.addLoreLines(" §a• §f" + name);
            }

            return item;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                if(e.getWarp() == null){
                    player.closeInventory();
                    player.sendMessage("Warp not set for this enterprise.");
                }else{
                    player.closeInventory();
                    player.teleport(e.getWarp());
                    player.sendMessage("Teleported to " + e.getName() + "'s warp.");
                }
            }
        }
    }

    public static TopEnterprisesGUI getInstance(){
        return instance;
    }

    static{
        instance = new TopEnterprisesGUI();
    }
}
