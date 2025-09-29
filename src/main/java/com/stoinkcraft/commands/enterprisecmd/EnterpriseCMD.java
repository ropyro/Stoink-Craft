package com.stoinkcraft.commands.enterprisecmd;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.commands.enterprisecmd.subcommands.*;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnterpriseCMD implements CommandExecutor {

    private StoinkCore plugin;

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public EnterpriseCMD(StoinkCore plugin){
        this.plugin = plugin;
        registerSubcommand(new CreateSubCommand());
        registerSubcommand(new InfoSubCommand());
        registerSubcommand(new InviteSubCommand());
        registerSubcommand(new JoinSubCommand());
        registerSubcommand(new ListSubCommand());
        registerSubcommand(new PromoteSubCommand());
        registerSubcommand(new ResignSubCommand());
        registerSubcommand(new ReloadSubCMD(plugin));
    }

    private void registerSubcommand(SubCommand sub) {
        subcommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§eEnterprise Commands:");
            for (SubCommand sub : subcommands.values()) {
                sender.sendMessage("§7 - " + sub.getUsage());
            }
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage("§cUnknown subcommand. Try /enterprise help");
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    public Map<String, SubCommand> getSubcommands() {
        return subcommands;
    }

}
