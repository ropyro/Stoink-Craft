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
    private CuboidRegion cuboidRegion;
    private CuboidRegion dirtLayerRegion;

    // Upgrade system
    private static final int MAX_GROWTH_SPEED_LEVEL = 10;
    private static final double BASE_GROWTH_CHANCE = 0.02;

    private String regionName;

    public CropGenerator(Location corner1, Location corner2, JobSite parent, String regionName) {
        super(parent);
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.bukkitWorld = corner1.getWorld();
        this.cuboidRegion = getRegion(corner1, corner2);
        this.dirtLayerRegion = getRegion(corner1.add(0, -1, 0), corner2.add(0, -1, 0));
        this.regionName = regionName;
    }

    @Override
    protected void onTick() {
        // Increase growth speed based on upgrade level
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
                getCuboidRegion(),
                regionName,
                flags,
                10);
    }

    /**
     * Artificially increases crop growth based on upgrade level
     * Higher levels = more growth stages added per tick
     */
    private void increaseGrowthSpeed() {
        // Calculate growth chance based on level
        // Level 1 = 5% chance, Level 10 = 50% chance per tick
        double growthChance = BASE_GROWTH_CHANCE * ((FarmlandSite)getParent()).getData().getCropGrowthSpeedLevel();

        // Run async to avoid lag
        Bukkit.getScheduler().runTaskAsynchronously(StoinkCore.getInstance(), () -> {
            com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

            try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
                // Iterate through all blocks in the crop region
                for (BlockVector3 pos : cuboidRegion) {
                    BlockState blockState = session.getBlock(pos);

                    // Check if it's a crop block
                    if (isCropBlock(blockState.getBlockType())) {
                        // Get current age
                        Integer currentAge = blockState.getState(PropertyKey.AGE);
                        if (currentAge != null && currentAge < 7) {
                            // Random chance to grow based on upgrade level
                            if (Math.random() < growthChance) {
                                BlockState newState = blockState.with(PropertyKey.AGE, currentAge + 1);
                                session.setBlock(pos, newState);
                                //Bukkit.getLogger().info("artificial growth" + pos.toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Calculate growth stages based on upgrade level
     * @return number of growth stages to add per tick
     */
    private int calculateGrowthStages() {
        int growthSpeedLevel = ((FarmlandSite)getParent()).getData().getCropGrowthSpeedLevel();
        // Exponential scaling: Level 1-3 = 1 stage, 4-6 = 2 stages, 7-9 = 3 stages, 10 = 4 stages
        if (growthSpeedLevel <= 3) return 1;
        if (growthSpeedLevel <= 6) return 2;
        if (growthSpeedLevel <= 9) return 3;
        return 4;

        // Alternative linear scaling: return growthSpeedLevel;
        // Alternative percentage chance: return Random.nextDouble() < (growthSpeedLevel * 0.1) ? 1 : 0;
    }

    /**
     * Check if a block type is a crop
     */
    private boolean isCropBlock(BlockType blockType) {
        return blockType == BlockTypes.WHEAT ||
                blockType == BlockTypes.CARROTS ||
                blockType == BlockTypes.POTATOES ||
                blockType == BlockTypes.BEETROOTS;
    }

    // Upgrade methods
    public boolean upgradeGrowthSpeed() {
        if (getGrowthSpeedLevel() < MAX_GROWTH_SPEED_LEVEL) {
            setGrowthSpeedLevel(getGrowthSpeedLevel() + 1);
            return true;
        }
        return false;
    }

    public int getGrowthSpeedLevel() {
        return ((FarmlandSite)getParent()).getData().getCropGrowthSpeedLevel();
    }

    public void setGrowthSpeedLevel(int level) {
        ((FarmlandSite)getParent()).getData().setCropGrowthSpeedLevel(Math.min(MAX_GROWTH_SPEED_LEVEL, Math.max(1, level)));
    }

    public int getMaxGrowthSpeedLevel() {
        return MAX_GROWTH_SPEED_LEVEL;
    }

    // ... rest of your existing methods ...


    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    public void setCropGeneratorType(CropGeneratorType cropGeneratorType) {
        ((FarmlandSite)getParent()).getData().setCurrentType(cropGeneratorType);
        regenerateCrops();
    }

    public String getRegionName() {
        return regionName;
    }

    public void regenerateCrops() {
        com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            BlockState farmland = BlockTypes.FARMLAND.getDefaultState()
                    .with(BlockTypes.FARMLAND.getProperty(PropertyKey.MOISTURE), 7);
            session.setBlocks((Region) dirtLayerRegion, farmland);

            switch(((FarmlandSite)getParent()).getData().getCurrentType()){
                case CARROT -> session.setBlocks((Region) cuboidRegion, BlockTypes.CARROTS.getDefaultState());
                case WHEAT -> session.setBlocks((Region) cuboidRegion, BlockTypes.WHEAT.getDefaultState());
                case POTATO -> session.setBlocks((Region) cuboidRegion, BlockTypes.POTATOES.getDefaultState());
                case BEETROOT -> session.setBlocks((Region) cuboidRegion, BlockTypes.BEETROOTS.getDefaultState());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceMissingCrops(){
        com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            switch(((FarmlandSite)getParent()).getData().getCurrentType()){
                case CARROT -> session.replaceBlocks(
                        cuboidRegion,
                        Set.of(BlockTypes.AIR.getDefaultState().toBaseBlock()),
                        BlockTypes.CARROTS.getDefaultState()
                );
                case WHEAT -> session.replaceBlocks(
                        cuboidRegion,
                        Set.of(BlockTypes.AIR.getDefaultState().toBaseBlock()),
                        BlockTypes.WHEAT.getDefaultState());
                case POTATO -> session.replaceBlocks(
                        cuboidRegion,
                        Set.of(BlockTypes.AIR.getDefaultState().toBaseBlock()),
                        BlockTypes.POTATOES.getDefaultState());
                case BEETROOT -> session.replaceBlocks(
                        cuboidRegion,
                        Set.of(BlockTypes.AIR.getDefaultState().toBaseBlock()),
                        BlockTypes.BEETROOTS.getDefaultState());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum CropGeneratorType {
        CARROT, WHEAT, POTATO, BEETROOT, NONE;
    }
}