package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

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
        if (EnterpriseManager.getEnterpriseManager().getEnterpriseList().isEmpty()) {
            sender.sendMessage("§7There are currently no enterprises.");
            return;
        }

        sender.sendMessage("§a== Top Enterprises ==");
        AtomicInteger rank = new AtomicInteger(1);

        EnterpriseManager.getEnterpriseManager().getEnterpriseList().stream()
                .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                .limit(10)
                .forEachOrdered(e -> {
                    int r = rank.getAndIncrement();
                    sender.sendMessage("§e#" + r + " §f" + e.getName() + " §7(Net: §a$" + String.format("%.2f", e.getNetWorth()) + "§7)");
                });
    }
}
