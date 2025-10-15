package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class JoinSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getUsage() {
        return "/enterprise join";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return List.of();
        Player player = (Player) sender;

        if (args.length == 1) {
            return EnterpriseManager.getEnterpriseManager().getInviters(player.getUniqueId()).stream()
                    .map(inviter -> EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(inviter))
                    .filter(Objects::nonNull)
                    .map(Enterprise::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();

        // Already in an enterprise?
        if (em.isInEnterprise(playerUUID)) {
            ChatUtils.sendMessage(player,"§cYou're already in an enterprise. Use /enterprise resign first.");
            return;
        }

        // Get all invites TO this player
        Map<UUID, UUID> allInvites = em.getAllInvites();
        List<Enterprise> invitedTo = allInvites.entrySet().stream()
                .filter(entry -> entry.getKey().equals(playerUUID))
                .map(entry -> em.getEnterpriseByMember(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (invitedTo.isEmpty()) {
            ChatUtils.sendMessage(player,"§cYou have no pending enterprise invites.");
            return;
        }

        // If no enterprise name given and more than 1 invite → list choices
        if (args.length == 1 && invitedTo.size() > 1) {
            ChatUtils.sendMessage(player,"§eYou have multiple invites. Use /enterprise join <name> to choose.");
            for (Enterprise e : invitedTo) {
                ChatUtils.sendMessage(player,"§7 - §a" + e.getName());
            }
            return;
        }

        // If only one invite and no args → auto-join
        Enterprise toJoin = null;
        if (args.length == 1 && invitedTo.size() == 1) {
            toJoin = invitedTo.get(0);
        }

        // If enterprise name provided → validate it
        if (args.length >= 2) {
            String targetName = args[1];
            for (Enterprise e : invitedTo) {
                if (e.getName().equalsIgnoreCase(targetName)) {
                    toJoin = e;
                    break;
                }
            }

            if (toJoin == null) {
                ChatUtils.sendMessage(player,"§cYou're not invited to an enterprise named §e" + targetName);
                return;
            }
        }

        if (toJoin == null) {
            ChatUtils.sendMessage(player,"§cInvalid join command usage. Try /enterprise join <enterpriseName>");
            return;
        }

        // Perform join
        toJoin.hireEmployee(playerUUID);
        em.clearInvite(playerUUID, toJoin.getCeo()); // Optional: remove only this invite

        ChatUtils.sendMessage(player,"§aYou have joined §e" + toJoin.getName() + "§a! Welcome to the grind.");
    }
}
