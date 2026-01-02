package com.stoinkcraft.enterprise.commands.serverenterprisecmd;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ServerEntTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager().getEnterpriseList()
                .stream()
                .filter(e -> e instanceof ServerEnterprise)
                .toList();

        if (args.length == 2 && args[0].equalsIgnoreCase("setwarp")) {
            String current = args[1].toLowerCase();
            return enterprises.stream()
                    .map(Enterprise::getName)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .sorted()
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("givebooster")) {
            return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("rebuild")) {
            return StoinkCore.getInstance().getEnterpriseManager().getEnterpriseList().stream().map(e -> e.getName()).toList();
        }

        if (args.length == 1) {
            return List.of("setwarp", "updateeco", "givebooster", "rebuild");
        }

        return List.of();
    }

}
