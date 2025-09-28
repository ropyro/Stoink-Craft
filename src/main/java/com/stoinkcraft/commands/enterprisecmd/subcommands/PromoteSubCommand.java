package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PromoteSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "promote";
    }

    @Override
    public String getUsage() {
        return "/enterprise promote <member>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return List.of();
        if (args.length != 1) return List.of();

        Player player = (Player) sender;
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) return List.of();

        return enterprise.getMembers().entrySet().stream()
                .filter(entry -> {
                    Role role = entry.getValue();
                    return role == Role.EMPLOYEE || role == Role.COO;
                })
                .map(entry -> {
                    UUID uuid = entry.getKey();
                    return Bukkit.getOfflinePlayer(uuid).getName(); // null-safe on Paper
                })
                .filter(Objects::nonNull)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .sorted()
                .toList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: " + getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        Role role = e.getMemberRole(player.getUniqueId());
        if(!role.equals(Role.CEO)){
            player.sendMessage("§cOnly the CEO can promote members of an enterprise!");
            return;
        }

        if(e.promoteMember(target.getUniqueId())){
            player.sendMessage(target.getName() + " has been promoted to " + e.getMemberRole(target.getUniqueId()) + "!");
        } else {
            player.sendMessage(target.getName() + " could not be promoted.");
        }
    }
}
