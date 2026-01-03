package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;

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

    private final Map<UUID, Integer> contributions = new HashMap<>();

    public ActiveContract(UUID enterpriseId, ContractDefinition definition, long expirationTime) {
        this.contractId = UUID.randomUUID();
        this.enterpriseId = enterpriseId;
        this.definition = definition;
        this.expirationTime = expirationTime;
    }

    public boolean canProgress() {
        return !completed && !isExpired();
    }

    public void addProgress(UUID playerId, int amount) {
        if (!canProgress()) return;

        progress += amount;
        contributions.merge(playerId, amount, Integer::sum);

        if (progress >= definition.targetAmount()) {
            progress = definition.targetAmount();
            completed = true;
            reward();
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

    private void onCompleted() {
        Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager()
                .getEnterpriseByID(enterpriseId);

        // Apply rewards
        definition.reward().apply(enterprise, this);

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
