package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TopSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getUsage() {
        return "/enterprise top";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseList().stream()
                .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                .limit(10)
                .toList();

        if (enterprises.isEmpty()) {
            player.sendMessage("§7There are currently no enterprises.");
            return;
        }

        // GUI setup
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

            gui.addItems(new SimpleItem(item));
        }

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§a§lTop Enterprises")
                .setGui(gui)
                .build();
        window.open();
    }
}
