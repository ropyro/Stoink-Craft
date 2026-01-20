package com.stoinkcraft.earning.jobsites.sites.skyrise;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.components.JobSiteNPC;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkyriseSite extends JobSite {

    private JobSiteHologram entryHologram;

    private JobSiteNPC farmerJoe;
    private static final String farmerJoeTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc0NDA5MzMxMTMxMCwKICAicHJvZmlsZUlkIiA6ICJiOWIzY2RlZmIyZmQ0YWY1ODQxMGViZWZjY2ZmYTBhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJpbnRlcnNlY2F0byIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kY2Y1Mzk3YmEwNTc5ZTI3NGMxZDJhN2I4M2ExMmU1MDQ0NDBjYjQzOTgzODZkNTI4MTk5NmQ0MWMwNjc3N2M1IgogICAgfQogIH0KfQ==";
    private static final String farmerJoeSignature = "k+SC0/ie439qZpjQQXSiYWRP4MWW4RLOsPdhc0KB/YBmJZGs1K/KhVtRrn1KFFV263foaBjEQtE/yoX9L5VYKmgJmTxscpDlX0KwnTVpZgDOwTU0rGUxg0VElmyqxRt49FH7UzJeuFs880jzHDBxuoRw28gHOMkaiE2WtdSDOXF6KcfwyZbZ/IlybI6ydcgzsVe6L8OXJuVEFStEuaPoE27qzz4OZX5wrYpW4FtmGIkISVXIEgh4Cd+R/toaXBLV7Egz/IuWrueihUv48QXv3lbPSncCuOcpqIjfJ+JSR1CcvkypbqhKdBMko7hTH77libQrz1k79Ghtppjw7cC6/tRdPAqOtNSAPk82nHbogctI7X7RBv+5ETtKK2nw8ckTyuqikgICYwjbmDNhhuSZHodb16pQy9LaGPXqi5ti4TgMFxsY98+Yys4N1Fz0WuMl1UDm44mjmH4o1aqsjeZKem/cqZbh3rppzLGZ/4lhmooTChGfPIONGCPdpgDh1yxzw8k96RNpG0bDJo5VzQB5LzuENiHgi1vBxFXdAQii5o7XZd6SPexmmwz4BNGymebjhnQ/VSj8PfTpF/SBBEYoJF3T7WR6Y/8UpbqDCbUQJhHxRSGu+qTg5CX2nkq1hw4bhKklOGRRlC0retK7oYGJhE3aJSY8m+wLQeJGL19+A8Y=";
    public static final Vector farmerJoeOffset = new Vector(-18.5, 1.5, -6.5);

    private JobSiteNPC minerBob;
    private static final String minerBobTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY4MDE3NzE1NzgxMSwKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY5YmRmYmIzZGRiNTA3YThjYjZiZTUyMDU3MWU0YjZmMjExNjBjMzUwNTlkMTFmZGYzYTI5NjI5ZmU1N2Y1MCIKICAgIH0KICB9Cn0=";
    private static final String minerBobSignature = "FGpddwZXQZ8jfAyCYxXQg5k9u6xJBszvw/vsIHV8sPjnsyBJDChZwHKXfc5bo5Pro1mfQZwJ5oQqtFlRbR4Mp2hODJmj96C9HPMFIjYTMXXGHo0FcHQ8fJPz93/qnhMIZquDXdgqVzcFYz0ga+DwkPhsEUSiDx/qzka7nESev5Dd+/V1gpIT8a0MJL25xAov/yUXEZ71I2v01SeMa9nqhiDxCpiu8VhpIR2BLdDGPSMqQz4uL+rzYWXkzjByWZpoSRRLf5cMqWcBFaoRLlR6ILAhhOdNPBPll9UDcfRVUestDxo0xxfgPddgPWPAik8RbDG3sNooGFfjviAcI3XzSO5k/esE+QJ97WRR/FQ9da94Zn4dsS6nEWV4n6n6L3cqsRyoDeboT5n0Y6HXiyudsfmIHHho12uttY8jkPtx+/ZDC/aJWLMAW89n5TqWzhv6s6q9qdiidwmw2rIBwPUx9fiVnWHiDBsL0V3/c2QHlfaqJ2j3sP/BpXnS2chQGI9Ub40Igm1MJHH5J1zs/oNLblYQ0McWoEx57oXOVx5qSnc4TzL36zB5AqSf0NGjF68cS+FP0YD3YjWWBABdZ9E+C34C38Ps9OCDM42GuQHC2wADHh0heidDr4VAABYA1MqpP7cCNb0hzTwwJtgijjCQ9ni63zbBdHR1clcN/4bEhT8=";
    public static final Vector minerBobOffset = new Vector(-15.5, 1.5, -6.5);

    public SkyriseSite(Enterprise enterprise, Location spawnPoint, SkyriseData data) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/building.schem"),
                data, data.isBuilt());

        entryHologram = new JobSiteHologram(this, "entryway", new Vector(-5.5, 4, 0.5), getEntryHoloLines());

        farmerJoe = new JobSiteNPC(this, ChatColor.GREEN + "Farmer Joe", farmerJoeOffset, farmerJoeTexture, farmerJoeSignature){
            @Override
            public void onRightClick(NPCRightClickEvent event) {
                super.onRightClick(event);
                getEnterprise().getJobSiteManager().getFarmlandSite().teleportPlayer(event.getClicker());
            }
        };

        minerBob = new JobSiteNPC(this, ChatColor.GOLD + "Miner Bob", minerBobOffset, minerBobTexture, minerBobSignature){
            @Override
            public void onRightClick(NPCRightClickEvent event) {
                super.onRightClick(event);
                getEnterprise().getJobSiteManager().getQuarrySite().teleportPlayer(event.getClicker());
            }
        };

        registerComponents();
    }

    private void registerComponents(){
        addComponent(entryHologram);
        addComponent(farmerJoe);
        addComponent(minerBob);
    }

    @Override
    public void build() {
        super.build();
    }

    @Override
    public void disband() {
        super.disband();
    }

    @Override
    public void tick() {
        super.tick();
    }

    private List<String> getEntryHoloLines(){
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + enterprise.getName() + "'s");
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Skyrise Building");
        return entryHoloGramLines;
    }

    @Override
    public SkyriseData getData() {
        return (SkyriseData)super.getData();
    }
}