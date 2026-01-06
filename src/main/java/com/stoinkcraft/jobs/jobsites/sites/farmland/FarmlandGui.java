package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.contracts.ContractDefinition;
import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.DescribableReward;
import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.jobsites.*;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;
import com.stoinkcraft.jobs.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.components.generators.PassiveMobGenerator;
import com.stoinkcraft.jobs.jobsites.components.structures.StructureData;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class FarmlandGui {

    private final FarmlandSite farmlandSite;
    private final Player opener;

    public FarmlandGui(FarmlandSite farmlandSite, Player opener){
        this.farmlandSite = farmlandSite;
        this.opener = opener;
    }

    private JobSiteUpgrade upgrade(String id) {
        return farmlandSite.getUpgrades()
                .stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private boolean unlocked(String upgradeId) {
        return farmlandSite.getData().getLevel(upgradeId) > 0;
    }

    public void openWindow() {
        Gui.Builder builder = createBaseGui(
                "# # # # ? # # # #",
                "# # # F A B # # #",
                "# # # # # # # # #",
                "# 1 2 . . . # C #",
                "# # # # # # # # #"

                );

        int xp = (int)farmlandSite.getData().getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);

        builder.addIngredient('?', new SimpleItem(
                new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§8§l» §a§lFarmland Help §8»")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lJobsite Progress §8»")
                        .addLoreLines(" §a• §fFarmland Level: §a" + level)
                        .addLoreLines(" §a• §fXP to next level: §a" + xpToNext + " XP")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lHow Jobsite XP Works §8»")
                        .addLoreLines(" §a• §fCompleting §adaily §fand §aweekly contracts")
                        .addLoreLines("   §fgrants §aFarmland XP")
                        .addLoreLines(" §a• §fXP increases your §aFarmland Level")
                        .addLoreLines(" §a• §fHigher levels unlock §abetter contracts")
                        .addLoreLines("   §fand are required for §aupgrades")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lUpgrades & Unlocks §8»")
                        .addLoreLines(" §a• §fCrop upgrades increase §agrowth speed")
                        .addLoreLines(" §a• §fAnimal upgrades increase §aspawn rate")
                        .addLoreLines("   §fand §amax capacity")
                        .addLoreLines(" §a• §fSome upgrades require a")
                        .addLoreLines("   §aspecific Farmland Level")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lEarning Money §8»")
                        .addLoreLines(" §a• §fHarvest crops and farm animals")
                        .addLoreLines(" §a• §fComplete contracts for §amoney")
                        .addLoreLines(" §a• §fMoney is split between")
                        .addLoreLines("   §fYou (§a" + (int)(SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE * 100) + "%§f)")
                        .addLoreLines("   §fand the Enterprise (§a" +
                                (100 - (int)(SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE * 100)) + "%§f)")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lNavigation §8»")
                        .addLoreLines(" §a• §fCrops Manager §8– §fSelect crops & upgrades")
                        .addLoreLines(" §a• §fAnimal Manager §8– §fSelect animals & upgrades")
                        .addLoreLines(" §a• §fContracts §8– §fView daily & weekly goals")
        ));

        builder.addIngredient('F', button(Material.IRON_HOE, "Crops Manager", this::openCropMenu));
        builder.addIngredient('A', button(Material.BEEF, "Animal Manager", this::openAnimalMenu));
        builder.addIngredient('B', simple(Material.HONEYCOMB, "Coming soon.."));
        builder.addIngredient('C', button(Material.GOLD_INGOT, "Contract List", this::openContractList));
        builder.addIngredient('1', new AbstractItem() {

            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder item = new ItemBuilder(Material.COBWEB);

                JobSiteStructure barn = farmlandSite.getStructure("barn");
                StructureData data = farmlandSite.getData().getStructure("barn");

                int jobsiteLevel = JobsiteLevelHelper.getLevelFromXp(
                        (int) farmlandSite.getData().getXp()
                );

                item.setDisplayName("§6Animal Barn");
                item.addLoreLines("§7Expands your farmland");
                item.addLoreLines("§7Allows more animals to be housed");
                item.addLoreLines(" ");

                switch (data.getState()) {

                    case LOCKED -> {
                        item.addLoreLines("§7Cost: §6$" + barn.getCost());
                        item.addLoreLines("§7Required Level: §e" + barn.getRequiredJobsiteLevel());

                        if (jobsiteLevel < barn.getRequiredJobsiteLevel()) {
                            item.addLoreLines("§c✖ You are level " + jobsiteLevel);
                        } else if (!barn.canUnlock(farmlandSite)) {
                            item.addLoreLines("§c✖ Requirements not met");
                        } else {
                            item.addLoreLines("§a✔ Ready to build");
                        }

                        item.addLoreLines(" ");
                        item.addLoreLines("§eClick to start construction");
                    }

                    case BUILDING -> {
                        long remaining = data.getRemainingMillis();
                        item.setMaterial(Material.SCAFFOLDING);
                        item.addLoreLines("§eUnder Construction");
                        item.addLoreLines(" ");
                        item.addLoreLines("§7Time Remaining:");
                        item.addLoreLines("§6" + ChatUtils.formatDuration(remaining));
                    }

                    case BUILT -> {
                        item.setMaterial(Material.OAK_LOG); // or OAK_PLANKS / HAY_BLOCK
                        item.addLoreLines("§a✔ Built");
                        item.addLoreLines(" ");
                        item.addLoreLines("§7This structure is complete");
                    }
                }

                return item;
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {

                JobSiteStructure barn = farmlandSite.getStructure("barn");
                StructureData data = farmlandSite.getData().getStructure("barn");

                switch (data.getState()) {

                    case LOCKED -> {
                        boolean success = farmlandSite.purchaseStructure(barn, p);

                        if (success) {
                            p.sendMessage("§aBarn construction started!");
                        } else {
                            p.sendMessage("§cYou cannot build the barn yet.");
                        }
                    }

                    case BUILDING -> {
                        long remaining = data.getRemainingMillis();
                        p.sendMessage("§eBarn is under construction.");
                        p.sendMessage("§7Time remaining: §6" + ChatUtils.formatDuration(remaining));
                    }

                    case BUILT -> {
                        p.sendMessage("§aThe barn is already built!");
                    }
                }
            }
        });

        builder.addIngredient('2', new AbstractItem() {

            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder item = new ItemBuilder(Material.BEEHIVE);

                JobSiteStructure beehive = farmlandSite.getStructure("beehive");
                StructureData data = farmlandSite.getData().getStructure("beehive");

                int jobsiteLevel = JobsiteLevelHelper.getLevelFromXp(
                        (int) farmlandSite.getData().getXp()
                );

                item.setDisplayName("§6Bee Hives");
                item.addLoreLines("§7Unlocks honey production");
                item.addLoreLines("§7Provides passive resources");
                item.addLoreLines(" ");

                switch (data.getState()) {

                    case LOCKED -> {
                        item.addLoreLines("§7Cost: §6$" + beehive.getCost());
                        item.addLoreLines("§7Required Level: §e" + beehive.getRequiredJobsiteLevel());

                        if (jobsiteLevel < beehive.getRequiredJobsiteLevel()) {
                            item.addLoreLines("§c✖ You are level " + jobsiteLevel);
                        } else if (!beehive.canUnlock(farmlandSite)) {
                            item.addLoreLines("§c✖ Requirements not met");
                        } else {
                            item.addLoreLines("§a✔ Ready to build");
                        }

                        item.addLoreLines(" ");
                        item.addLoreLines("§eClick to start construction");
                    }

                    case BUILDING -> {
                        long remaining = data.getRemainingMillis();
                        item.setMaterial(Material.SCAFFOLDING);
                        item.addLoreLines("§eUnder Construction");
                        item.addLoreLines(" ");
                        item.addLoreLines("§7Time Remaining:");
                        item.addLoreLines("§6" + ChatUtils.formatDuration(remaining));
                    }

                    case BUILT -> {
                        item.setMaterial(Material.BEE_NEST);
                        item.addLoreLines("§a✔ Built");
                        item.addLoreLines(" ");
                        item.addLoreLines("§7Bee hives are operational");
                    }
                }

                return item;
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {

                JobSiteStructure beehive = farmlandSite.getStructure("beehive");
                StructureData data = farmlandSite.getData().getStructure("beehive");

                switch (data.getState()) {

                    case LOCKED -> {
                        boolean success = farmlandSite.purchaseStructure(beehive, p);

                        if (success) {
                            p.sendMessage("§aBee Hive construction started!");
                        } else {
                            p.sendMessage("§cYou cannot build the bee hives yet.");
                        }
                    }

                    case BUILDING -> {
                        long remaining = data.getRemainingMillis();
                        p.sendMessage("§eBee Hives are under construction.");
                        p.sendMessage("§7Time remaining: §6" + ChatUtils.formatDuration(remaining));
                    }

                    case BUILT -> {
                        p.sendMessage("§aBee Hives are already built!");
                    }
                }
            }
        });

        open(builder.build(), "§8Farmland Menu");
    }
    private void openContractList() {

        StoinkCore core = StoinkCore.getInstance();
        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(opener.getUniqueId());

        if (enterprise == null) return;

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(
                        new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                                .setDisplayName(" ")
                ))
                .addIngredient('?', new SimpleItem(
                        new ItemBuilder(Material.OAK_SIGN)
                                .setDisplayName("§eContract Help")
                ))
                .build();

        List<ActiveContract> contracts =
                core.getContractManager()
                        .getContracts(enterprise, JobSiteType.FARMLAND);

        contracts.forEach(contract ->
                gui.addItems(createContractItem(contract)));

        Window.single()
                .setViewer(opener)
                .setTitle("§8Farmland Contracts")
                .setGui(gui)
                .build()
                .open();
    }

    private SimpleItem createContractItem(ActiveContract contract) {

        ContractDefinition def = contract.getDefinition();

        ItemBuilder builder = new ItemBuilder(def.displayItem());

        // ===== Title =====
        if (contract.isCompleted()) {
            builder.setDisplayName("§a✔ " + def.displayName());
        } else {
            builder.setDisplayName("§e" + def.displayName());
        }

        // ===== Description =====
        builder.addLoreLines(" ");
        def.description().forEach(line ->
                builder.addLoreLines("§7" + line));

        // ===== Progress =====
        builder.addLoreLines(" ");
        builder.addLoreLines("§eProgress: §f" +
                contract.getProgress() + "/" + contract.getTarget());

        // ===== Expiration =====
        builder.addLoreLines("§eExpires: §f" +
                formatTimeRemaining(contract.getExpirationTime()));

        // ===== Rewards =====
        builder.addLoreLines(" ");
        builder.addLoreLines("§6Rewards:");
        addRewardLore(builder, def.reward());

        return new SimpleItem(builder);
    }

    private void addRewardLore(ItemBuilder builder, Reward reward) {

        if (reward instanceof CompositeReward composite) {
            composite.getRewards().forEach(r ->
                    addRewardLore(builder, r));
            return;
        }

        if (reward instanceof DescribableReward describable) {
            describable.getLore().forEach(line ->
                    builder.addLoreLines("§7- " + line));
        }
    }

    private String formatTimeRemaining(long expiry) {

        long millis = expiry - System.currentTimeMillis();
        if (millis <= 0) return "Expired";

        long minutes = millis / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }

    private void openCropMenu() {
        Gui.Builder builder = createBaseGui(
                "# # # # ? # # # #",
                "# W C P B # G # #",
                "# # # # # # # # #"
        );

        addCropIngredient(builder, 'W', Material.WHEAT,
                CropGenerator.CropGeneratorType.WHEAT, null);

        addCropIngredient(builder, 'C', Material.CARROT,
                CropGenerator.CropGeneratorType.CARROT, "unlock_carrot");

        addCropIngredient(builder, 'P', Material.POTATO,
                CropGenerator.CropGeneratorType.POTATO, "unlock_potato");

        addCropIngredient(builder, 'B', Material.BEETROOT,
                CropGenerator.CropGeneratorType.BEETROOT, "unlock_beetroot");

        addUpgradeIngredient(builder, 'G',
                "crop_growth_speed", Material.WATER_BUCKET, "Growth Speed");

        open(builder.build(), "§8Crop Manager");
    }

    private void openAnimalMenu() {
        Gui.Builder builder = createBaseGui(
                "# # # # ? # # # #",
                "# C S P H # M K #",
                "# # # # # # # # #"
        );

        addMobIngredient(builder, 'C', Material.COW_SPAWN_EGG,
                PassiveMobGenerator.PassiveMobType.COW, null);

        addMobIngredient(builder, 'S', Material.SHEEP_SPAWN_EGG,
                PassiveMobGenerator.PassiveMobType.SHEEP, "unlock_sheep");

        addMobIngredient(builder, 'P', Material.PIG_SPAWN_EGG,
                PassiveMobGenerator.PassiveMobType.PIG, "unlock_pig");

        addMobIngredient(builder, 'H', Material.CHICKEN_SPAWN_EGG,
                PassiveMobGenerator.PassiveMobType.CHICKEN, "unlock_chicken");

        addUpgradeIngredient(builder, 'M',
                "mob_spawn_speed", Material.FEATHER, "Spawn Speed");

        addUpgradeIngredient(builder, 'K',
                "mob_capacity", Material.CHEST, "Capacity");

        open(builder.build(), "§8Animal Manager");
    }

    private void addCropIngredient(
            Gui.Builder builder,
            char slot,
            Material mat,
            CropGenerator.CropGeneratorType type,
            String unlockUpgradeId
    ) {
        JobSiteUpgrade upg = unlockUpgradeId == null ? null : findUpgrade(unlockUpgradeId);

        builder.addIngredient(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder item = new ItemBuilder(mat);
                boolean selected = farmlandSite.getData().getCurrentCropType() == type;
                boolean unlocked = upg == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                int jobsiteLevel =
                        JobsiteLevelHelper.getLevelFromXp((int) farmlandSite.getData().getXp());

                if (!unlocked) {
                    item.setDisplayName("§cUnlock " + type.name());
                    item.addLoreLines(" ");

                    item.addLoreLines("§7Cost: §6$" + upg.cost(1));
                    item.addLoreLines("§7Required Level: §e" + upg.requiredJobsiteLevel());

                    if (jobsiteLevel < upg.requiredJobsiteLevel()) {
                        item.addLoreLines("§c✖ You are level " + jobsiteLevel);
                    } else {
                        item.addLoreLines("§a✔ Requirement met");
                    }

                    item.addLoreLines(" ");
                    item.addLoreLines("§eClick to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + type.name() + " §7(Selected)");
                    item.addLoreLines("§7Currently active crop");

                } else {
                    item.setDisplayName("§eSelect " + type.name());
                    item.addLoreLines("§7Click to switch crop");
                }


                return item;
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {
                if (unlockUpgradeId != null &&
                        farmlandSite.getData().getLevel(unlockUpgradeId) == 0) {
                    farmlandSite.purchaseUpgrade(findUpgrade(unlockUpgradeId), p);
                } else {
                    farmlandSite.getCropGenerator().setCropType(type);
                }
            }
        });
    }

    private void addMobIngredient(
            Gui.Builder builder,
            char slot,
            Material mat,
            PassiveMobGenerator.PassiveMobType type,
            String unlockUpgradeId
    ) {
        JobSiteUpgrade upg = unlockUpgradeId == null ? null : findUpgrade(unlockUpgradeId);

        builder.addIngredient(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder item = new ItemBuilder(mat);
                boolean selected = farmlandSite.getData().getCurrentMobType() == type;
                boolean unlocked = upg == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                int jobsiteLevel =
                        JobsiteLevelHelper.getLevelFromXp((int) farmlandSite.getData().getXp());

                if (!unlocked) {
                    item.setDisplayName("§cUnlock " + type.getDisplayName());
                    item.addLoreLines(" ");

                    item.addLoreLines("§7Cost: §6$" + upg.cost(1));
                    item.addLoreLines("§7Required Level: §e" + upg.requiredJobsiteLevel());

                    if (jobsiteLevel < upg.requiredJobsiteLevel()) {
                        item.addLoreLines("§c✖ You are level " + jobsiteLevel);
                    } else {
                        item.addLoreLines("§a✔ Requirement met");
                    }

                    item.addLoreLines(" ");
                    item.addLoreLines("§eClick to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + type.getDisplayName() + " §7(Selected)");
                    item.addLoreLines("§7Currently active animal");

                } else {
                    item.setDisplayName("§eSelect " + type.getDisplayName());
                    item.addLoreLines("§7Click to switch animal");
                }

                return item;
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {
                if (unlockUpgradeId != null &&
                        farmlandSite.getData().getLevel(unlockUpgradeId) == 0) {
                    farmlandSite.purchaseUpgrade(findUpgrade(unlockUpgradeId), p);
                } else {
                    farmlandSite.getMobGenerator().setMobType(type);
                }
            }
        });
    }

    private void addUpgradeIngredient(
            Gui.Builder builder,
            char slot,
            String upgradeId,
            Material mat,
            String name
    ) {
        JobSiteUpgrade upg = findUpgrade(upgradeId);
        if (upg == null) return;

        builder.addIngredient(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                int lvl = farmlandSite.getData().getLevel(upgradeId);
                ItemBuilder item = new ItemBuilder(mat)
                        .setDisplayName("§e" + name)
                        .addLoreLines("Level: " + lvl + "/" + upg.maxLevel());

                if (lvl < upg.maxLevel()) {
                    item.addLoreLines("Cost: $" + upg.cost(lvl + 1));
                    item.addLoreLines("Click to upgrade");
                } else {
                    item.addLoreLines("§aMax Level");
                }

                int currentJobsiteLevel =
                        JobsiteLevelHelper.getLevelFromXp((int) farmlandSite.getData().getXp());

                item.addLoreLines("Required Level: " + upg.requiredJobsiteLevel());

                if (currentJobsiteLevel < upg.requiredJobsiteLevel()) {
                    item.addLoreLines("§cYou are level " + currentJobsiteLevel);
                } else {
                    item.addLoreLines("§aYou meet the level requirement");
                }
                return item;
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {
                if(farmlandSite.purchaseUpgrade(upg, p)){
                    ChatUtils.sendMessage(p, "Purchased upgrade, " + name);
                }else{
                    ChatUtils.sendMessage(p, "Unable to purchase upgrade, insufficient funds or jobsite level");
                }
                p.closeInventory();
            }
        });
    }

    private JobSiteUpgrade findUpgrade(String id) {
        return farmlandSite.getUpgrades().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private SimpleItem filler() {
        return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
    }

    private SimpleItem simple(Material mat, String name) {
        return new SimpleItem(new ItemBuilder(mat).setDisplayName(name));
    }

    private AbstractItem button(Material mat, String name, Runnable action) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(mat).setDisplayName(name);
            }

            @Override
            public void handleClick(ClickType click, Player p, InventoryClickEvent e) {
                action.run();
            }
        };
    }

    private Gui.Builder createBaseGui(String... structure) {
        Gui.Builder builder = Gui.normal().setStructure(structure);

        builder.addIngredient('#', filler());
        //builder.addIngredient('?', simple(Material.OAK_SIGN, "Help"));

        return builder;
    }

    private void open(Gui gui, String title) {
        Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build()
                .open();
    }
}
