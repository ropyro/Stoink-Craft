package com.stoinkcraft.earning.jobsites.protection;

import org.jetbrains.annotations.NotNull;

public interface ProtectedZone {

    /**
     * Check if this zone allows the given action at the query location.
     *
     * @param query The protection query containing player, location, action, etc.
     * @return ALLOW to permit, DENY to block, ABSTAIN if this zone doesn't govern this location
     */
    @NotNull
    ProtectionResult checkProtection(@NotNull ProtectionQuery query);
}
