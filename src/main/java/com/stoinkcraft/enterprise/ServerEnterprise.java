package com.stoinkcraft.enterprise;

import com.stoinkcraft.items.booster.Booster;
import com.stoinkcraft.utils.SCConstants;
import java.util.UUID;

public class ServerEnterprise extends Enterprise {

    public ServerEnterprise(String name) {
        super(name, SCConstants.serverCEO, "SERVER");
    }

    // Constructor for Gson deserialization - matches parent signature
    public ServerEnterprise(String name, UUID ceo, double bankBalance, double reputation,
                            int outstandingShares, Booster activeBooster,
                            UUID enterpriseID) {
        super(name, ceo, bankBalance, reputation, outstandingShares, activeBooster, enterpriseID);
    }

    @Override
    public void hireEmployee(UUID employee) {
        // Server enterprises have no employee limit
        super.getMembers().put(employee, Role.EMPLOYEE);
    }
}