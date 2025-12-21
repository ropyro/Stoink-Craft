package com.stoinkcraft.jobs.contracts;

import java.util.ArrayList;
import java.util.List;

public class ContractManager {

    private List<Contract> contractList = new ArrayList<>();

    public ContractManager(){

    }

    public List<Contract> getContractList(){
        return this.contractList;
    }



}
