package com.stoinkcraft.earning.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.contracts.ActiveContract;

public interface Reward {
    void apply(Enterprise enterprise, ActiveContract contract);
}