package com.stoinkcraft.jobs.contracts.triggers;

import com.stoinkcraft.jobs.contracts.ContractContext;

public interface ContractTrigger {

    boolean matches(ContractContext context);

    int getProgressIncrement(ContractContext context);
}