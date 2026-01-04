package com.stoinkcraft.jobs.contracts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContractSaveData {

    public UUID contractId;
    public UUID enterpriseId;
    public String definitionId;

    public int progress;
    public long expirationTime;
    public boolean completed;

    public Map<UUID, Integer> contributions = new HashMap<>();
}
