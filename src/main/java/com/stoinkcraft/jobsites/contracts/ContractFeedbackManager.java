package com.stoinkcraft.jobsites.contracts;

import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.contracts.rewards.*;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
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
    private static final int MAX_BARS_PER_PLAYER = 4; // Limit to prevent UI clutter

    // Map<PlayerId, Map<ContractId, BossBar>>
    private final Map<UUID, Map<String, BossBar>> activeBars = new HashMap<>();
    // Map<PlayerId, Map<ContractId, LastUpdateTime>>
    private final Map<UUID, Map<String, Long>> lastUpdate = new HashMap<>();


    /* =======================
       Boss Bar Progress
       ======================= */
    public void showBossBar(Player player, ActiveContract contract) {
        UUID playerId = player.getUniqueId();
        String contractId = contract.getDefinition().id();

        Map<String, BossBar> playerBars = activeBars.computeIfAbsent(
                playerId, k -> new LinkedHashMap<>()); // LinkedHashMap preserves insertion order

        Map<String, Long> playerUpdates = lastUpdate.computeIfAbsent(
                playerId, k -> new HashMap<>());

        // Get or create bar for this specific contract
        BossBar bar = playerBars.computeIfAbsent(contractId, id -> {
            // Enforce max bars limit - remove oldest if at capacity
            if (playerBars.size() >= MAX_BARS_PER_PLAYER) {
                removeOldestBar(playerBars, playerUpdates);
            }
            return Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        });

        ContractDefinition def = contract.getDefinition();
        double progress = (double) contract.getProgress() / contract.getTarget();

        bar.setTitle("§e" + def.displayName()
                + " §7(" + contract.getProgress()
                + "/" + contract.getTarget() + ")");

        bar.setProgress(Math.min(1.0, progress));
        bar.addPlayer(player);
        bar.setVisible(true);

        playerUpdates.put(contractId, System.currentTimeMillis());

        if (contract.isCompleted()) {
            bar.setColor(BarColor.BLUE);
            bar.setTitle("§a✔ " + def.displayName() + " Complete!");
        }
    }

    /**
     * Removes the oldest (least recently updated) bar when at capacity.
     */
    private void removeOldestBar(Map<String, BossBar> playerBars, Map<String, Long> playerUpdates) {
        String oldestId = playerUpdates.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(playerBars.keySet().iterator().next());

        BossBar oldBar = playerBars.remove(oldestId);
        if (oldBar != null) {
            oldBar.removeAll();
        }
        playerUpdates.remove(oldestId);
    }

    /**
     * Clears a specific contract's boss bar for a player.
     */
    public void clearContract(Player player, ActiveContract contract) {
        clearContract(player.getUniqueId(), contract.getDefinition().id());
    }

    /**
     * Clears a specific contract's boss bar by IDs.
     */
    private void clearContract(UUID playerId, String contractId) {
        Map<String, BossBar> playerBars = activeBars.get(playerId);
        if (playerBars != null) {
            BossBar bar = playerBars.remove(contractId);
            if (bar != null) {
                bar.removeAll();
            }

            // Clean up empty maps
            if (playerBars.isEmpty()) {
                activeBars.remove(playerId);
            }
        }

        Map<String, Long> playerUpdates = lastUpdate.get(playerId);
        if (playerUpdates != null) {
            playerUpdates.remove(contractId);

            if (playerUpdates.isEmpty()) {
                lastUpdate.remove(playerId);
            }
        }
    }

    /**
     * Clears all boss bars for a player.
     */
    public void clear(Player player) {
        UUID playerId = player.getUniqueId();

        Map<String, BossBar> playerBars = activeBars.remove(playerId);
        if (playerBars != null) {
            playerBars.values().forEach(BossBar::removeAll);
        }

        lastUpdate.remove(playerId);
    }

    /**
     * Clears all active boss bars for all players.
     */
    public void clearAll() {
        activeBars.values().forEach(playerBars ->
                playerBars.values().forEach(BossBar::removeAll)
        );
        activeBars.clear();
        lastUpdate.clear();
    }

    /**
     * Clears the bar if the contract is done or expired.
     */
    public void clearIfFinished(Player player, ActiveContract contract) {
        if (contract.isCompleted() || contract.isExpired()) {
            clearContract(player, contract);
        }
    }

    public void startCleanupTask(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            Iterator<Map.Entry<UUID, Map<String, Long>>> playerIt = lastUpdate.entrySet().iterator();

            while (playerIt.hasNext()) {
                Map.Entry<UUID, Map<String, Long>> playerEntry = playerIt.next();
                UUID playerId = playerEntry.getKey();
                Map<String, Long> contractUpdates = playerEntry.getValue();
                Map<String, BossBar> playerBars = activeBars.get(playerId);

                if (playerBars == null) {
                    playerIt.remove();
                    continue;
                }

                // Check each contract's bar for this player
                Iterator<Map.Entry<String, Long>> contractIt = contractUpdates.entrySet().iterator();

                while (contractIt.hasNext()) {
                    Map.Entry<String, Long> contractEntry = contractIt.next();

                    if (now - contractEntry.getValue() < INACTIVITY_TIMEOUT_MS) {
                        continue;
                    }

                    // Inactive - remove this contract's bar
                    String contractId = contractEntry.getKey();
                    BossBar bar = playerBars.remove(contractId);
                    if (bar != null) {
                        bar.removeAll();
                    }
                    contractIt.remove();
                }

                // Clean up player entry if no more active contracts
                if (contractUpdates.isEmpty()) {
                    playerIt.remove();
                    activeBars.remove(playerId);
                }
            }
        }, 40L, 40L);
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
        double totalMoney = extractTotalMoney(def.reward(), enterprise);
        double playerSharePercent = ConfigLoader.getEconomy().getPlayerPaySplit();
        double enterpriseMoney = totalMoney * (1 - playerSharePercent);
        double playerPoolMoney = totalMoney * playerSharePercent;
        int totalXp = extractTotalXp(def.reward());

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

            String boosterSuffix = enterprise.hasActiveBooster()
                    ? " §8(§a" + enterprise.getBoosterMultiplier() + "x boosted§8)"
                    : "";

            return new String[]{
                    "",
                    "§8§l» " + primary + "§l" + icon + " Contract Complete! §8| " + secondary + def.displayName(),
                    "§8§l» §7Contributors: " + contributors,
                    "§8§l» " + secondary + "Enterprise: §f" + enterpriseRewards + boosterSuffix,
                    "§8§l» " + secondary + "You Earned: §f" + yourRewards + boosterSuffix,
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

    private double extractTotalMoney(Reward reward, Enterprise enterprise) {
        if (reward instanceof MoneyReward money) {
            return money.getBoostedTotal(enterprise);
        } else if (reward instanceof CompositeReward composite) {
            return composite.getRewards().stream()
                    .filter(r -> r instanceof MoneyReward)
                    .mapToDouble(r -> ((MoneyReward) r).getBoostedTotal(enterprise))
                    .sum();
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
