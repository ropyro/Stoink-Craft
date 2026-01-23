package com.stoinkcraft.jobsites.sites;

public class JobsiteLevelHelper {

    private static final double BASE_XP = 500;
    private static final double EXPONENT = 1.6;

    public static int getXpForLevel(int level) {
        if (level <= 1) return 0;
        return (int) Math.round(BASE_XP * Math.pow(level - 1, EXPONENT));
    }

    public static int getLevelFromXp(int xp) {
        int level = 1;

        while (true) {
            int next = getXpForLevel(level + 1);
            if (xp < next) return level;
            level++;
            if (level >= 100) return 100;
        }
    }

    public static int getXpToNextLevel(int xp) {
        int level = getLevelFromXp(xp);
        int nextXp = getXpForLevel(level + 1);
        return nextXp - xp;
    }

    public static int getXpForNextLevel(int level){
        return getXpForLevel(level + 1);
    }
}
