package com.stoinkcraft.jobsites.contracts;

import com.stoinkcraft.jobsites.contracts.rewards.Reward;
import com.stoinkcraft.jobsites.contracts.triggers.ContractTrigger;
import com.stoinkcraft.jobsites.sites.JobSiteType;
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