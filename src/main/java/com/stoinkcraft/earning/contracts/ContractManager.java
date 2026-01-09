package com.stoinkcraft.earning.contracts;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.contracts.triggers.ContractTrigger;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteManager;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.utils.ContractTimeUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    public void regenerateContracts(Enterprise enterprise){
        contracts.get(enterprise.getID()).clear();
        generateContracts(enterprise, false);
        generateContracts(enterprise, true);
        enterprise.sendEnterpriseMessage("All contracts have been regenerated");
    }

    public void handleContext(Enterprise enterprise, ContractContext context) {
        JobSiteType jobSiteType = context.getJobSiteType();
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
        if(!getContracts(enterprise).stream().anyMatch(ac -> !ac.isCompleted())){
            regenerateContracts(enterprise, jobSiteType);
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

        // Get or create the list directly - DO NOT call getContracts() to avoid recursion
        List<ActiveContract> enterpriseContracts =
                contracts.computeIfAbsent(enterprise.getID(), k -> new ArrayList<>());

        for (JobSiteType type : JobSiteType.values()) {
            JobSite site = jsm.getJobSite(type);
            if (site == null) continue;

            // Fixed count: 7 daily, 7 weekly
            int count = weekly
                    ? ContractScaling.WEEKLY_CONTRACT_COUNT
                    : ContractScaling.DAILY_CONTRACT_COUNT;

            // Filter from local list - not through getContracts()
            Set<String> activeContractIds = enterpriseContracts.stream()
                    .filter(c -> c.getDefinition().jobSiteType() == type)
                    .filter(c -> c.isWeekly() == weekly)
                    .filter(c -> !c.isExpired())
                    .map(c -> c.getDefinition().id())
                    .collect(Collectors.toSet());

            // Get available contracts excluding already active ones
            List<ContractDefinition> available =
                    contractPool.getAvailableContracts(enterprise, type, weekly).stream()
                            .filter(def -> !activeContractIds.contains(def.id()))
                            .collect(Collectors.toCollection(ArrayList::new));

            Collections.shuffle(available);

            // Calculate current count from local list
            int currentCount = (int) enterpriseContracts.stream()
                    .filter(c -> c.getDefinition().jobSiteType() == type)
                    .filter(c -> c.isWeekly() == weekly)
                    .filter(c -> !c.isExpired())
                    .count();

            int needed = Math.max(0, count - currentCount);

            available.stream()
                    .limit(needed)
                    .forEach(def -> {
                        long expiry = weekly
                                ? ContractTimeUtil.nextWeek()
                                : ContractTimeUtil.nextDay();

                        // Add directly to the list
                        enterpriseContracts.add(
                                new ActiveContract(enterprise.getID(), def, expiry, weekly));
                    });
        }
    }

    public int generateContracts(Enterprise enterprise, boolean weekly, JobSiteType type) {
        AtomicInteger amountGenerated = new AtomicInteger();

        JobSiteManager jsm = enterprise.getJobSiteManager();

        // Get or create the list directly - DO NOT call getContracts() to avoid recursion
        List<ActiveContract> enterpriseContracts =
                contracts.computeIfAbsent(enterprise.getID(), k -> new ArrayList<>());

        JobSite site = jsm.getJobSite(type);
        if (site == null) return 0;

        // Fixed count: 7 daily, 7 weekly
        int count = weekly
                ? ContractScaling.WEEKLY_CONTRACT_COUNT
                : ContractScaling.DAILY_CONTRACT_COUNT;

        // Filter from local list - not through getContracts()
        Set<String> activeContractIds = enterpriseContracts.stream()
                .filter(c -> c.getDefinition().jobSiteType() == type)
                .filter(c -> c.isWeekly() == weekly)
                .filter(c -> !c.isExpired())
                .map(c -> c.getDefinition().id())
                .collect(Collectors.toSet());

        // Get available contracts excluding already active ones
        List<ContractDefinition> available =
                contractPool.getAvailableContracts(enterprise, type, weekly).stream()
                        .filter(def -> !activeContractIds.contains(def.id()))
                        .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(available);

        // Calculate current count from local list
        int currentCount = (int) enterpriseContracts.stream()
                .filter(c -> c.getDefinition().jobSiteType() == type)
                .filter(c -> c.isWeekly() == weekly)
                .filter(c -> !c.isExpired())
                .count();

        int needed = Math.max(0, count - currentCount);

        available.stream()
                .limit(needed)
                .forEach(def -> {
                    long expiry = weekly
                            ? ContractTimeUtil.nextWeek()
                            : ContractTimeUtil.nextDay();

                    // Add directly to the list
                    enterpriseContracts.add(
                            new ActiveContract(enterprise.getID(), def, expiry, weekly));
                    amountGenerated.getAndIncrement();
                });
        return amountGenerated.get();
    }

    public void regenerateContracts(Enterprise enterprise, JobSiteType type) {
        List<ActiveContract> enterpriseContracts = contracts.get(enterprise.getID());

        if (enterpriseContracts != null) {
            // Remove only contracts for the specified JobSiteType
            enterpriseContracts.removeIf(c -> c.getDefinition().jobSiteType() == type);
        }

        int amountGenerated = 0;
        amountGenerated += generateContracts(enterprise, false, type);
        amountGenerated += generateContracts(enterprise, true, type);

        enterprise.sendEnterpriseMessage("§6§lAll " + type.getDisplayName() + " contracts were completed!",
                " ",
                "§a" + amountGenerated + " §7New contracts are now available",
                " ");
    }

    public ContractPool getContractPool() {
        return contractPool;
    }

    public void setContracts(Enterprise enterprise, List<ActiveContract> activeContracts) {
        this.contracts.put(enterprise.getID(), new ArrayList<>(activeContracts));
    }
}
