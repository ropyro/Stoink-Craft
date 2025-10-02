package com.stoinkcraft.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InviteSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getUsage() {
        return "/enterprise invite <player>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: " + getUsage());
            return;
        }

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (e == null) {
            player.sendMessage("§cYou're not in an enterprise.");
            return;
        }

        Role role = e.getMemberRole(player.getUniqueId());
        if(role.equals(Role.EMPLOYEE)){
            player.sendMessage("§cOnly managers and above can hire employees!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        if (EnterpriseManager.getEnterpriseManager().isInEnterprise(target.getUniqueId())) {
            player.sendMessage("§cThat player is already in an enterprise.");
            return;
        }

        EnterpriseManager.getEnterpriseManager().sendInvite(target.getUniqueId(), player.getUniqueId());
        player.sendMessage("§aInvite sent to §e" + target.getName());
        target.sendMessage("§aYou have been invited to join §e" + e.getName() + "§a! Use §e/enterprise join§a to accept.");
    }
}
