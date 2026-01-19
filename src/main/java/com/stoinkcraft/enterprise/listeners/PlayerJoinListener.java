package com.stoinkcraft.enterprise.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.contracts.ActiveContract;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.guis.UnemployedGUI;
import com.stoinkcraft.misc.daily.DailyManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final StoinkCore plugin;

    public PlayerJoinListener(StoinkCore plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    new UnemployedGUI(player).openWindow();
                }
            };

            int delayTicks = 30 * 20;
            task.runTaskLater(plugin, delayTicks);
        }
        Bukkit.getScheduler().runTaskLater(StoinkCore.getInstance(), new Runnable() {
            @Override
            public void run() {
                sendMOTD(event.getPlayer());
            }
        }, 20*3);
    }

    private void sendMOTD(Player player) {
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        StoinkCore core = StoinkCore.getInstance();

        player.sendMessage("");
        player.sendMessage("§8§l» §6✦ §e§lWelcome back, §f" + player.getName() + "§e§l! §6✦");

        if (enterprise == null) {
            player.sendMessage("§8§l» §7Status: §cUnemployed §8| §7Use §b/enterprise §7to get started");
        } else {
            // Enterprise info line
            String netWorth = "§a$" + ChatUtils.formatMoneyNoCents(enterprise.getNetWorth());
            player.sendMessage("§8§l» §b" + enterprise.getName() + " §8| " + netWorth + " §8| " +
                    enterprise.getMemberRole(player.getUniqueId()).getFormattedName());

            // Contracts summary
            List<ActiveContract> contracts = core.getContractManager().getContracts(enterprise);
            long completed = contracts.stream().filter(ActiveContract::isCompleted).count();
            long active = contracts.stream().filter(c -> !c.isCompleted()).count();

            String contractInfo = completed > 0
                    ? "§a" + completed + " ready §8| §e" + active + " active"
                    : "§e" + active + " active";
            player.sendMessage("§8§l» §7Contracts: " + contractInfo);

            // Job sites compact line
            player.sendMessage("§8§l» §7Sites: " + getJobSitesCompact(enterprise));
        }

        // Daily reward notification
        if (DailyManager.INSTANCE.canClaimDaily(player)) {
            player.sendMessage("§8§l» §6⬢ §eDaily reward ready! §7(/daily)");
        }

        player.sendMessage("");
    }

    private String getJobSitesCompact(Enterprise enterprise) {
        StringBuilder sb = new StringBuilder();

        FarmlandSite farmland = enterprise.getJobSiteManager().getFarmlandSite();
        QuarrySite quarry = enterprise.getJobSiteManager().getQuarrySite();
        GraveyardSite graveyard = enterprise.getJobSiteManager().getGraveyardSite();

        if (farmland != null && farmland.isBuilt()) {
            sb.append("§6⌂").append(farmland.getLevel()).append(" ");
        } else {
            sb.append("§8⌂ ");
        }

        if (quarry != null && quarry.isBuilt()) {
            sb.append("§b⛏").append(quarry.getLevel()).append(" ");
        } else {
            sb.append("§8⛏ ");
        }

        if (graveyard != null && graveyard.isBuilt()) {
            sb.append("§5⚰").append(graveyard.getLevel());
        } else {
            sb.append("§8⚰");
        }

        return sb.toString();
    }
}
