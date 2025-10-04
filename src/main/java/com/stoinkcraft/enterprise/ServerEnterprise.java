package com.stoinkcraft.enterprise;

import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ServerEnterprise extends Enterprise{

    public ServerEnterprise(String name) {
        super(name, SCConstants.serverCEO);
    }

    @Override
    public void hireEmployee(UUID employee){
        super.getMembers().put(employee, Role.EMPLOYEE);
    }

    @Override
    public boolean promoteMember(UUID member){
        return false;
    }
}
