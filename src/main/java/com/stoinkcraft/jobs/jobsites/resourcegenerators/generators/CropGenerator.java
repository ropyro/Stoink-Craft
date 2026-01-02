package com.stoinkcraft.jobs.jobsites.resourcegenerators.generators;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.ResourceGenerator;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.RegionUtils;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CropGenerator extends ResourceGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;

    private final CuboidRegion cuboidRegion;
    private final CuboidRegion dirtLayerRegion;

    private final String regionName;

    // Base growth chance per upgrade level:
    // Level 1 = 2%
    // Level 10 = 20%
    private static final double BASE_GROWTH_CHANCE = 0.02;

    public CropGenerator(Location corner1, Location corner2, JobSite parent, String regionName) {
        super(parent);

        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.bukkitWorld = corner1.getWorld();
        this.regionName = regionName;

        this.cuboidRegion = getRegion(this.corner1, this.corner2);

        // IMPORTANT: clone the coords before subtracting Y
        Location dirt1 = corner1.clone().add(0, -1, 0);
        Location dirt2 = corner2.clone().add(0, -1, 0);
        this.dirtLayerRegion = getRegion(dirt1, dirt2);
    }

    @Override
    protected void onTick() {
        if (TimeUtils.isDay(getParent().getSpawnPoint().getWorld())) {
            increaseGrowthSpeed();
        }
    }

    @Override
    public void init() {
        Map<StateFlag, StateFlag.State> flags = new HashMap<>();
        flags.put(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
        flags.put(Flags.INTERACT, StateFlag.State.ALLOW);
        flags.put(Flags.USE, StateFlag.State.ALLOW);
        flags.put(Flags.BLOCK_PLACE, StateFlag.State.DENY);

        RegionUtils.createProtectedRegion(
                getParent().getSpawnPoint().getWorld(),
                cuboidRegion,
                regionName,
                flags,
                10
        );
    }

    /**
     * Applies artificial growth based on upgrade level.
     */
    private void increaseGrowthSpeed() {
        int level = getGrowthSpeedLevel();

        if (level <= 0) return;

        double chance = BASE_GROWTH_CHANCE * level; // 2% per level

        Bukkit.getScheduler().runTaskAsynchronously(StoinkCore.getInstance(), () -> {
            com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

            try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
                for (BlockVector3 pos : cuboidRegion) {
                    BlockState state = session.getBlock(pos);

                    if (!isCropBlock(state.getBlockType())) continue;

                    Integer age = state.getState(PropertyKey.AGE);
                    if (age == null || age >= 7) continue;

                    if (Math.random() < chance) {
                        BlockState grown = state.with(PropertyKey.AGE, age + 1);
                        session.setBlock(pos, grown);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isCropBlock(BlockType type) {
        return type == BlockTypes.WHEAT ||
                type == BlockTypes.CARROTS ||
                type == BlockTypes.POTATOES ||
                type == BlockTypes.BEETROOTS;
    }

    // ---------------------------------------
    // UPGRADE INTEGRATION
    // ---------------------------------------

    private int getGrowthSpeedLevel() {
        // directly read from upgrade map
        return ((FarmlandSite) getParent()).getData().getLevel("crop_growth_speed");
    }

    // ---------------------------------------
    // CROP TYPE SELECTION
    // ---------------------------------------

    private FarmlandData data() {
        return ((FarmlandSite) getParent()).getData();
    }

    private CropGeneratorType getCropType() {
        return data().getCurrentCropType();
    }

    public void setCropType(CropGeneratorType type) {
        data().setCurrentCropType(type);
        regenerateCrops();
    }

    // ---------------------------------------
    // REGENERATE CROPS ON TYPE CHANGE
    // ---------------------------------------

    public void regenerateCrops() {
        com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {

            // replace dirt layer with hydrated farmland
            BlockState farmland = BlockTypes.FARMLAND.getDefaultState()
                    .with(BlockTypes.FARMLAND.getProperty(PropertyKey.MOISTURE), 7);
            session.setBlocks((Region) dirtLayerRegion, farmland);

            // set crop layer
            BlockState crop;

            switch (getCropType()) {
                case CARROT -> crop = BlockTypes.CARROTS.getDefaultState();
                case POTATO -> crop = BlockTypes.POTATOES.getDefaultState();
                case BEETROOT -> crop = BlockTypes.BEETROOTS.getDefaultState();
                default -> crop = BlockTypes.WHEAT.getDefaultState();
            }

            session.setBlocks((Region) cuboidRegion, crop);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceMissingCrops() {
        com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

        BlockState air = BlockTypes.AIR.getDefaultState();
        BlockState crop;

        switch (getCropType()) {
            case CARROT -> crop = BlockTypes.CARROTS.getDefaultState();
            case POTATO -> crop = BlockTypes.POTATOES.getDefaultState();
            case BEETROOT -> crop = BlockTypes.BEETROOTS.getDefaultState();
            default -> crop = BlockTypes.WHEAT.getDefaultState();
        }

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            session.replaceBlocks(
                    cuboidRegion,
                    Set.of(air.toBaseBlock()),
                    crop
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------
    // GETTERS
    // ---------------------------------------

    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    public String getRegionName() {
        return regionName;
    }

    // ---------------------------------------
    // ENUM
    // ---------------------------------------

    public enum CropGeneratorType {
        WHEAT, CARROT, POTATO, BEETROOT
    }
}