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

    private boolean completed;

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
        this.completed = currentProgress >= targetAmount;
    }

    public Contract(ContractType contractType, JobSiteType jobSiteType, double reward, int targetAmount, UUID enterpriseId, long expirationTime){
        this(contractType, jobSiteType, reward, targetAmount, 0, UUID.randomUUID(), enterpriseId, expirationTime);
    }

    public boolean canProgress(){
        return !isExpired() && !completed;
    }

    public void addProgress(int progress){
        if(!canProgress()) return;
        this.currentProgress += progress;
        if(currentProgress >= targetAmount){
            currentProgress = targetAmount;
            this.completed = true;
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
        return System.currentTimeMillis() > expirationTime;
    }

    public boolean isCompleted(){
        return completed;
    }
}
