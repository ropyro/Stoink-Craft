package com.stoinkcraft.earning.jobsites;

public enum JobSiteType {

    SKYRISE("Skyrise"), QUARRY("Quarry"), FARMLAND("Farmland"), GRAVEYARD("Graveyard");

    String displayName;

    JobSiteType(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return this.displayName;
    }

}
