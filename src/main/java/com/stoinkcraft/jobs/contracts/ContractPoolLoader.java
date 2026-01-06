package com.stoinkcraft.jobs.contracts;

import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.JobSiteXpReward;
import com.stoinkcraft.jobs.contracts.rewards.MoneyReward;
import com.stoinkcraft.jobs.contracts.triggers.*;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.structures.BeeHiveStructure;
import com.stoinkcraft.jobs.jobsites.components.structures.MausoleumStructure;
import com.stoinkcraft.jobs.jobsites.sites.graveyard.UndeadMobType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContractPoolLoader {

    public static ContractPool load() {

        List<ContractDefinition> daily = new ArrayList<>();
        List<ContractDefinition> weekly = new ArrayList<>();

        loadFarmlandContracts(daily, weekly);
        loadQuarryContracts(daily, weekly);
        loadGraveyardContracts(daily, weekly);

        return new ContractPool(daily, weekly);
    }

    // ==================== FARMLAND CONTRACTS ====================

    private static void loadFarmlandContracts(List<ContractDefinition> daily, List<ContractDefinition> weekly) {

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
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 25)
                )),
                Material.WHEAT,
                "Harvest Wheat",
                List.of("Harvest wheat grown in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_wheat_medium",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                5,
                192,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 60)
                )),
                Material.WHEAT,
                "Wheat Harvest",
                List.of("Harvest a large amount of wheat.")
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
                "farmland_carrot_medium",
                new CropHarvestTrigger(Material.CARROTS),
                JobSiteType.FARMLAND,
                10,
                192,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1800, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 85)
                )),
                Material.CARROT,
                "Carrot Harvest",
                List.of("Harvest a large amount of carrots.")
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
                "farmland_cow_medium",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                8,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 70)
                )),
                Material.COW_SPAWN_EGG,
                "Cattle Culling",
                List.of("Cull many cows within your farmland.")
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
                "farmland_wheat_weekly",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                1,
                500,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 200)
                )),
                Material.HAY_BLOCK,
                "Wheat Master",
                List.of("Harvest massive amounts of wheat.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_carrot_weekly",
                new CropHarvestTrigger(Material.CARROTS),
                JobSiteType.FARMLAND,
                5,
                500,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 250)
                )),
                Material.GOLDEN_CARROT,
                "Carrot Master",
                List.of("Harvest massive amounts of carrots.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_potato_weekly",
                new CropHarvestTrigger(Material.POTATOES),
                JobSiteType.FARMLAND,
                20,
                500,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(7500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 300)
                )),
                Material.BAKED_POTATO,
                "Potato Master",
                List.of("Harvest massive amounts of potatoes.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_beetroot_weekly",
                new CropHarvestTrigger(Material.BEETROOTS),
                JobSiteType.FARMLAND,
                30,
                500,
                Map.of("unlock_beetroot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 400)
                )),
                Material.BEETROOT_SOUP,
                "Beetroot Master",
                List.of("Harvest massive amounts of beetroots.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_cow_weekly",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                1,
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 250)
                )),
                Material.BEEF,
                "Cattle Baron",
                List.of("Cull many cows throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_sheep_weekly",
                new MobKillTrigger(EntityType.SHEEP),
                JobSiteType.FARMLAND,
                3,
                75,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(7000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 300)
                )),
                Material.WHITE_WOOL,
                "Wool Tycoon",
                List.of("Cull many sheep throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_pig_weekly",
                new MobKillTrigger(EntityType.PIG),
                JobSiteType.FARMLAND,
                6,
                75,
                Map.of("unlock_pig", 1),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 350)
                )),
                Material.COOKED_PORKCHOP,
                "Pork Producer",
                List.of("Cull many pigs throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_chicken_weekly",
                new MobKillTrigger(EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                10,
                100,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(9000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 400)
                )),
                Material.FEATHER,
                "Poultry King",
                List.of("Cull many chickens throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_mixed_weekly",
                new MobKillTrigger(EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                15,
                200,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(15000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 600)
                )),
                Material.DIAMOND_SWORD,
                "Livestock Control",
                List.of("Cull any livestock across your farmland.")
        ));

        /*
         * =========================
         * FARMLAND – HONEY (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "farmland_honey_basic",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                BeeHiveStructure.REQUIRED_LEVEL,
                8,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 70)
                )),
                Material.HONEYCOMB,
                "Harvest Honeycomb",
                List.of("Harvest honeycomb from your beehives.")
        ));

        daily.add(new ContractDefinition(
                "farmland_honey_medium",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                BeeHiveStructure.REQUIRED_LEVEL,
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(3000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 130)
                )),
                Material.HONEYCOMB,
                "Busy Beekeeper",
                List.of("Harvest a good amount of honeycomb.")
        ));

        daily.add(new ContractDefinition(
                "farmland_honey_large",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                BeeHiveStructure.REQUIRED_LEVEL,
                40,
                Map.of("honey_speed", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5500, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 220)
                )),
                Material.HONEYCOMB_BLOCK,
                "Apiary Expert",
                List.of("Harvest large amounts of honeycomb.")
        ));

        /*
         * =========================
         * FARMLAND – HONEY (WEEKLY)
         * =========================
         */

        weekly.add(new ContractDefinition(
                "farmland_honey_weekly",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                BeeHiveStructure.REQUIRED_LEVEL,
                75,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 500)
                )),
                Material.HONEY_BLOCK,
                "Honey Magnate",
                List.of("Harvest honeycomb throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_honey_weekly_large",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                BeeHiveStructure.REQUIRED_LEVEL,
                150,
                Map.of("honey_speed", 1),
                new CompositeReward(List.of(
                        new MoneyReward(25000, 0.4),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 950)
                )),
                Material.HONEY_BLOCK,
                "Master Apiarist",
                List.of("Become the ultimate beekeeper.")
        ));
    }

    // ==================== QUARRY CONTRACTS ====================

    private static void loadQuarryContracts(List<ContractDefinition> daily, List<ContractDefinition> weekly) {

        /*
         * =========================
         * QUARRY – BASIC ORES (DAILY)
         * =========================
         */

        // Cobblestone - always available
        daily.add(new ContractDefinition(
                "quarry_cobblestone_basic",
                new BlockMineTrigger(Material.COBBLESTONE),
                JobSiteType.QUARRY,
                1,
                128,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 20)
                )),
                Material.COBBLESTONE,
                "Stone Collector",
                List.of("Mine cobblestone in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_cobblestone_medium",
                new BlockMineTrigger(Material.COBBLESTONE),
                JobSiteType.QUARRY,
                5,
                384,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
                )),
                Material.COBBLESTONE,
                "Stone Hauler",
                List.of("Mine a large amount of cobblestone.")
        ));

        // Coal - always available
        daily.add(new ContractDefinition(
                "quarry_coal_basic",
                new BlockMineTrigger(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
                JobSiteType.QUARRY,
                1,
                32,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 30)
                )),
                Material.COAL,
                "Coal Miner",
                List.of("Mine coal ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_coal_medium",
                new BlockMineTrigger(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
                JobSiteType.QUARRY,
                8,
                96,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
                )),
                Material.COAL_BLOCK,
                "Coal Collector",
                List.of("Mine a large amount of coal ore.")
        ));

        // Iron - always available
        daily.add(new ContractDefinition(
                "quarry_iron_basic",
                new BlockMineTrigger(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
                JobSiteType.QUARRY,
                1,
                24,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 40)
                )),
                Material.RAW_IRON,
                "Iron Miner",
                List.of("Mine iron ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_iron_medium",
                new BlockMineTrigger(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
                JobSiteType.QUARRY,
                10,
                64,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 100)
                )),
                Material.IRON_BLOCK,
                "Iron Collector",
                List.of("Mine a large amount of iron ore.")
        ));

        // Diamond - always available but rare in mine
        daily.add(new ContractDefinition(
                "quarry_diamond_basic",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                1,
                8,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
                )),
                Material.DIAMOND,
                "Diamond Hunter",
                List.of("Mine diamond ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_diamond_medium",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                15,
                24,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(4000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 180)
                )),
                Material.DIAMOND_BLOCK,
                "Diamond Collector",
                List.of("Mine a large amount of diamond ore.")
        ));

        /*
         * =========================
         * QUARRY – STONE VARIETIES (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_granite_basic",
                new BlockMineTrigger(Material.GRANITE),
                JobSiteType.QUARRY,
                5,
                64,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 25)
                )),
                Material.GRANITE,
                "Granite Gatherer",
                List.of("Mine granite in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_diorite_basic",
                new BlockMineTrigger(Material.DIORITE),
                JobSiteType.QUARRY,
                5,
                64,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 25)
                )),
                Material.DIORITE,
                "Diorite Gatherer",
                List.of("Mine diorite in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_andesite_basic",
                new BlockMineTrigger(Material.ANDESITE),
                JobSiteType.QUARRY,
                5,
                64,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 25)
                )),
                Material.ANDESITE,
                "Andesite Gatherer",
                List.of("Mine andesite in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_stone_mixed",
                new BlockMineTrigger(Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE),
                JobSiteType.QUARRY,
                8,
                256,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 60)
                )),
                Material.STONE,
                "Stone Mason",
                List.of("Mine any decorative stone types.")
        ));

        /*
         * =========================
         * QUARRY – COPPER (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_copper_basic",
                new BlockMineTrigger(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE),
                JobSiteType.QUARRY,
                10,
                48,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(900, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 45)
                )),
                Material.RAW_COPPER,
                "Copper Miner",
                List.of("Mine copper ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_copper_block",
                new BlockMineTrigger(Material.COPPER_BLOCK, Material.RAW_COPPER_BLOCK),
                JobSiteType.QUARRY,
                12,
                16,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 70)
                )),
                Material.COPPER_BLOCK,
                "Copper Block Hunter",
                List.of("Mine copper blocks in the quarry.")
        ));

        /*
         * =========================
         * QUARRY – PRECIOUS METALS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_gold_basic",
                new BlockMineTrigger(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                15,
                32,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 60)
                )),
                Material.RAW_GOLD,
                "Gold Miner",
                List.of("Mine gold ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_emerald_basic",
                new BlockMineTrigger(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE),
                JobSiteType.QUARRY,
                18,
                16,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 100)
                )),
                Material.EMERALD,
                "Emerald Seeker",
                List.of("Mine emerald ore in the quarry.")
        ));

        /*
         * =========================
         * QUARRY – DEEP MINERALS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_deepslate_basic",
                new BlockMineTrigger(Material.DEEPSLATE, Material.COBBLED_DEEPSLATE),
                JobSiteType.QUARRY,
                20,
                128,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 40)
                )),
                Material.DEEPSLATE,
                "Deepslate Miner",
                List.of("Mine deepslate in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_lapis_basic",
                new BlockMineTrigger(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE),
                JobSiteType.QUARRY,
                20,
                24,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1100, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 55)
                )),
                Material.LAPIS_LAZULI,
                "Lapis Hunter",
                List.of("Mine lapis ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_deep_diamond",
                new BlockMineTrigger(Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                22,
                16,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(2500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 125)
                )),
                Material.DEEPSLATE_DIAMOND_ORE,
                "Deep Diamond Hunter",
                List.of("Mine deepslate diamond ore.")
        ));

        /*
         * =========================
         * QUARRY – NETHER RESOURCES (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_netherrack_basic",
                new BlockMineTrigger(Material.NETHERRACK),
                JobSiteType.QUARRY,
                25,
                128,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 35)
                )),
                Material.NETHERRACK,
                "Nether Miner",
                List.of("Mine netherrack in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_nether_gold",
                new BlockMineTrigger(Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                25,
                48,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
                )),
                Material.NETHER_GOLD_ORE,
                "Nether Gold Hunter",
                List.of("Mine nether gold ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_quartz_basic",
                new BlockMineTrigger(Material.NETHER_QUARTZ_ORE),
                JobSiteType.QUARRY,
                25,
                48,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1300, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 65)
                )),
                Material.QUARTZ,
                "Quartz Collector",
                List.of("Mine nether quartz ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_ancient_debris",
                new BlockMineTrigger(Material.ANCIENT_DEBRIS),
                JobSiteType.QUARRY,
                28,
                8,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 250)
                )),
                Material.ANCIENT_DEBRIS,
                "Debris Hunter",
                List.of("Mine ancient debris in the quarry.")
        ));

        /*
         * =========================
         * QUARRY – GEODES (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_geode_basic",
                new GeodeMineTrigger(),
                JobSiteType.QUARRY,
                1,
                10,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
                )),
                Material.AMETHYST_CLUSTER,
                "Geode Hunter",
                List.of("Find and mine geode crystals.")
        ));

        daily.add(new ContractDefinition(
                "quarry_geode_medium",
                new GeodeMineTrigger(),
                JobSiteType.QUARRY,
                10,
                30,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2500, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 125)
                )),
                Material.AMETHYST_BLOCK,
                "Crystal Collector",
                List.of("Find and mine many geode crystals.")
        ));

        /*
         * =========================
         * QUARRY – WEEKLY
         * =========================
         */

        weekly.add(new ContractDefinition(
                "quarry_cobblestone_weekly",
                new BlockMineTrigger(Material.COBBLESTONE),
                JobSiteType.QUARRY,
                1,
                1000,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 200)
                )),
                Material.COBBLESTONE,
                "Stone Hauler",
                List.of("Mine massive amounts of cobblestone.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_coal_weekly",
                new BlockMineTrigger(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
                JobSiteType.QUARRY,
                1,
                200,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 250)
                )),
                Material.COAL_BLOCK,
                "Coal Baron",
                List.of("Mine massive amounts of coal ore.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_iron_weekly",
                new BlockMineTrigger(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
                JobSiteType.QUARRY,
                5,
                150,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 350)
                )),
                Material.IRON_BLOCK,
                "Iron Tycoon",
                List.of("Mine massive amounts of iron ore.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_diamond_weekly",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                10,
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 500)
                )),
                Material.DIAMOND_BLOCK,
                "Diamond Magnate",
                List.of("Mine massive amounts of diamond ore.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_copper_weekly",
                new BlockMineTrigger(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.COPPER_BLOCK, Material.RAW_COPPER_BLOCK),
                JobSiteType.QUARRY,
                10,
                200,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(7000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 300)
                )),
                Material.COPPER_BLOCK,
                "Copper Kingpin",
                List.of("Mine massive amounts of copper.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_gold_weekly",
                new BlockMineTrigger(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                15,
                100,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 400)
                )),
                Material.GOLD_BLOCK,
                "Gold Tycoon",
                List.of("Mine massive amounts of gold ore.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_deepslate_weekly",
                new BlockMineTrigger(
                        Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
                        Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE,
                        Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE,
                        Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_LAPIS_ORE,
                        Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE
                ),
                JobSiteType.QUARRY,
                20,
                500,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(9000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 400)
                )),
                Material.DEEPSLATE,
                "Deepslate Master",
                List.of("Mine massive amounts of deepslate materials.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_nether_weekly",
                new BlockMineTrigger(Material.NETHERRACK, Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS),
                JobSiteType.QUARRY,
                25,
                400,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 500)
                )),
                Material.NETHERRACK,
                "Nether Excavator",
                List.of("Mine massive amounts of nether materials.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_geode_weekly",
                new GeodeMineTrigger(),
                JobSiteType.QUARRY,
                5,
                100,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 350)
                )),
                Material.AMETHYST_BLOCK,
                "Crystal Master",
                List.of("Find and mine many geode crystals throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_mixed_ores_weekly",
                new BlockMineTrigger(
                        Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE,
                        Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE,
                        Material.LAPIS_ORE, Material.REDSTONE_ORE,
                        Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE,
                        Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE,
                        Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE,
                        Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE
                ),
                JobSiteType.QUARRY,
                15,
                300,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(15000, 0.4),
                        new JobSiteXpReward(JobSiteType.QUARRY, 600)
                )),
                Material.DIAMOND_PICKAXE,
                "Master Miner",
                List.of("Mine any ore type in the quarry.")
        ));
    }

    private static void loadGraveyardContracts(List<ContractDefinition> daily, List<ContractDefinition> weekly) {

        /*
         * =========================
         * GRAVEYARD – BASIC UNDEAD (DAILY)
         * =========================
         */

        // Zombie contracts
        daily.add(new ContractDefinition(
                "graveyard_zombie_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE),
                JobSiteType.GRAVEYARD,
                1,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 30)
                )),
                Material.ZOMBIE_HEAD,
                "Slay Zombies",
                List.of("Slay zombies in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_zombie_medium",
                new UndeadKillTrigger(EntityType.ZOMBIE),
                JobSiteType.GRAVEYARD,
                8,
                40,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 70)
                )),
                Material.ZOMBIE_HEAD,
                "Zombie Slayer",
                List.of("Slay many zombies in your graveyard.")
        ));

        // Skeleton contracts
        daily.add(new ContractDefinition(
                "graveyard_skeleton_basic",
                new UndeadKillTrigger(EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 30)
                )),
                Material.SKELETON_SKULL,
                "Slay Skeletons",
                List.of("Slay skeletons in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_skeleton_medium",
                new UndeadKillTrigger(EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                8,
                40,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 70)
                )),
                Material.SKELETON_SKULL,
                "Skeleton Slayer",
                List.of("Slay many skeletons in your graveyard.")
        ));

        // Mixed undead (basic)
        daily.add(new ContractDefinition(
                "graveyard_undead_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                25,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 40)
                )),
                Material.BONE,
                "Undead Purge",
                List.of("Slay any undead in your graveyard.")
        ));

        /*
         * =========================
         * GRAVEYARD – TIER 2 MOBS (DAILY)
         * =========================
         */

        // Husk contracts (requires attunement)
        daily.add(new ContractDefinition(
                "graveyard_husk_basic",
                new UndeadKillTrigger(EntityType.HUSK),
                JobSiteType.GRAVEYARD,
                UndeadMobType.HUSK.getRequiredLevel(),
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 55)
                )),
                Material.SAND,
                "Desert Wanderers",
                List.of("Slay husks in your graveyard.", "Requires Husk attunement.")
        ));

        // Stray contracts
        daily.add(new ContractDefinition(
                "graveyard_stray_basic",
                new UndeadKillTrigger(EntityType.STRAY),
                JobSiteType.GRAVEYARD,
                UndeadMobType.STRAY.getRequiredLevel(),
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 55)
                )),
                Material.POWDER_SNOW_BUCKET,
                "Frozen Archers",
                List.of("Slay strays in your graveyard.", "Requires Stray attunement.")
        ));

        /*
         * =========================
         * GRAVEYARD – TIER 3 MOBS (DAILY)
         * =========================
         */

        // Zombie Villager contracts
        daily.add(new ContractDefinition(
                "graveyard_zombie_villager_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE_VILLAGER),
                JobSiteType.GRAVEYARD,
                UndeadMobType.ZOMBIE_VILLAGER.getRequiredLevel(),
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 90)
                )),
                Material.EMERALD,
                "Fallen Merchants",
                List.of("Slay zombie villagers in your graveyard.", "Requires Zombie Villager attunement.")
        ));

        // Drowned contracts
        daily.add(new ContractDefinition(
                "graveyard_drowned_basic",
                new UndeadKillTrigger(EntityType.DROWNED),
                JobSiteType.GRAVEYARD,
                UndeadMobType.DROWNED.getRequiredLevel(),
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 90)
                )),
                Material.TRIDENT,
                "Sunken Dead",
                List.of("Slay drowned in your graveyard.", "Requires Drowned attunement.")
        ));

        /*
         * =========================
         * GRAVEYARD – TIER 4 MOBS (DAILY)
         * =========================
         */

        // Wither Skeleton contracts
        daily.add(new ContractDefinition(
                "graveyard_wither_skeleton_basic",
                new UndeadKillTrigger(EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                10,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(3500, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 150)
                )),
                Material.WITHER_SKELETON_SKULL,
                "Wither's Servants",
                List.of("Slay wither skeletons in your graveyard.", "Requires Wither Skeleton attunement.")
        ));

        // Phantom contracts
