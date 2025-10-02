package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.guis.TopEnterprisesGUI;
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

        TopEnterprisesGUI.getInstance().openWindow(player, enterprises);
    }
}
