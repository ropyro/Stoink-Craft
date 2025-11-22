package com.stoinkcraft.utils;

import org.bukkit.World;

public class TimeUtils {

    public static boolean isDay(World world){
        long time = world.getTime();
        return time >= 0 && time < 12300;
    }
}
