package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.DescribableReward;
import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.jobsites.JobSite;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ContractFeedbackManager {

    private static final long INACTIVITY_TIMEOUT_MS = 10_000; // 10 seconds
    private final Map<UUID, BossBar> activeBars = new HashMap<>();
    private final Map<UUID, Long> lastUpdate = new HashMap<>();


    /* =======================
       Boss Bar Progress
       ======================= */
    public void showBossBar(Player player, ActiveContract contract) {

        BossBar bar = activeBars.computeIfAbsent(
                player.getUniqueId(),
                uuid -> Bukkit.createBossBar(
                        "",
                        BarColor.GREEN,
                        BarStyle.SOLID
                )
        );

        ContractDefinition def = contract.getDefinition();

        double progress =
                (double) contract.getProgress() / contract.getTarget();

        bar.setTitle("§e" + def.displayName()
                + " §7(" + contract.getProgress()
                + "/" + contract.getTarget() + ")");

        bar.setProgress(Math.min(1.0, progress));
        bar.addPlayer(player);
        bar.setVisible(true);

        lastUpdate.put(player.getUniqueId(), System.currentTimeMillis());

        if (contract.isCompleted()) {
            bar.setColor(BarColor.BLUE);
            bar.setTitle("§a✔ " + def.displayName() + " Complete!");
        }
    }

    /**
     * Clears the boss bar for a player.
     */
    public void clear(Player player) {
        BossBar bar = activeBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    /**
     * Clears all active boss bars
     */
    public void clearAll(){
        activeBars.keySet().stream().forEach(uuid -> activeBars.remove(uuid).removeAll());
    }

    /**
     * Clears the bar if the contract is done or expired.
     */
    public void clearIfFinished(Player player, ActiveContract contract) {
        if (contract.isCompleted() || contract.isExpired()) {
            clear(player);
        }
    }

    public void startCleanupTask(JavaPlugin plugin) {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            long now = System.currentTimeMillis();

            Iterator<Map.Entry<UUID, Long>> it = lastUpdate.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry<UUID, Long> entry = it.next();

                if (now - entry.getValue() < INACTIVITY_TIMEOUT_MS) {
                    continue;
                }

                UUID playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);

                if (player != null) {
                    clear(player);
                }

                it.remove();
            }

        }, 40L, 40L); // every 2 seconds
    }

    /* =======================
       Completion Announcement
       ======================= */

    public void announceCompletion(Enterprise enterprise, ActiveContract contract) {

        ContractDefinition def = contract.getDefinition();
        JobSite site = enterprise.getJobSiteManager()
                .getJobSite(def.jobSiteType());

        int beforeLevel = site.getLevel();
        double beforeXp = site.getData().getXp();

        // (XP already applied at this point)
        int afterLevel = site.getLevel();
        double afterXp = site.getData().getXp();

        sendEnterpriseMessage(enterprise,
                "§6§lContract Complete!",
                "§e" + def.displayName(),
                "",
                "§7Contributions:"
        );

        contract.getContributions().forEach((uuid, amount) -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            double percent = contract.getContributionPercentages().get(uuid) * 100;

            sendEnterpriseMessage(enterprise,
                    " §f- " + player.getName() + ": " +
                            amount + " §7(" + (int) percent + "%)"
            );
        });

        sendEnterpriseMessage(enterprise,
                "",
                "§6Rewards:"
        );

        appendRewardLore(enterprise, def.reward());

        sendEnterpriseMessage(enterprise,
                "",
                "§aJobsite Progress:",
                " §fXP: " + format(beforeXp) + " → " + format(afterXp),
                " §fLevel: " + beforeLevel + " → " + afterLevel
        );
    }

    private void appendRewardLore(Enterprise enterprise, Reward reward) {

        if (reward instanceof CompositeReward composite) {
            composite.getRewards().forEach(r ->
                    appendRewardLore(enterprise, r));
            return;
        }

        if (reward instanceof DescribableReward describable) {
            describable.getLore().forEach(line ->
                    sendEnterpriseMessage(enterprise, " §f- " + line));
        }
    }

    private void sendEnterpriseMessage(Enterprise enterprise, String... lines) {
        List<Player> players = enterprise.getOnlineMembers();
        players.forEach(player -> {
            for (String line : lines) {
                player.sendMessage(line);
            }
        });
    }

    private String format(double value) {
        return String.format("%.1f", value);
    }
}
