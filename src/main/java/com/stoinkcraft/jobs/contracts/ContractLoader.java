package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.JobSiteXpReward;
import com.stoinkcraft.jobs.contracts.rewards.MoneyReward;
import com.stoinkcraft.jobs.contracts.triggers.CropHarvestTrigger;
import com.stoinkcraft.jobs.contracts.triggers.MobKillTrigger;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContractLoader {

    public static ContractPool load() {

        List<ContractDefinition> daily = new ArrayList<>();
        List<ContractDefinition> weekly = new ArrayList<>();

        /*
         * =========================
         * FARMLAND – CROPS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "farmland_wheat_basic",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                1,
                64,
                Map.of(), // wheat is always unlocked
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 25)
                )),
                Material.WHEAT,
                "Harvest Wheat",
                List.of("Harvest wheat grown in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_carrot_basic",
                new CropHarvestTrigger(Material.CARROTS),
                JobSiteType.FARMLAND,
                5,
                64,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(750, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 35)
                )),
                Material.CARROT,
                "Harvest Carrots",
                List.of("Harvest carrots in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_potato_basic",
                new CropHarvestTrigger(Material.POTATOES),
                JobSiteType.FARMLAND,
                20,
                96,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 50)
                )),
                Material.POTATO,
                "Harvest Potatoes",
                List.of("Harvest potatoes in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_beetroot_basic",
                new CropHarvestTrigger(Material.BEETROOTS),
                JobSiteType.FARMLAND,
                30,
                128,
                Map.of("unlock_beetroot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 75)
                )),
                Material.BEETROOT,
                "Harvest Beetroots",
                List.of("Harvest beetroots in your farmland.")
        ));

        /*
         * =========================
         * FARMLAND – MOBS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "farmland_cow_basic",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                1,
                5,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 30)
                )),
                Material.COW_SPAWN_EGG,
                "Cull Cows",
                List.of("Cull cows within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_sheep_basic",
                new MobKillTrigger(EntityType.SHEEP),
                JobSiteType.FARMLAND,
                3,
                25,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 40)
                )),
                Material.SHEEP_SPAWN_EGG,
                "Cull Sheep",
                List.of("Cull sheep within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_pig_basic",
                new MobKillTrigger(EntityType.PIG),
                JobSiteType.FARMLAND,
                6,
                30,
                Map.of("unlock_pig", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 55)
                )),
                Material.PIG_SPAWN_EGG,
                "Cull Pigs",
                List.of("Cull pigs within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_chicken_basic",
                new MobKillTrigger(EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                10,
                40,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 70)
                )),
                Material.CHICKEN_SPAWN_EGG,
                "Cull Chickens",
                List.of("Cull chickens within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_horse_basic",
                new MobKillTrigger(EntityType.HORSE),
                JobSiteType.FARMLAND,
                18,
                10,
                Map.of("unlock_horse", 1),
                new CompositeReward(List.of(
                        new MoneyReward(3000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 120)
                )),
                Material.HORSE_SPAWN_EGG,
                "Cull Horses",
                List.of("Cull horses within your farmland.")
        ));

        /*
         * =========================
         * FARMLAND – WEEKLY
         * =========================
         */

        weekly.add(new ContractDefinition(
                "farmland_crops_weekly",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                10,
                1000,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 400)
                )),
                Material.HAY_BLOCK,
                "Master Farmer",
                List.of("Harvest massive amounts of crops.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_mobs_weekly",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                15,
                500,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 500)
                )),
                Material.DIAMOND_SWORD,
                "Livestock Control",
                List.of("Cull livestock across your farmland.")
        ));

        return new ContractPool(daily, weekly);
    }
}
