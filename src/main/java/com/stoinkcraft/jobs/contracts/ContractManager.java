package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.triggers.ContractTrigger;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteManager;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.utils.ContractTimeUtil;

import java.util.*;

public class ContractManager {

    private final Map<UUID, List<ActiveContract>> contracts = new HashMap<>();
    private final ContractPool contractPool;

    public ContractManager(ContractPool contractPool) {
        this.contractPool = contractPool;
    }

    public List<ActiveContract> getContracts(Enterprise enterprise) {
        List<ActiveContract> list =
                contracts.computeIfAbsent(enterprise.getID(), k -> new ArrayList<>());

        if (list.isEmpty()) {
            generateContracts(enterprise, false);
            generateContracts(enterprise, true);
        }
        return list;
    }

    public List<ActiveContract> getContracts(Enterprise enterprise, JobSiteType type) {
        return getContracts(enterprise).stream()
                .filter(c -> c.getDefinition().jobSiteType() == type)
                .toList();
    }

    public void addContract(Enterprise enterprise, ActiveContract contract) {
        contracts.computeIfAbsent(enterprise.getID(), k -> new ArrayList<>())
                .add(contract);
    }

    public void handleContext(Enterprise enterprise, ContractContext context) {

        for (ActiveContract contract : getContracts(enterprise, context.getJobSiteType())) {

            if (!contract.canProgress()) continue;

            ContractTrigger trigger = contract.getDefinition().trigger();
            if (!trigger.matches(context)) continue;

            contract.addProgress(
                    context.getPlayer().getUniqueId(),
                    trigger.getProgressIncrement(context)
            );
            ContractFeedbackManager feedbackManager = StoinkCore.getInstance().getContractFeedbackManager();
            feedbackManager.showBossBar(context.getPlayer(), contract);
            feedbackManager.clearIfFinished(context.getPlayer(), contract);
        }
    }

    public void handleDailyReset() {
        removeExpiredContracts();

        for (Enterprise enterprise : StoinkCore.getInstance().getEnterpriseManager().getEnterpriseList()) {
            generateContracts(enterprise, false);
        }
    }

    public void handleWeeklyReset() {
        removeExpiredContracts();

        for (Enterprise enterprise : StoinkCore.getInstance().getEnterpriseManager().getEnterpriseList()) {
            generateContracts(enterprise, true);
        }
    }

    public void removeExpiredContracts() {

        for (Iterator<Map.Entry<UUID, List<ActiveContract>>> it =
             contracts.entrySet().iterator(); it.hasNext(); ) {

            Map.Entry<UUID, List<ActiveContract>> entry = it.next();
            List<ActiveContract> list = entry.getValue();

            list.removeIf(ActiveContract::isExpired);

            if (list.isEmpty()) {
                it.remove();
            }
        }
    }

    public void generateContracts(Enterprise enterprise, boolean weekly) {

        JobSiteManager jsm = enterprise.getJobSiteManager();

        for (JobSiteType type : JobSiteType.values()) {

            JobSite site = jsm.getJobSite(type);
            if (site == null) continue;

            int count = weekly
                    ? ContractScaling.weeklyContractsForLevel(site.getLevel())
                    : ContractScaling.dailyContractsForLevel(site.getLevel());

            List<ContractDefinition> available =
                    new ArrayList<>(contractPool.getAvailableContracts(enterprise, type, weekly));

            Collections.shuffle(available);

            available.stream()
                    .limit(count)
                    .forEach(def -> {
                        long expiry = weekly
                                ? ContractTimeUtil.nextWeek()
                                : ContractTimeUtil.nextDay();

                        addContract(enterprise,
                                new ActiveContract(enterprise.getID(), def, expiry));
                    });
        }
    }

    public ContractPool getContractPool() {
        return contractPool;
    }

    public void setContracts(Enterprise enterprise, List<ActiveContract> activeContracts){
        this.contracts.put(enterprise.getID(), new ArrayList<>(activeContracts));
    }
}
