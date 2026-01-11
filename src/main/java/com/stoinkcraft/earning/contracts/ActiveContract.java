package com.stoinkcraft.earning.contracts;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.reputation.ReputationCalculator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ActiveContract {

    private final UUID contractId;
    private final UUID enterpriseId;
    private final ContractDefinition definition;

    private int progress;
    private final long expirationTime;
    private boolean completed;
    private final boolean weekly;
    private final boolean bonus;

    private final Map<UUID, Integer> contributions;

    public ActiveContract(UUID enterpriseId, ContractDefinition definition, long expirationTime) {
        this(enterpriseId, definition, expirationTime, false, false);
    }

    public ActiveContract(UUID enterpriseId, ContractDefinition definition, long expirationTime, boolean weekly) {
        this(enterpriseId, definition, expirationTime, weekly, false);
    }

    public ActiveContract(UUID enterpriseId, ContractDefinition definition, long expirationTime, boolean weekly, boolean bonus) {
        this.contractId = UUID.randomUUID();
        this.enterpriseId = enterpriseId;
        this.definition = definition;
        this.expirationTime = expirationTime;
        this.weekly = weekly;
        this.bonus = bonus;
        this.contributions = new HashMap<>();
    }

    public ActiveContract(
            UUID contractId,
            UUID enterpriseId,
            ContractDefinition definition,
            long expirationTime,
            int progress,
            boolean completed,
            boolean weekly,
            boolean bonus,
            Map<UUID, Integer> contributions
    ) {
        this.contractId = contractId;
        this.enterpriseId = enterpriseId;
        this.definition = definition;
        this.expirationTime = expirationTime;
        this.progress = progress;
        this.completed = completed;
        this.weekly = weekly;
        this.bonus = bonus;
        this.contributions = new HashMap<>(contributions);
    }

    public ContractSaveData toSaveData() {
        ContractSaveData data = new ContractSaveData();
        data.contractId = contractId;
        data.enterpriseId = enterpriseId;
        data.definitionId = definition.id();
        data.progress = progress;
        data.expirationTime = expirationTime;
        data.completed = completed;
        data.weekly = weekly;
        data.bonus = bonus;
        data.contributions = new HashMap<>(contributions);
        return data;
    }

    public boolean canProgress() {
        return !completed && !isExpired();
    }

    public void addProgress(UUID playerId, int amount) {
        if (!canProgress()) return;

        int remaining = definition.targetAmount() - progress;
        int actualAmount = Math.min(amount, remaining);

        progress += actualAmount;
        contributions.merge(playerId, actualAmount, Integer::sum);

        if (progress >= definition.targetAmount()) {
            completed = true;
            onCompleted();
        }
    }

    private void reward() {
        Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager()
                .getEnterpriseByID(enterpriseId);

        definition.reward().apply(enterprise, this);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public UUID getContractId() {
        return contractId;
    }

    public ContractDefinition getDefinition() {
        return definition;
    }

    public int getProgress() {
        return progress;
    }

    public int getTarget() {
        return definition.targetAmount();
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isWeekly() {
        return weekly;
    }

    public boolean isBonus() {
        return bonus;
    }

    private void onCompleted() {
        Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager()
                .getEnterpriseByID(enterpriseId);

        // Apply rewards
        reward();

        // Add reputation for completing the contract (both regular and bonus give positive rep)
        double repGain = ReputationCalculator.getCompletionReputation(weekly);
        enterprise.addReputation(repGain);

        // Announce completion
        StoinkCore.getInstance()
                .getContractFeedbackManager()
                .announceCompletion(enterprise, this);
    }

    public Map<UUID, Integer> getContributions() {
        return Collections.unmodifiableMap(contributions);
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Map<UUID, Double> getContributionPercentages() {
        if (progress == 0) return Map.of();

        return contributions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() / (double) progress
                ));
    }
}
