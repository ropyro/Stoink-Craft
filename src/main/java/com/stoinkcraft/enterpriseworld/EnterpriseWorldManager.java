package com.stoinkcraft.enterpriseworld;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class EnterpriseWorldManager {

    private final World world;

    public EnterpriseWorldManager() {
        if (Bukkit.getWorld("enterprise_world") == null) {
            WorldCreator creator = new WorldCreator("enterprise_world");
            creator.generator(new VoidWorldGenerator());
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);
            this.world = creator.createWorld();
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
