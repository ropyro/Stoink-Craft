package com.stoinkcraft.jobsites.sites.protection;

public enum ProtectionResult {
    /**
     * Explicitly allow this action to proceed
     */
    ALLOW,

    /**
     * Explicitly deny this action
     */
    DENY,

    /**
     * This zone does not govern this location/action - defer to others
     */
    ABSTAIN
}