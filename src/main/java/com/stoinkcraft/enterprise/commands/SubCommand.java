package com.stoinkcraft.enterprise.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName(); // e.g. "create", "info"
    String getUsage(); // for help menu
    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of(); // By default, nothing
    }
}