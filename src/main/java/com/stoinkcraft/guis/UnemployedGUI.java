package com.stoinkcraft.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
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

public class UnemployedGUI {

    private Player opener;

    public UnemployedGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # A B C # D # #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('A', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GRASS_BLOCK)
                                .setDisplayName(" §aFarmerLLC ")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to join (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        EnterpriseManager.getEnterpriseManager().getEnterpriseByName("FarmerLLC").hireEmployee(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage("You have joined the FarmerLLC!");
                    }
                })
                .addIngredient('B', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.IRON_PICKAXE)
                                .setDisplayName(" §aMinerCorp ")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to join (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        EnterpriseManager.getEnterpriseManager().getEnterpriseByName("MinerCorp").hireEmployee(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage("You have joined the MinerCorp!");
                    }
                })
                .addIngredient('C', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.ZOMBIE_HEAD)
                                .setDisplayName(" §aHunterInc ")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to join (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        EnterpriseManager.getEnterpriseManager().getEnterpriseByName("HunterInc").hireEmployee(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage("You have joined the MinerCorp!");
                    }
                })
                .addIngredient('D', new SimpleItem(new ItemBuilder(Material.DIAMOND)
                        .setDisplayName(" §aFound Private Enterprise ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fTo create a private enterprise, use ")
                        .addLoreLines(" §f/enterprise create <name> ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fCost: " + ChatUtils.formatMoney(SCConstants.ENTERPRISE_FOUNDING_COST))))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName(" §aEnterprise Help ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §f/enterprise setwarp - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise info - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise resign - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise disband - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise warp [name] - teleports to an enterprise")
                        .addLoreLines(" §a• §f/enterprise invite <player> - invite new members")
                ))
                .build();

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Find Employment Menu")
                .setGui(gui)
                .build();
        window.open();
    }
}
