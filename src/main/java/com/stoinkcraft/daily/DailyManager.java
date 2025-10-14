package com.stoinkcraft.daily;

import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DailyManager {

    private Map<UUID, Long> dailyClaimTimes = new HashMap<>();

    public static DailyManager INSTANCE;

    public DailyManager(){
        INSTANCE = this;
    }

    public void claimDaily(Player player) {
        if(canClaimDaily(player)){
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "crates givekey Treasure " + player.getName() + " 1");
            dailyClaimTimes.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public boolean canClaimDaily(Player player) {
        Long lastClaim = dailyClaimTimes.get(player.getUniqueId());
        if (lastClaim == null) {
            return true; // never claimed before
        }

        long timeSinceLastClaim = System.currentTimeMillis() - lastClaim;
        return timeSinceLastClaim >= SCConstants.DAY_MILLIS;
    }

    public long getTimeUntilNextClaim(Player player) {
        Long lastClaim = dailyClaimTimes.get(player.getUniqueId());
        if (lastClaim == null) return 0;
        long timeSinceLastClaim = System.currentTimeMillis() - lastClaim;
        return Math.max(0, SCConstants.DAY_MILLIS - timeSinceLastClaim);
    }

}
