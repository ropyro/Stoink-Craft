package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.guis.TopEnterprisesGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class TopSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getUsage() {
        return "/enterprise top";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseList().stream()
               // .filter(e -> !(e instanceof ServerEnterprise))
                .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                .limit(10)
                .toList();


        if (enterprises.isEmpty()) {
            player.sendMessage("§7There are currently no enterprises.");
            return;
        }

        TopEnterprisesGUI.getInstance().openWindow(player, enterprises);
    }
}
