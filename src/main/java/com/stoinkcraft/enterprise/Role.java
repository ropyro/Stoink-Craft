package com.stoinkcraft.enterprise;

public enum Role {
    CEO("CEO", "§c§lCEO"), EMPLOYEE("Employee", "§eEmployee");

    String name;
    String formattedName;

    Role(String name, String formattedName){
        this.name = name;
        this.formattedName = formattedName;
    }

    public String roleName(){
        return name;
    }

    public String getFormattedName(){
        return formattedName;
    }
}
