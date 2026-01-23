package com.stoinkcraft.jobsites.contracts;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.JobsiteLevelHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ContractPool {

    private final List<ContractDefinition> dailyContracts;
    private final List<ContractDefinition> weeklyContracts;

    public ContractPool(List<ContractDefinition> daily, List<ContractDefinition> weekly) {
        this.dailyContracts = daily;
        this.weeklyContracts = weekly;
    }

    public List<ContractDefinition> getAvailableContracts(
            Enterprise enterprise,
            JobSiteType jobSiteType,
            boolean weekly
    ) {
        JobSite site = enterprise.getJobSiteManager().getJobSite(jobSiteType);
        if (site == null) return List.of();

        Map<String, Integer> upgrades =
                site.getData().getUpgrades();

        return (weekly ? weeklyContracts : dailyContracts).stream()
                .filter(def -> def.jobSiteType() == jobSiteType)
                .filter(def -> def.minJobsiteLevel() <= JobsiteLevelHelper.getLevelFromXp((int)site.getData().getXp()))
                .filter(def -> hasRequiredUpgrades(def, upgrades))
                .toList();
    }

    private boolean hasRequiredUpgrades(
            ContractDefinition def,
            Map<String, Integer> upgrades
    ) {
        for (Map.Entry<String, Integer> entry : def.requiredUpgrades().entrySet()) {

            String upgradeId = entry.getKey();
            int requiredLevel = entry.getValue();

            int currentLevel = upgrades.getOrDefault(upgradeId, 0);

            if (currentLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    public ContractDefinition getById(String id) {
        return Stream.concat(dailyContracts.stream(), weeklyContracts.stream())
                .filter(def -> def.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}