//        daily.add(new ContractDefinition(
//                "graveyard_phantom_basic",
//                new UndeadKillTrigger(EntityType.PHANTOM),
//                JobSiteType.GRAVEYARD,
//                UndeadMobType.PHANTOM.getRequiredLevel(),
//                10,
//                Map.of(),
//                new CompositeReward(List.of(
//                        new MoneyReward(3500, 0.4),
//                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 150)
//                )),
//                Material.PHANTOM_MEMBRANE,
//                "Night Terrors",
//                List.of("Slay phantoms in your graveyard.", "Requires Phantom attunement.")
//        ));

        /*
         * =========================
         * GRAVEYARD – MAUSOLEUM SPIDERS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_spider_basic",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                MausoleumStructure.REQUIRED_LEVEL,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 50)
                )),
                Material.SPIDER_EYE,
                "Crypt Crawlers",
                List.of("Slay spiders from the Mausoleum.", "Requires Mausoleum.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_spider_medium",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                MausoleumStructure.REQUIRED_LEVEL + 5,
                35,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2500, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 110)
                )),
                Material.SPIDER_EYE,
                "Arachnid Exterminator",
                List.of("Slay many spiders from the Mausoleum.")
        ));

        /*
         * =========================
         * GRAVEYARD – SOULS (DAILY)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_souls_basic",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                5,
                10,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 65)
                )),
                Material.SOUL_LANTERN,
                "Soul Collector",
                List.of("Collect souls from slain undead.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_souls_medium",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                15,
                25,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(3000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 130)
                )),
                Material.SOUL_LANTERN,
                "Soul Harvester",
                List.of("Collect many souls from slain undead.")
        ));

        /*
         * =========================
         * GRAVEYARD – WEEKLY CONTRACTS
         * =========================
         */

        weekly.add(new ContractDefinition(
                "graveyard_undead_weekly",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                150,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 250)
                )),
                Material.BONE_BLOCK,
                "Graveyard Keeper",
                List.of("Slay many undead throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_zombie_weekly",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED),
                JobSiteType.GRAVEYARD,
                UndeadMobType.HUSK.getRequiredLevel(),
                200,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 400)
                )),
                Material.ZOMBIE_HEAD,
                "Zombie Exterminator",
                List.of("Slay all types of zombies throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_skeleton_weekly",
                new UndeadKillTrigger(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                UndeadMobType.STRAY.getRequiredLevel(),
                200,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 400)
                )),
                Material.SKELETON_SKULL,
                "Skeleton Exterminator",
                List.of("Slay all types of skeletons throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_spider_weekly",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                MausoleumStructure.REQUIRED_LEVEL,
                100,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 350)
                )),
                Material.COBWEB,
                "Web Clearer",
                List.of("Clear many spider hordes throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_souls_weekly",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                5,
                75,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 500)
                )),
                Material.SOUL_TORCH,
                "Soul Reaper",
                List.of("Collect many souls throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_elite_weekly",
                new UndeadKillTrigger(EntityType.WITHER_SKELETON, EntityType.PHANTOM),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(20000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 800)
                )),
                Material.NETHER_STAR,
                "Elite Hunter",
                List.of("Slay the most dangerous undead throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_mixed_weekly",
                new UndeadKillTrigger(
                        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.STRAY,
                        EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED, EntityType.WITHER_SKELETON, EntityType.PHANTOM
                ),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                300,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(25000, 0.4),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 1000)
                )),
                Material.DIAMOND_SWORD,
                "Master of the Dead",
                List.of("Prove your mastery over all undead.")
        ));
    }
}
