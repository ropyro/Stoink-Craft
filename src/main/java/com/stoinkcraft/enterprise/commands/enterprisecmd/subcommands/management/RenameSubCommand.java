package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RenameSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public String getUsage() {
        return "/enterprise rename <name>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) return List.of();
            Player player = (Player)sender;
            return List.of(EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId()).getName());
        }
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        if (args.length < 2) {
            ChatUtils.sendMessage(player,"§cUsage: " + getUsage());
            return;
        }

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (e == null) {
            ChatUtils.sendMessage(player,"§cYou're not in an enterprise.");
            return;
        }

        Role role = e.getMemberRole(player.getUniqueId());
        if(role.equals(Role.EMPLOYEE)){
            ChatUtils.sendMessage(player,"§cOnly managers and above can rename the enterprise!");
            return;
        }

        StringBuilder entName = new StringBuilder("");
        for(int i = 1; i < args.length; i++){
            if(i == args.length - 1)
                entName.append(args[i]);
            else
                entName.append(args[i] + " ");
        }

        ChatUtils.sendMessage(player,"§a" + e.getName() + " §fhas been renamed to §a" + entName);
        e.setName(entName.toString());
        e.getJobSiteManager().getSkyriseSite().initializeEntryHologram();
    }
}
