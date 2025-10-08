package com.stoinkcraft.enterprise;

public enum Role {
    CEO("CEO"), EMPLOYEE("Employee");

    String name;

    Role(String name){
        this.name = name;
    }

    public String roleName(){
        return name;
    }
}
