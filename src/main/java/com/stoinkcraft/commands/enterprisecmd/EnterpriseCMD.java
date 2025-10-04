package com.stoinkcraft.commands.enterprisecmd;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.commands.enterprisecmd.subcommands.*;
import com.stoinkcraft.commands.enterprisecmd.subcommands.admin.ReloadSubCMD;
import com.stoinkcraft.commands.enterprisecmd.subcommands.management.*;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.guis.EnterpriseGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EnterpriseCMD implements CommandExecutor {

    private StoinkCore plugin;

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public EnterpriseCMD(StoinkCore plugin){
        this.plugin = plugin;
        registerSubcommand(new CreateSubCommand());
        registerSubcommand(new InfoSubCommand());
        registerSubcommand(new InviteSubCommand());
        registerSubcommand(new JoinSubCommand());
        registerSubcommand(new TopSubCommand());
        registerSubcommand(new PromoteSubCommand());
        registerSubcommand(new ResignSubCommand());
        registerSubcommand(new ReloadSubCMD(plugin));
        registerSubcommand(new DisbandSubCommand());
        registerSubcommand(new DelwarpSubCommand());
        registerSubcommand(new SetwarpSubCommand());
        registerSubcommand(new WarpSubCommand());
    }

    private void registerSubcommand(SubCommand sub) {
        subcommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        if(args.length == 0 && EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())){
            Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
            new EnterpriseGUI(player, enterprise).openWindow();
            return true;
        }else if(args.length == 0){
            player.sendMessage("§cYou are not in an enterprise. Try /enterprise help");
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
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
