package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSiteType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ContractManager {

    private HashMap<Enterprise, List<Contract>> contractMap = new HashMap<>();

    public ContractManager(){
        loadContracts();
    }

    public List<Contract> getContracts(Enterprise enterprise) {
        return contractMap.getOrDefault(enterprise, Collections.emptyList());
    }

    public void addContract(Enterprise enterprise, Contract contract) {
        contractMap.computeIfAbsent(enterprise, e -> new ArrayList<>())
                .add(contract);
    }

    public List<Contract> getContracts(Enterprise enterprise, JobSiteType jobSiteType) {
        return getContracts(enterprise).stream()
                .filter(c -> c.getJobSiteType() == jobSiteType)
                .toList();
    }


    public void loadContracts(){
        //TODO: add deserialization from JSON file
    }

    public void saveContracts(){
        //TODO: add serialization to JSON file
    }
}
