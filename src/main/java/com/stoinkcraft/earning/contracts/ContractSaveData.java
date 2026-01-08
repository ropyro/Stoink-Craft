package com.stoinkcraft.earning.contracts;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContractSaveData {

    @Expose
    public UUID contractId;
    @Expose
    public UUID enterpriseId;
    @Expose
    public String definitionId;
    @Expose
    public int progress;
    @Expose
    public long expirationTime;
    @Expose
    public boolean completed;
    @Expose
    public boolean weekly;
    @Expose
    public Map<UUID, Integer> contributions = new HashMap<>();
}
