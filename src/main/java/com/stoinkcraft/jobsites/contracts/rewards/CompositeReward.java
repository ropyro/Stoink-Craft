package com.stoinkcraft.jobsites.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.contracts.ActiveContract;

import java.util.List;

public class CompositeReward implements Reward {

    private final List<Reward> rewards;

    public CompositeReward(List<Reward> rewards) {
        this.rewards = rewards;
    }

    @Override
    public void apply(Enterprise enterprise, ActiveContract contract) {
        rewards.forEach(r -> r.apply(enterprise, contract));
    }

    public List<Reward> getRewards() {
        return rewards;
    }
}