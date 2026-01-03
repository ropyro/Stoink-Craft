package com.stoinkcraft.jobs.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ActiveContract;

public interface Reward {
    void apply(Enterprise enterprise, ActiveContract contract);
}