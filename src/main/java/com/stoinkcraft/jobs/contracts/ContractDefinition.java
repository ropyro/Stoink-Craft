package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.contracts.triggers.ContractTrigger;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
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