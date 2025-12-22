package com.stoinkcraft.jobs.contracts;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.enterprise.EnterpriseManager;
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
    protected JobSiteType jobSiteType;

    @Expose
    protected ContractType contractType;
    @Expose
    protected double reward;
    @Expose
    protected long expirationTime;

    private boolean expired;

    public Contract(ContractType contractType, JobSiteType jobSiteType, double reward, int targetAmount,
                    int currentProgress, UUID contractId, UUID enterpriseId, long expirationTime){
        this.contractType = contractType;
        this.jobSiteType = jobSiteType;
        this.reward = reward;
        this.targetAmount = targetAmount;
        this.currentProgress = currentProgress;
        this.contractId = contractId;
        this.enterpriseId = enterpriseId;
        this.expirationTime = expirationTime;
        expired = expirationTime < System.currentTimeMillis();
    }

    public Contract(ContractType contractType, JobSiteType jobSiteType, double reward, int targetAmount,
                    int currentProgress, UUID enterpriseId, long expirationTime){
        this(contractType, jobSiteType, reward, targetAmount, currentProgress, UUID.randomUUID(), enterpriseId, expirationTime);
    }

    public void addProgress(int progress){
        this.currentProgress += progress;
        if(currentProgress >= targetAmount){
            currentProgress = targetAmount;
            reward();
        }
    }

    private void reward(){
        EnterpriseManager.getEnterpriseManager().getEnterpriseByID(enterpriseId).increaseNetworth(reward);
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getEnterpriseId() {
        return enterpriseId;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public double getReward() {
        return reward;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public JobSiteType getJobSiteType() {
        return jobSiteType;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public boolean isExpired() {
        return expired;
    }
}
