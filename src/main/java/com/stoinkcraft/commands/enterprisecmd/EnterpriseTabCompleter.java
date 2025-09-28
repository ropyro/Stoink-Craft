package com.stoinkcraft.commands.enterprisecmd;

import com.stoinkcraft.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnterpriseTabCompleter implements TabCompleter {

    private final Map<String, SubCommand> subcommands;
    public EnterpriseTabCompleter(Map<String, SubCommand> subcommands) {
        this.subcommands = subcommands;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // First argument — show subcommands
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            return subcommands.keySet().stream()
                    .filter(name -> name.startsWith(current))
                    .sorted()
                    .toList();
        }

        // Delegating to matching subcommand
        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }
}
