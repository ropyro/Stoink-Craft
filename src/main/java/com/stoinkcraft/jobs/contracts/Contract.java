package com.stoinkcraft.jobs.contracts;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSiteType;

import java.util.UUID;

public class Contract {

    @Expose
    protected UUID contractId;
    @Expose
    protected UUID enterpriseId;
    @Expose
    protected int targetAmount;
    @Expose
    protected int currentProgress;
    @Expose
    protected double reward;
    @Expose
    protected long expirationTime;
    @Expose
    protected JobSiteType jobSiteType;

    @Expose
    protected ContractType contractType;

    private boolean expired;

    public Contract(ContractType contractType, long expirationTime){
        expired = expirationTime < System.currentTimeMillis();
    }
}
