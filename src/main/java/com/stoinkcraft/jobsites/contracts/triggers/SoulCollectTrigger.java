package com.stoinkcraft.jobsites.contracts.triggers;

import com.stoinkcraft.jobsites.contracts.ContractContext;

public class SoulCollectTrigger implements ContractTrigger {

    @Override
    public boolean matches(ContractContext context) {
        // We'll use a String marker "SOUL" as eventData
        String marker = context.getEventData(String.class);
        return "SOUL".equals(marker);
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}
