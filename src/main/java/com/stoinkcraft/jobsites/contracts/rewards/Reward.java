package com.stoinkcraft.jobsites.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.contracts.ActiveContract;

public interface Reward {
    void apply(Enterprise enterprise, ActiveContract contract);
}