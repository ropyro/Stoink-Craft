package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class InfoSubCommand implements SubCommand {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getUsage() {
        return "/enterprise info [name]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        Enterprise e;

        if (args.length == 1) {
            e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        } else {
            e = EnterpriseManager.getEnterpriseManager().getEnterpriseByName(args[1]);
        }

        if (e == null) {
            ChatUtils.sendMessage(player,"§cEnterprise not found or you're not in one.");
            return;
        }

        ChatUtils.sendMessage(player,"§a== Enterprise Info: §e" + e.getName() + "§a ==");
        ChatUtils.sendMessage(player,"§7CEO: " + getNameFromUUID(e.getCeo()));

        List<UUID> employees = e.getEmployees();
        if (!employees.isEmpty()) {
            StringBuilder builder = new StringBuilder("§7Employees: ");
            for (UUID uuid : employees) {
                builder.append(getNameFromUUID(uuid)).append(", ");
            }
            ChatUtils.sendMessage(player,builder.substring(0, builder.length() - 2)); // remove last comma
        }

        ChatUtils.sendMessage(player,"§7Bank Balance: §a$" + e.getBankBalance());
        ChatUtils.sendMessage(player,"§7Net Worth: §a$" + e.getNetWorth());
    }

    private String getNameFromUUID(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return (p.getName() != null) ? p.getName() : "Unknown";
    }
}
