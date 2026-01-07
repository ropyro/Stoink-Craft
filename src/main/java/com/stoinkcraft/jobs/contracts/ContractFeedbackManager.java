package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.rewards.*;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
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
        int beforeXp = site.getData().getXp();

        // (XP already applied at this point)
        int afterLevel = site.getLevel();
        int afterXp = site.getData().getXp();
        int xpGained = afterXp - beforeXp;

        // Get styling based on JobSite type
        String[] colors = getJobSiteColors(def.jobSiteType());
        String primary = colors[0];
        String secondary = colors[1];
        String icon = getJobSiteIcon(def.jobSiteType());

        // Build contributors string
        String contributors = buildContributorsString(contract);

        // Extract reward totals
        double totalMoney = extractTotalMoney(def.reward());
        double playerSharePercent = extractPlayerSharePercent(def.reward());
        double enterpriseMoney = totalMoney * (1 - playerSharePercent);
        double playerPoolMoney = totalMoney * playerSharePercent;
        int totalXp = extractTotalXp(def.reward());

        // Build progress string
        String progress = buildProgressString(beforeLevel, afterLevel, xpGained, secondary);

        // Build enterprise rewards string
        String enterpriseRewards = "§a+$" + formatCompact(enterpriseMoney) + " §8| §e+" + totalXp + " XP";

        // Send personalized messages to each member
        enterprise.sendPersonalizedMessage(player -> {
            UUID uuid = player.getUniqueId();
            Double contribution = contract.getContributionPercentages().get(uuid);

            String yourRewards;
            if (contribution != null && contribution > 0) {
                double playerMoney = playerPoolMoney * contribution;
                int percent = (int) (contribution * 100);
                yourRewards = "§a+$" + formatCompact(playerMoney) + " §7(" + percent + "% contribution)";
            } else {
                yourRewards = "§7None §8(no contribution)";
            }

            return new String[]{
                    "",
                    "§8§l» " + primary + "§l" + icon + " Contract Complete! §8| " + secondary + def.displayName(),
                    "§8§l» §7Contributors: " + contributors,
                    "§8§l» " + secondary + "Enterprise: §f" + enterpriseRewards,
                    "§8§l» " + secondary + "You Earned: §f" + yourRewards,
                    "§8§l» §7Progress: " + progress,
                    ""
            };
        });
    }

// ==================== Helper Methods ====================

    private String[] getJobSiteColors(JobSiteType type) {
        return switch (type) {
            case FARMLAND -> new String[]{"§6", "§e"};
            case QUARRY -> new String[]{"§b", "§3"};
            case GRAVEYARD -> new String[]{"§5", "§d"};
            default -> new String[]{"§6", "§e"};
        };
    }

    private String getJobSiteIcon(JobSiteType type) {
        return switch (type) {
            case FARMLAND -> "🌾";
            case QUARRY -> "⛏";
            case GRAVEYARD -> "💀";
            default -> "✔";
        };
    }

    private String buildContributorsString(ActiveContract contract) {
        StringBuilder sb = new StringBuilder();
        Map<UUID, Integer> contributions = contract.getContributions();
        Map<UUID, Double> percentages = contract.getContributionPercentages();

        int count = 0;
        int maxDisplay = 3;

        List<Map.Entry<UUID, Integer>> sorted = contributions.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();

        for (Map.Entry<UUID, Integer> entry : sorted) {
            if (count >= maxDisplay) {
                int remaining = sorted.size() - maxDisplay;
                if (remaining > 0) {
                    sb.append(" §7+").append(remaining).append(" more");
                }
                break;
            }

            if (count > 0) sb.append(" §8| ");

            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            int percent = (int) (percentages.get(entry.getKey()) * 100);

            sb.append("§f").append(player.getName())
                    .append(" §7(").append(percent).append("%)");

            count++;
        }

        return sb.toString();
    }

    private String buildProgressString(int beforeLevel, int afterLevel, int xpGained, String color) {
        StringBuilder sb = new StringBuilder();

        if (afterLevel > beforeLevel) {
            sb.append("§fLv.").append(beforeLevel)
                    .append(" §7→ §a§lLv.").append(afterLevel).append(" ⬆");
        } else {
            sb.append("§fLv.").append(afterLevel);
        }

        sb.append(" §8| ").append(color).append("+").append(formatCompact(xpGained)).append(" XP");

        return sb.toString();
    }

// ==================== Reward Extraction Methods ====================

    private double extractTotalMoney(Reward reward) {
        if (reward instanceof MoneyReward money) {
            return money.getTotalAmount();
        } else if (reward instanceof CompositeReward composite) {
            return composite.getRewards().stream()
                    .mapToDouble(this::extractTotalMoney)
                    .sum();
        }
        return 0;
    }

    private double extractPlayerSharePercent(Reward reward) {
        if (reward instanceof MoneyReward money) {
            return money.getPlayerShare();
        } else if (reward instanceof CompositeReward composite) {
            return composite.getRewards().stream()
                    .filter(r -> r instanceof MoneyReward)
                    .map(r -> ((MoneyReward) r).getPlayerShare())
                    .findFirst()
                    .orElse(0.0);
        }
        return 0;
    }

    private int extractTotalXp(Reward reward) {
        if (reward instanceof JobSiteXpReward xp) {
            return xp.getXp();
        } else if (reward instanceof CompositeReward composite) {
            return composite.getRewards().stream()
                    .mapToInt(this::extractTotalXp)
                    .sum();
        }
        return 0;
    }

    private String formatCompact(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf((int) number);
    }

    private String formatCompact(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}
