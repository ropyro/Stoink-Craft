package com.stoinkcraft.jobsites.sites;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.collections.CollectionManager;
import com.stoinkcraft.jobsites.components.JobSiteComponent;
import com.stoinkcraft.jobsites.components.JobSiteNPC;
import com.stoinkcraft.jobsites.components.JobSiteStructure;
import com.stoinkcraft.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.utils.RegionUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class JobSite {
    protected final Enterprise enterprise;
    protected final JobSiteType type;
    protected final JobSiteData data;
    protected final Region region;
    protected final Location spawnPoint;
    protected File schematic;
    protected boolean isBuilt;
    protected final List<JobSiteUpgrade> upgrades = new ArrayList<>();
    protected final List<JobSiteComponent> components = new ArrayList<>();

    public JobSite(Enterprise enterprise, JobSiteType type, Location spawnPoint, File schematic, JobSiteData data, boolean isBuilt) {
        this.enterprise = enterprise;
        this.type = type;
        this.region = SchematicUtils.getRegionFromSchematic(schematic, spawnPoint);
        this.spawnPoint = spawnPoint;
        this.schematic = schematic;
        this.data = data;
        this.isBuilt = isBuilt;
        data.setParent(this);
    }

    public JobSiteData getData() {
        return data;
    }

    public List<JobSiteUpgrade> getUpgrades() {
        return upgrades;
    }

    public @Nullable JobSiteStructure getStructure(String structureId) {
        return components.stream()
                .filter(component -> component instanceof JobSiteStructure)
                .map(component -> (JobSiteStructure) component)
                .filter(s -> s.getUnlockableId().equalsIgnoreCase(structureId))
                .findFirst()
                .orElse(null);
    }

    public List<Unlockable> getUnlockables() {
        return components.stream()
                .filter(component -> component instanceof Unlockable)
                .map(component -> (Unlockable) component)
                .toList();
    }

    public @Nullable Unlockable getUnlockable(String id) {
        return getUnlockables().stream()
                .filter(u -> u.getUnlockableId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public void disband() {
        if(containsActivePlayer()){
            Bukkit.getOnlinePlayers().stream().filter(p -> contains(p.getLocation())).forEach(p -> p.chat("/spawn"));
        }
        components.forEach(JobSiteComponent::disband);
        removeBuild();
        StoinkCore.getInstance().getProtectionManager().unindexJobSite(this);
    }

    private void removeBuild() {
        com.sk89q.worldedit.world.World weWorld =
                FaweAPI.getWorld(getSpawnPoint().getWorld().getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            session.setBlocks(region, BlockTypes.AIR.getDefaultState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean isBuilt) {
        this.isBuilt = isBuilt;
    }

    public void rebuild() {
        disband();
        setBuilt(false);
        build();
    }

    public void teleportPlayer(Player player, boolean ignoreBuilt) {
        if (!isBuilt && !ignoreBuilt) build();
        Location spawn = spawnPoint.clone().add(0.5, 0, 0.5);
        spawn.setYaw(90);
        player.teleport(spawn);
    }

    public void teleportPlayer(Player player) {
        teleportPlayer(player, false);
    }

    public void build() {
        if (isBuilt) {
            StoinkCore.getInstance().getLogger().info(type + " site for " + enterprise.getName() + " already built.");
            return;
        }

        SchematicUtils.pasteSchematic(schematic, spawnPoint, true);

        components.forEach(JobSiteComponent::build);

        isBuilt = true;
        getData().setBuilt(true);
        StoinkCore.getInstance().getLogger().info("Built " + type + " job site for " + enterprise.getName() + " at " + spawnPoint);

        StoinkCore.getInstance().getProtectionManager().indexJobSite(this);
    }

    public void tick() {
        components.forEach(JobSiteComponent::tick);
    }

    public boolean contains(Location loc) {
        if (region == null) return false;
        return region.contains(RegionUtils.toBlockVector3(loc));
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public boolean purchaseUpgrade(JobSiteUpgrade upgrade, Player player) {
        if (!enterprise.hasManagementPermission(player.getUniqueId())) return false;

        JobSiteData d = getData();
        int current = d.getLevel(upgrade.id());

        if (current >= upgrade.maxLevel()) return false;
        if (!upgrade.canUnlock(this)) return false;

        int currentLevel = JobsiteLevelHelper.getLevelFromXp(d.getXp());
        if (currentLevel < upgrade.requiredJobsiteLevel()) return false;

        int cost = upgrade.cost(current + 1);
        if (enterprise.getBankBalance() < cost) return false;

        enterprise.decreaseBankBalance(cost);
        d.setLevel(upgrade.id(), current + 1);
        upgrade.apply(this, current + 1);

        return true;
    }

    public boolean purchaseUnlockable(Unlockable unlockable, Player player) {
        if (!enterprise.hasManagementPermission(player.getUniqueId())) return false;
        if (!unlockable.canUnlock()) return false;

        int cost = unlockable.getCost();
        if (enterprise.getBankBalance() < cost) return false;

        enterprise.decreaseBankBalance(cost);
        getData().startUnlock(unlockable);

        return true;
    }

    @Deprecated
    public boolean purchaseStructure(JobSiteStructure structure, Player player) {
        return purchaseUnlockable(structure, player);
    }

    public int getLevel() {
        return JobsiteLevelHelper.getLevelFromXp(getData().getXp());
    }

    public void levelUp() {
        int newLevel = getLevel();
        components.forEach(JobSiteComponent::levelUp);
        enterprise.sendEnterpriseMessage(
                "",
                "§a§l" + type.name() + " leveled Up!",
                "",
                "§7Level " + (newLevel-1)+ " ▶ §a" + newLevel,
                ""
        );
        CollectionManager.playLevelUpSound(enterprise, this);
    }

    public void addComponent(JobSiteComponent component) {
        this.components.add(component);
    }

    public List<JobSiteComponent> getComponents() {
        return components;
    }

    public void initializeNpcsFromRegistry() {
        components.stream()
                .filter(c -> c instanceof JobSiteNPC)
                .map(c -> (JobSiteNPC) c)
                .forEach(JobSiteNPC::initializeFromRegistry);
    }

    public Enterprise getEnterprise() {
        return this.enterprise;
    }

    public JobSiteType getType() {
        return type;
    }

    public Region getRegion() {
        return region;
    }

    public boolean containsActivePlayer() {
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(p -> contains(p.getLocation()));
    }
}