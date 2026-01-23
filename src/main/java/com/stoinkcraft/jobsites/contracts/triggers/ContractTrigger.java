package com.stoinkcraft.jobsites.contracts.triggers;

import com.stoinkcraft.jobsites.contracts.ContractContext;

public interface ContractTrigger {

    boolean matches(ContractContext context);

    int getProgressIncrement(ContractContext context);
}