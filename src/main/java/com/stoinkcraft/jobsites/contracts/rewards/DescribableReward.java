package com.stoinkcraft.jobsites.contracts.rewards;

import java.util.List;

public interface DescribableReward extends Reward {
    List<String> getLore();
}
