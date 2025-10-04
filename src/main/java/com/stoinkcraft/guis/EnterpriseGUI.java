package com.stoinkcraft.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
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

public class EnterpriseGUI {

    private Player opener;
    private Enterprise enterprise;

    public EnterpriseGUI(Player opener, Enterprise enterprise){
        this.opener = opener;
        this.enterprise = enterprise;
    }

    public void openWindow(){
        String netWorth = String.format("%.2f", enterprise.getNetWorth());
        String balance = String.format("%.2f", enterprise.getBankBalance());

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # A B C D E # #",
                        "# # # # # # # # #",
                        "# . . . X X X X #",
                        "# X X X X X X X #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('X', new SimpleItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .setDisplayName(" §cThis member slot is locked ")))
                .addIngredient('A', new SimpleItem(new ItemBuilder(Material.BOOK)
                        .setDisplayName(" §aHiring coming soon... ")))
                .addIngredient('B', new SimpleItem(new ItemBuilder(Material.CHEST)
                        .setDisplayName(" §aBank withdrawal coming soon. ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fBalance: " + balance)))
                .addIngredient('C', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName(" §aEnterprise Help ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §f/enterprise - opens this menu")
                        .addLoreLines(" §a• §f/enterprise setwarp - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise info - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise resign - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise disband - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise warp [name] - teleports to an enterprise")
                        .addLoreLines(" §a• §f/enterprise invite <player> - invite new members")
                ))
                .addIngredient('D', new SimpleItem(new ItemBuilder(Material.GOLD_INGOT)
                        .setDisplayName(" §aNetworth ")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fNetworth: " + netWorth)
                ))
                .addIngredient('E', new SimpleItem(new ItemBuilder(Material.GRASS_BLOCK)
                        .setDisplayName(" §aClaiming coming soon... ")
                ))
                .build();

        ItemStack openerskull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) openerskull.getItemMeta();
        meta.setOwningPlayer(opener);
        openerskull.setItemMeta(meta);

        ItemBuilder openerhead = new ItemBuilder(openerskull)
                .setDisplayName(" §aYOU ")
                .addLoreLines(" ")
                .addLoreLines(" §a• §fRole: " + enterprise.getMemberRole(opener.getUniqueId()))
                .addLoreLines(" ");

        gui.addItems(new SimpleItem(openerhead));

        // Add top enterprises to slots 1–6
        for (UUID uuid : enterprise.getMembers().keySet()) {
            if(uuid.equals(opener.getUniqueId())) continue;
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);

            ItemStack memberSkull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberSkullMeta = (SkullMeta) memberSkull.getItemMeta();
            memberSkullMeta.setOwningPlayer(member);
            memberSkull.setItemMeta(memberSkullMeta);

            ItemBuilder memberHead = new ItemBuilder(memberSkull)
                    .setDisplayName(" §a" + member.getName())
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fRole: " + enterprise.getMemberRole(uuid))
                    .addLoreLines(" ");

            gui.addItems(new SimpleItem(memberHead));
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8" + enterprise.getName())
                .setGui(gui)
                .build();
        window.open();
    }
}
