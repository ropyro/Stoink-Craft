package com.stoinkcraft.earning.contracts;

import com.stoinkcraft.earning.contracts.rewards.Reward;
import com.stoinkcraft.earning.contracts.triggers.ContractTrigger;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public record ContractDefinition(
        String id,
        ContractTrigger trigger,
        JobSiteType jobSiteType,
        int minJobsiteLevel,
        int targetAmount,
        Map<String, Integer> requiredUpgrades,
        Reward reward,

        Material displayItem,
        String displayName,
        List<String> description
) {}