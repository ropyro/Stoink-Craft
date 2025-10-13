package com.stoinkcraft.enterprise.commands;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TopCeoCMD implements CommandExecutor {

    public TopCeoCMD() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        // /topceo update
        if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
            StoinkCore.updateTopCeoNpcs();
            sender.sendMessage(ChatColor.GREEN + "Top CEO NPCs updated.");
            return true;
        }

        // /topceo move <1-3>
        if (args.length == 2 && args[0].equalsIgnoreCase("move")) {
            int position;
            try {
                position = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a number from 1 to 3.");
                return true;
            }

            if (position < 1 || position > 3) {
                player.sendMessage(ChatColor.RED + "Leaderboard position must be between 1 and 3.");
                return true;
            }

            NPC npc = StoinkCore.getNpcByPosition(position);
            if (npc == null) {
                player.sendMessage(ChatColor.RED + "Top CEO NPC for position " + position + " not found.");
                return true;
            }

            npc.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage(ChatColor.GREEN + "Moved Top CEO NPC #" + position + " to your location.");
            return true;
        }

        // /topceo spawn <1-3>
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            int position;
            try {
                position = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a number from 1 to 3.");
                return true;
            }

            if (position < 1 || position > 3) {
                player.sendMessage(ChatColor.RED + "Leaderboard position must be between 1 and 3.");
                return true;
            }

            List<Enterprise> top = EnterpriseManager
                    .getEnterpriseManager()
                    .getEnterpriseList()
                    .stream()
                    .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                    .collect(Collectors.toList());

            if (top.size() < position) {
                player.sendMessage(ChatColor.RED + "Not enough enterprises yet.");
                return true;
            }

            Enterprise targetEnt = top.get(position - 1);
            OfflinePlayer ceo = Bukkit.getOfflinePlayer(targetEnt.getCeo());
            String npcName = ceo.getName() != null ? ceo.getName() : "CEO";

            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
            npc.spawn(player.getLocation());

            String displayName = "#" + position + " " + ChatColor.GREEN + ChatColor.BOLD + targetEnt.getName();
            npc.setName(displayName);

            npc.data().setPersistent("top_ceo_position", position);

            SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(npcName);
            skinTrait.run();

            npc.getOrAddTrait(LookClose.class).toggle();

            player.sendMessage(ChatColor.GREEN + "Spawned " + npc.getName());
            return true;
        }

        // Invalid usage
        player.sendMessage(ChatColor.RED + "Usage: /topceo <spawn|move|update> <1-3>");
        return true;
    }


    public static String getSuffix(int number) {
        return switch (number) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
