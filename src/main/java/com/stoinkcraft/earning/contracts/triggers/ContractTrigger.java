package com.stoinkcraft.earning.contracts.triggers;

import com.stoinkcraft.earning.contracts.ContractContext;

public interface ContractTrigger {

    boolean matches(ContractContext context);

    int getProgressIncrement(ContractContext context);
}