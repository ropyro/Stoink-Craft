package com.stoinkcraft.jobsites.worlds;

import org.bukkit.*;

public class EnterpriseWorldManager {

    private final World world;

    public EnterpriseWorldManager() {
        if (Bukkit.getWorld("enterprise_world") == null) {
            WorldCreator creator = new WorldCreator("enterprise_world");
            creator.generator(new VoidWorldGenerator());
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);
            this.world = creator.createWorld();
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        } else {
            this.world = Bukkit.getWorld("enterprise_world");
        }

        assert world != null;
        world.setSpawnLocation(0, 64, 0);
    }

    public World getWorld() {
        return world;
    }
}
