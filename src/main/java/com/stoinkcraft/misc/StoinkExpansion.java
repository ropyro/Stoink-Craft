package com.stoinkcraft.misc;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.JobsiteLevelHelper;
import com.stoinkcraft.earning.jobsites.components.generators.HoneyGenerator;
import com.stoinkcraft.earning.jobsites.components.structures.MausoleumStructure;
import com.stoinkcraft.earning.jobsites.components.structures.PowerCellStructure;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.earning.jobsites.sites.quarry.OreSet;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.PlayerUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StoinkExpansion extends PlaceholderExpansion {

    private StoinkCore plugin;

    public StoinkExpansion(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "stoinkcraft";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ropyro";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());

        switch (identifier.toLowerCase()) {

            // ==================== Existing Placeholders ====================

            case "enterprise":
                if (e == null) return "§7Unemployed";
                return e.getName();

            case "role":
                if (e == null) return "§7Unemployed";
                return e.getMemberRole(player.getUniqueId()).getFormattedName();

            case "ent_balance":
                if (e == null) return " ";
                return "$" + ChatUtils.formatMoneyNoCents(e.getBankBalance());

            case "networth":
                if (e == null) return " ";
                return "$" + ChatUtils.formatMoneyNoCents(e.getNetWorth());

            case "player_networth":
                return "$" + ChatUtils.formatMoneyNoCents(PlayerUtils.getPlayerNetworth(player));

            case "scoreboard":
                return getCurrentJobSiteUnformattedName(player, e);

            // ==================== Farmland Placeholders ====================

            case "farmland_level":
                if (e == null) return "0";
                FarmlandSite farmland = e.getJobSiteManager().getFarmlandSite();
                return farmland != null ? String.valueOf(farmland.getLevel()) : "0";

            case "farmland_xp":
                if (e == null) return "0";
                farmland = e.getJobSiteManager().getFarmlandSite();
                return farmland != null ? formatNumber(farmland.getData().getXp()) : "0";

            case "farmland_xp_next":
                if (e == null) return "0";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "0";
                return formatNumber(JobsiteLevelHelper.getXpForNextLevel(farmland.getLevel()));

            case "farmland_xp_bar":
                if (e == null) return createProgressBar(0, "6", "7");
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return createProgressBar(0, "6", "7");
                return createProgressBar(getXpProgress(farmland), "6", "7");

            case "farmland_xp_percent":
                if (e == null) return "0%";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "0%";
                return (int) Math.round(getXpProgress(farmland) * 100) + "%";

            case "farmland_xp_progress":
                if (e == null) return "0/0";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "0/0";
                return getXpProgressString(farmland);

            case "farmland_crop":
                if (e == null) return "None";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "None";
                return formatName(farmland.getData().getCurrentCropType().name());

            case "farmland_animal":
                if (e == null) return "None";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "None";
                return formatName(farmland.getData().getCurrentMobType().name());

            case "farmland_honey_status":
                if (e == null) return "Locked";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "Locked";
                if (!farmland.areBeeHivesBuilt()) return "§cLocked";
                long readyHives = farmland.getHoneyGenerators().stream()
                        .filter(HoneyGenerator::canHarvest).count();
                int totalHives = farmland.getHoneyGenerators().size();
                return readyHives + "/" + totalHives + " Ready";

            case "farmland_animal_count":
                if (e == null) return "0";
                farmland = e.getJobSiteManager().getFarmlandSite();
                if (farmland == null) return "0";
                return String.valueOf(farmland.getMobGenerator().getCurrentMobCount());

            // ==================== Quarry Placeholders ====================

            case "quarry_level":
                if (e == null) return "0";
                QuarrySite quarry = e.getJobSiteManager().getQuarrySite();
                return quarry != null ? String.valueOf(quarry.getLevel()) : "0";

            case "quarry_xp":
                if (e == null) return "0";
                quarry = e.getJobSiteManager().getQuarrySite();
                return quarry != null ? formatNumber(quarry.getData().getXp()) : "0";

            case "quarry_xp_next":
                if (e == null) return "0";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "0";
                return formatNumber(JobsiteLevelHelper.getXpForNextLevel(quarry.getLevel()));

            case "quarry_xp_bar":
                if (e == null) return createProgressBar(0, "b", "7");
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return createProgressBar(0, "b", "7");
                return createProgressBar(getXpProgress(quarry), "b", "7");

            case "quarry_xp_percent":
                if (e == null) return "0%";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "0%";
                return (int) Math.round(getXpProgress(quarry) * 100) + "%";

            case "quarry_xp_progress":
                if (e == null) return "0/0";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "0/0";
                return getXpProgressString(quarry);

            case "quarry_ore_set":
                if (e == null) return "None";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "None";
                // Assuming you have a method to get current ore set
                return getQuarryOreSetName(quarry);

            case "quarry_power_status":
                if (e == null) return "Locked";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "Locked";
                return getQuarryPowerStatus(quarry);

            case "quarry_haste_level":
                if (e == null) return "None";
                quarry = e.getJobSiteManager().getQuarrySite();
                if (quarry == null) return "None";
                return getQuarryHasteLevel(quarry);

            // ==================== Graveyard Placeholders ====================

            case "graveyard_level":
                if (e == null) return "0";
                GraveyardSite graveyard = e.getJobSiteManager().getGraveyardSite();
                return graveyard != null ? String.valueOf(graveyard.getLevel()) : "0";

            case "graveyard_xp":
                if (e == null) return "0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                return graveyard != null ? formatNumber(graveyard.getData().getXp()) : "0";

            case "graveyard_xp_next":
                if (e == null) return "0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "0";
                return formatNumber(JobsiteLevelHelper.getXpForNextLevel(graveyard.getLevel()));

            case "graveyard_xp_bar":
                if (e == null) return createProgressBar(0, "5", "7");
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return createProgressBar(0, "5", "7");
                return createProgressBar(getXpProgress(graveyard), "5", "7");

            case "graveyard_xp_percent":
                if (e == null) return "0%";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "0%";
                return (int) Math.round(getXpProgress(graveyard) * 100) + "%";

            case "graveyard_xp_progress":
                if (e == null) return "0/0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "0/0";
                return getXpProgressString(graveyard);

            case "graveyard_souls":
                if (e == null) return "0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                return graveyard != null ? formatNumber(graveyard.getData().getSouls()) : "0";

            case "graveyard_tombstones":
                if (e == null) return "0/0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "0/0";
                int active = graveyard.getData().getTombstonesPurchased();
                int max = graveyard.getMaxPurchasableTombstones();
                int total = GraveyardSite.TOTAL_TOMBSTONES;
                return active + "/" + total + " §7(§d" + max + " §7avail)";

            case "graveyard_mausoleum_status":
                if (e == null) return "Locked";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "Locked";
                return getMausoleumStatus(graveyard);

            case "graveyard_active_tombstones":
                if (e == null) return "0";
                graveyard = e.getJobSiteManager().getGraveyardSite();
                if (graveyard == null) return "0";
                return String.valueOf(graveyard.getData().getTombstonesPurchased());

            // ==================== Dynamic JobSite Placeholders ====================

            case "current_jobsite_header":
                return getCurrentJobSiteHeader(player, e);

            case "current_jobsite_level":
                return getCurrentJobSiteLevel(player, e);

            case "current_jobsite_xp":
                return getCurrentJobSiteXp(player, e);

            case "current_jobsite_extra":
                return getCurrentJobSiteExtra(player, e);

            case "current_jobsite":
                return getCurrentJobSiteName(player, e);

            default:
                return null;
        }
    }

// ==================== New Helper Methods ====================

    private String getXpProgressString(JobSite site) {
        int currentXp = site.getData().getXp();
        int currentLevel = site.getLevel();
        int xpForCurrentLevel = JobsiteLevelHelper.getXpForLevel(currentLevel);
        int xpForNextLevel = JobsiteLevelHelper.getXpForLevel(currentLevel + 1);

        int xpIntoLevel = currentXp - xpForCurrentLevel;
        int xpNeededForLevel = xpForNextLevel - xpForCurrentLevel;

        return formatNumber(xpIntoLevel) + "/" + formatNumber(xpNeededForLevel);
    }

    private String getQuarryOreSetName(QuarrySite quarry) {
        // Adjust based on your QuarrySite implementation
        // This assumes you have a method to get current ore set
        try {
            OreSet oreSet = quarry.getData().getCurrentOreSet();
            if (oreSet == null) return "Basic";
            return oreSet.getDisplayName();
        } catch (Exception ex) {
            return "Basic";
        }
    }

    private String getQuarryPowerStatus(QuarrySite quarry) {
        // Adjust based on your PowerCellStructure implementation
        try {
            PowerCellStructure powerCell = quarry.getPowerCell();
            if (powerCell == null) return "§cLocked";

            UnlockableState state = powerCell.getUnlockState();
            return switch (state) {
                case LOCKED -> "§cLocked";
                case BUILDING -> "§eBuilding...";
                case UNLOCKED -> "§aActive";
            };
        } catch (Exception ex) {
            return "§cLocked";
        }
    }

    private String getQuarryHasteLevel(QuarrySite quarry) {
        // Adjust based on your implementation
        try {
            PowerCellStructure powerCell = quarry.getPowerCell();
            if (powerCell == null || !powerCell.isUnlocked()) return "§cNone";

            int powerLevel = quarry.getData().getLevel("power_level");
            if (powerLevel <= 0) return "§7Lv. 0";
            return "§aLv. " + powerLevel;
        } catch (Exception ex) {
            return "§cNone";
        }
    }

    private String getMausoleumStatus(GraveyardSite graveyard) {
        try {
            MausoleumStructure mausoleum = graveyard.getMausoleumStructure();
            if (mausoleum == null) return "Mausoleum: §cLocked";

            UnlockableState state = mausoleum.getUnlockState();

            return switch (state) {
                case LOCKED -> {
                    if(graveyard.getLevel() >= MausoleumStructure.REQUIRED_LEVEL){
                        yield "Mausoleum: §aReady to build";
                    }else{
                        yield "Mausoleum: §cLocked §7(Lv. " + MausoleumStructure.REQUIRED_LEVEL + ")";
                    }
                }
                case BUILDING -> {
                    long timeRemaining = graveyard.getData()
                            .getUnlockableProgress(mausoleum.getUnlockableId()).getRemainingMillis();
                    yield "Mausoleum: §eBuilding Time Remaining: §a" + ChatUtils.formatDuration(timeRemaining);
                }
                case UNLOCKED -> {
                    if (mausoleum.isHordeActive()) {
                        yield "§5§l🕷 Horde Active! §7(§c" + mausoleum.getCachedSpiderCount() + "§7)";
                    } else {
                        yield "Next Horde: §e" + ChatUtils.formatDurationSeconds(mausoleum.getRemainingSeconds());
                    }
                }
            };
        } catch (Exception ex) {
            return "Mausoleum: §cLocked";
        }
    }

// ==================== Existing Helper Methods ====================

    private JobSiteType getCurrentJobSiteType(Player player, Enterprise e) {
        if (e == null) return null;
        return e.getJobSiteManager().resolveJobsite(player.getLocation());
    }

    private String getCurrentJobSiteName(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);
        if (type == null) return "§7None";

        return switch (type) {
            case FARMLAND -> "§6Farmland";
            case QUARRY -> "§bQuarry";
            case GRAVEYARD -> "§5Graveyard";
            default -> "§7Unknown";
        };
    }

    private String getCurrentJobSiteUnformattedName(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);
        if (type == null) return "default";

        return switch (type) {
            case FARMLAND -> "farmland";
            case QUARRY -> "quarry";
            case GRAVEYARD -> "graveyard";
            default -> "default";
        };
    }

    private String getCurrentJobSiteHeader(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);

        if (type == null) {
            return "§7§lNo JobSite";
        }

        return switch (type) {
            case FARMLAND -> {
                FarmlandSite site = e.getJobSiteManager().getFarmlandSite();
                int level = site != null ? site.getLevel() : 0;
                yield "§6§lFarmland §8- §eLv. " + level;
            }
            case QUARRY -> {
                QuarrySite site = e.getJobSiteManager().getQuarrySite();
                int level = site != null ? site.getLevel() : 0;
                yield "§b§lQuarry §8- §3Lv. " + level;
            }
            case GRAVEYARD -> {
                GraveyardSite site = e.getJobSiteManager().getGraveyardSite();
                int level = site != null ? site.getLevel() : 0;
                yield "§5§lGraveyard §8- §dLv. " + level;
            }
            default -> "§7§lUnknown";
        };
    }

    private String getCurrentJobSiteLevel(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);

        if (type == null) {
            return " §7• §fTravel to a JobSite";
        }

        return switch (type) {
            case FARMLAND -> {
                FarmlandSite site = e.getJobSiteManager().getFarmlandSite();
                if (site == null) yield " §7• §fNot built";
                String bar = createProgressBar(getXpProgress(site), "6", "7");
                int percent = (int) Math.round(getXpProgress(site) * 100);
                yield " §6• §7" + bar + " §e" + percent + "%";
            }
            case QUARRY -> {
                QuarrySite site = e.getJobSiteManager().getQuarrySite();
                if (site == null) yield " §7• §fNot built";
                String bar = createProgressBar(getXpProgress(site), "b", "7");
                int percent = (int) Math.round(getXpProgress(site) * 100);
                yield " §b• §7" + bar + " §3" + percent + "%";
            }
            case GRAVEYARD -> {
                GraveyardSite site = e.getJobSiteManager().getGraveyardSite();
                if (site == null) yield " §7• §fNot built";
                String bar = createProgressBar(getXpProgress(site), "5", "7");
                int percent = (int) Math.round(getXpProgress(site) * 100);
                yield " §5• §7" + bar + " §d" + percent + "%";
            }
            default -> " §7• §fUnknown";
        };
    }

    private String getCurrentJobSiteXp(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);

        if (type == null) {
            return " §7• §fto see stats";
        }

        return switch (type) {
            case FARMLAND -> {
                FarmlandSite site = e.getJobSiteManager().getFarmlandSite();
                if (site == null) yield " ";
                yield " §6• §fXP: §e" + getXpProgressString(site);
            }
            case QUARRY -> {
                QuarrySite site = e.getJobSiteManager().getQuarrySite();
                if (site == null) yield " ";
                yield " §b• §fXP: §3" + getXpProgressString(site);
            }
            case GRAVEYARD -> {
                GraveyardSite site = e.getJobSiteManager().getGraveyardSite();
                if (site == null) yield " ";
                yield " §5• §fXP: §d" + getXpProgressString(site);
            }
            default -> " ";
        };
    }

    private String getCurrentJobSiteExtra(Player player, Enterprise e) {
        JobSiteType type = getCurrentJobSiteType(player, e);

        if (type == null) {
            return " ";
        }

        return switch (type) {
            case FARMLAND -> {
                FarmlandSite site = e.getJobSiteManager().getFarmlandSite();
                if (site == null) yield " ";
                String cropType = site.getData().getCurrentCropType().name();
                yield " §6• §fCrop: §e" + formatName(cropType);
            }
            case QUARRY -> {
                QuarrySite site = e.getJobSiteManager().getQuarrySite();
                if (site == null) yield " ";
                yield " §b• §f" + getQuarryOreSetName(site);
            }
            case GRAVEYARD -> {
                GraveyardSite site = e.getJobSiteManager().getGraveyardSite();
                if (site == null) yield " ";
                int souls = site.getData().getSouls();
                yield " §5• §fSouls: §d" + formatNumber(souls) + " ✦";
            }
            default -> " ";
        };
    }

    private String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) return "None";
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private double getXpProgress(JobSite site) {
        int currentXp = site.getData().getXp();
        int currentLevel = site.getLevel();
        int xpForCurrentLevel = JobsiteLevelHelper.getXpForLevel(currentLevel);
        int xpForNextLevel = JobsiteLevelHelper.getXpForLevel(currentLevel + 1);

        int xpIntoLevel = currentXp - xpForCurrentLevel;
        int xpNeededForLevel = xpForNextLevel - xpForCurrentLevel;

        if (xpNeededForLevel <= 0) return 1.0;
        return Math.min(1.0, (double) xpIntoLevel / xpNeededForLevel);
    }

    private String createProgressBar(double progress, String filledColor, String emptyColor) {
        int totalBars = 10;
        int filledBars = (int) Math.round(progress * totalBars);
        filledBars = Math.max(0, Math.min(totalBars, filledBars));

        StringBuilder bar = new StringBuilder();

        bar.append("§").append(filledColor);
        for (int i = 0; i < filledBars; i++) {
            bar.append("▌");
        }

        bar.append("§").append(emptyColor);
        for (int i = filledBars; i < totalBars; i++) {
            bar.append("▌");
        }

        return bar.toString();
    }
}