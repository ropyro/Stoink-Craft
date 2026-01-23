package com.stoinkcraft.jobsites.sites.protection;

public enum ProtectionAction {
    /**
     * Breaking/destroying a block
     */
    BREAK,

    /**
     * Placing a block
     */
    PLACE,

    /**
     * General right-click interaction (doors, buttons, levers, etc.)
     */
    RIGHT_CLICK,

    /**
     * Right-click with shears on a block (beehives, pumpkins, etc.)
     */
    SHEAR,

    /**
     * Damaging or killing an entity
     */
    PVE,

    /**
     * Attacking or damaging another player
     */
    PVP,

    /**
     * Explosion damage to blocks (TNT, creepers, etc.)
     */
    EXPLOSION
}