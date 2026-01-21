package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DemoteSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "demote";
    }

    @Override
    public String getUsage() {
        return "/enterprise demote <player>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (e == null) return List.of();

        if (args.length == 1) {
            return e.getExecutives().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null && name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        if (args.length < 2) {
            ChatUtils.sendMessage(player, "§cUsage: " + getUsage());
            return;
        }

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (e == null) {
            ChatUtils.sendMessage(player, "§cYou're not in an enterprise.");
            return;
        }

        if (!e.getMemberRole(player.getUniqueId()).equals(Role.CEO)) {
            ChatUtils.sendMessage(player, "§cOnly the CEO can demote members!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetUUID = target.getUniqueId();

        if (!e.isMember(targetUUID)) {
            ChatUtils.sendMessage(player, "§cThat player is not in your enterprise.");
            return;
        }

        Role currentRole = e.getMemberRole(targetUUID);
        if (currentRole.equals(Role.CEO)) {
            ChatUtils.sendMessage(player, "§cYou cannot demote the CEO.");
            return;
        }

        if (currentRole.equals(Role.EMPLOYEE)) {
            ChatUtils.sendMessage(player, "§cThat player is already an Employee.");
            return;
        }

        e.setMemberRole(targetUUID, Role.EMPLOYEE);
        ChatUtils.sendMessage(player, "§a" + target.getName() + " has been demoted to Employee.");

        if (target.isOnline()) {
            target.getPlayer().sendMessage("§cYou have been demoted to §e§lEmployee §cin §b" + e.getName() + "§c.");
        }
    }
}
