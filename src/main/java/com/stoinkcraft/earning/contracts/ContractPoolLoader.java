package com.stoinkcraft.earning.contracts;

import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.earning.contracts.rewards.CompositeReward;
import com.stoinkcraft.earning.contracts.rewards.JobSiteXpReward;
import com.stoinkcraft.earning.contracts.rewards.MoneyReward;
import com.stoinkcraft.earning.contracts.triggers.*;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.sites.graveyard.UndeadMobType;
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
         * DAILY CONTRACTS - CROPS
         * =========================
         * Design:
         * - Basic contracts completable in ~10-15 minutes of active farming
         * - Medium contracts take ~25-30 minutes
         * - Rewards scale with difficulty and level requirement
         */

        // --- WHEAT (Always Available) ---
        daily.add(new ContractDefinition(
                "farmland_wheat_basic",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                1,
                48,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(300, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 40)
                )),
                Material.WHEAT,
                "Harvest Wheat",
                List.of("Harvest wheat grown in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_wheat_medium",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                6,
                128,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(750, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 100)
                )),
                Material.WHEAT,
                "Wheat Harvest",
                List.of("Harvest a large amount of wheat.")
        ));

        // --- CARROTS (Requires unlock_carrot @ Level 5) ---
        daily.add(new ContractDefinition(
                "farmland_carrot_basic",
                new CropHarvestTrigger(Material.CARROTS),
                JobSiteType.FARMLAND,
                5,
                48,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 50)
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
                128,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 130)
                )),
                Material.CARROT,
                "Carrot Harvest",
                List.of("Harvest a large amount of carrots.")
        ));

        // --- POTATOES (Requires unlock_potato @ Level 12) ---
        daily.add(new ContractDefinition(
                "farmland_potato_basic",
                new CropHarvestTrigger(Material.POTATOES),
                JobSiteType.FARMLAND,
                12,
                64,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 80)
                )),
                Material.POTATO,
                "Harvest Potatoes",
                List.of("Harvest potatoes in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_potato_medium",
                new CropHarvestTrigger(Material.POTATOES),
                JobSiteType.FARMLAND,
                18,
                160,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 180)
                )),
                Material.POTATO,
                "Potato Harvest",
                List.of("Harvest a large amount of potatoes.")
        ));

        // --- BEETROOTS (Requires unlock_beetroot @ Level 22) ---
        daily.add(new ContractDefinition(
                "farmland_beetroot_basic",
                new CropHarvestTrigger(Material.BEETROOTS),
                JobSiteType.FARMLAND,
                22,
                64,
                Map.of("unlock_beetroot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(900, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 120)
                )),
                Material.BEETROOT,
                "Harvest Beetroots",
                List.of("Harvest beetroots in your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_beetroot_medium",
                new CropHarvestTrigger(Material.BEETROOTS),
                JobSiteType.FARMLAND,
                28,
                160,
                Map.of("unlock_beetroot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 260)
                )),
                Material.BEETROOT,
                "Beetroot Harvest",
                List.of("Harvest a large amount of beetroots.")
        ));

        /*
         * =========================
         * DAILY CONTRACTS - ANIMALS
         * =========================
         * Design:
         * - Animals are slower to spawn, so targets are lower
         * - Higher value per kill but takes more active time
         */

        // --- COWS (Always available once barn is built) ---
        daily.add(new ContractDefinition(
                "farmland_cow_basic",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                0,  // Barn level
                8,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 50)
                )),
                Material.BEEF,
                "Cull Cows",
                List.of("Cull cows within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_cow_medium",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                5,
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 120)
                )),
                Material.COOKED_BEEF,
                "Cattle Culling",
                List.of("Cull many cows within your farmland.")
        ));

        // --- SHEEP (Requires unlock_sheep @ Level 10) ---
        daily.add(new ContractDefinition(
                "farmland_sheep_basic",
                new MobKillTrigger(EntityType.SHEEP),
                JobSiteType.FARMLAND,
                0,
                10,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 60)
                )),
                Material.MUTTON,
                "Cull Sheep",
                List.of("Cull sheep within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_sheep_medium",
                new MobKillTrigger(EntityType.SHEEP),
                JobSiteType.FARMLAND,
                0,
                25,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 140)
                )),
                Material.WHITE_WOOL,
                "Wool Collection",
                List.of("Cull many sheep within your farmland.")
        ));

        // --- PIGS (Requires unlock_pig @ Level 14) ---
        daily.add(new ContractDefinition(
                "farmland_pig_basic",
                new MobKillTrigger(EntityType.PIG),
                JobSiteType.FARMLAND,
                0,
                10,
                Map.of("unlock_pig", 1),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 75)
                )),
                Material.PORKCHOP,
                "Cull Pigs",
                List.of("Cull pigs within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_pig_medium",
                new MobKillTrigger(EntityType.PIG),
                JobSiteType.FARMLAND,
                0,
                25,
                Map.of("unlock_pig", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 170)
                )),
                Material.COOKED_PORKCHOP,
                "Pork Production",
                List.of("Cull many pigs within your farmland.")
        ));

        // --- CHICKENS (Requires unlock_chicken @ Level 18) ---
        daily.add(new ContractDefinition(
                "farmland_chicken_basic",
                new MobKillTrigger(EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                0,
                15,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 90)
                )),
                Material.CHICKEN,
                "Cull Chickens",
                List.of("Cull chickens within your farmland.")
        ));

        daily.add(new ContractDefinition(
                "farmland_chicken_medium",
                new MobKillTrigger(EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                0,
                35,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1600, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 200)
                )),
                Material.FEATHER,
                "Poultry Processing",
                List.of("Cull many chickens within your farmland.")
        ));

        // --- HORSES (Requires unlock_horse @ Level 24) ---
//        daily.add(new ContractDefinition(
//                "farmland_horse_basic",
//                new MobKillTrigger(EntityType.HORSE),
//                JobSiteType.FARMLAND,
//                24,
//                6,
//                Map.of("unlock_horse", 1),
//                new CompositeReward(List.of(
//                        new MoneyReward(1500, 0.35),
//                        new JobSiteXpReward(JobSiteType.FARMLAND, 180)
//                )),
//                Material.LEATHER,
//                "Cull Horses",
//                List.of("Cull horses within your farmland.")
//        ));

        /*
         * =========================
         * DAILY CONTRACTS - HONEY
         * =========================
         */

        daily.add(new ContractDefinition(
                "farmland_honey_basic",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                ConfigLoader.getStructures().getBeehiveRequiredLevel(),
                6,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 100)
                )),
                Material.HONEYCOMB,
                "Harvest Honeycomb",
                List.of("Harvest honeycomb from your beehives.")
        ));

        daily.add(new ContractDefinition(
                "farmland_honey_medium",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                24,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1800, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 220)
                )),
                Material.HONEYCOMB,
                "Busy Beekeeper",
                List.of("Harvest a good amount of honeycomb.")
        ));

        daily.add(new ContractDefinition(
                "farmland_honey_large",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                28,
                28,
                Map.of("honey_speed", 3),
                new CompositeReward(List.of(
                        new MoneyReward(3500, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 400)
                )),
                Material.HONEYCOMB_BLOCK,
                "Apiary Expert",
                List.of("Harvest large amounts of honeycomb.", "Requires Honey Speed level 3+")
        ));

        /*
         * =========================
         * WEEKLY CONTRACTS - CROPS
         * =========================
         * Design:
         * - ~5-7x daily targets
         * - Rewards are ~4x daily (not 7x - incentivizes daily play too)
         */

        weekly.add(new ContractDefinition(
                "farmland_wheat_weekly",
                new CropHarvestTrigger(Material.WHEAT),
                JobSiteType.FARMLAND,
                1,
                350,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2500, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 300)
                )),
                Material.HAY_BLOCK,
                "Wheat Master",
                List.of("Harvest massive amounts of wheat this week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_carrot_weekly",
                new CropHarvestTrigger(Material.CARROTS),
                JobSiteType.FARMLAND,
                5,
                350,
                Map.of("unlock_carrot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(3500, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 400)
                )),
                Material.GOLDEN_CARROT,
                "Carrot Master",
                List.of("Harvest massive amounts of carrots this week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_potato_weekly",
                new CropHarvestTrigger(Material.POTATOES),
                JobSiteType.FARMLAND,
                12,
                400,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 550)
                )),
                Material.BAKED_POTATO,
                "Potato Master",
                List.of("Harvest massive amounts of potatoes this week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_beetroot_weekly",
                new CropHarvestTrigger(Material.BEETROOTS),
                JobSiteType.FARMLAND,
                22,
                400,
                Map.of("unlock_beetroot", 1),
                new CompositeReward(List.of(
                        new MoneyReward(7000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 750)
                )),
                Material.BEETROOT_SOUP,
                "Beetroot Master",
                List.of("Harvest massive amounts of beetroots this week.")
        ));

        // Mixed crop weekly
        weekly.add(new ContractDefinition(
                "farmland_crops_mixed_weekly",
                new CropHarvestTrigger(List.of(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS)),
                JobSiteType.FARMLAND,
                15,
                600,
                Map.of("unlock_potato", 1),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 650)
                )),
                Material.COMPOSTER,
                "Harvest Festival",
                List.of("Harvest any crops in large quantities.")
        ));

        /*
         * =========================
         * WEEKLY CONTRACTS - ANIMALS
         * =========================
         */

        weekly.add(new ContractDefinition(
                "farmland_cow_weekly",
                new MobKillTrigger(EntityType.COW),
                JobSiteType.FARMLAND,
                10,
                40,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(3000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 350)
                )),
                Material.LEATHER,
                "Cattle Baron",
                List.of("Cull many cows throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_sheep_weekly",
                new MobKillTrigger(EntityType.SHEEP),
                JobSiteType.FARMLAND,
                10,
                50,
                Map.of("unlock_sheep", 1),
                new CompositeReward(List.of(
                        new MoneyReward(4000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 450)
                )),
                Material.WHITE_WOOL,
                "Wool Tycoon",
                List.of("Cull many sheep throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_pig_weekly",
                new MobKillTrigger(EntityType.PIG),
                JobSiteType.FARMLAND,
                14,
                50,
                Map.of("unlock_pig", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 550)
                )),
                Material.COOKED_PORKCHOP,
                "Pork Producer",
                List.of("Cull many pigs throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_chicken_weekly",
                new MobKillTrigger(EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                18,
                70,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5500, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 600)
                )),
                Material.FEATHER,
                "Poultry King",
                List.of("Cull many chickens throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_mixed_animals_weekly",
                new MobKillTrigger(EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN),
                JobSiteType.FARMLAND,
                18,
                100,
                Map.of("unlock_chicken", 1),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 850)
                )),
                Material.DIAMOND_SWORD,
                "Livestock Control",
                List.of("Cull any livestock across your farmland.")
        ));

        /*
         * =========================
         * WEEKLY CONTRACTS - HONEY
         * =========================
         */

        weekly.add(new ContractDefinition(
                "farmland_honey_weekly",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                ConfigLoader.getStructures().getBeehiveRequiredLevel(),
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 650)
                )),
                Material.HONEY_BLOCK,
                "Honey Magnate",
                List.of("Harvest honeycomb throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "farmland_honey_weekly_large",
                new HoneyHarvestTrigger(),
                JobSiteType.FARMLAND,
                26,
                100,
                Map.of("honey_speed", 5),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.35),
                        new JobSiteXpReward(JobSiteType.FARMLAND, 1200)
                )),
                Material.HONEY_BLOCK,
                "Master Apiarist",
                List.of("Become the ultimate beekeeper.", "Requires Honey Speed level 5+")
        ));
    }

    // ==================== QUARRY CONTRACTS ====================

    private static void loadQuarryContracts(List<ContractDefinition> daily, List<ContractDefinition> weekly) {

        /*
         * =========================
         * QUARRY CONTRACT DESIGN
         * =========================
         * - Contracts are completable within 1-2 regen cycles
         * - Basic contracts: ~50% of a single regen
         * - Medium contracts: ~100% of a single regen
         * - Weekly: ~3-4 regen cycles worth
         *
         * Mine volume estimate: ~21x21x22 = ~9,700 blocks per regen
         * With ore distribution, expect per regen (Mining Basics):
         * - Cobblestone: ~5,300
         * - Coal: ~1,750
         * - Iron: ~970
         * - Diamond: ~194
         */

        /*
         * =========================
         * DAILY - MINING BASICS (Always Available)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_cobblestone_basic",
                new BlockMineTrigger(Material.COBBLESTONE, Material.STONE),
                JobSiteType.QUARRY,
                1,
                200,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(350, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 45)
                )),
                Material.COBBLESTONE,
                "Stone Collector",
                List.of("Mine cobblestone in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_cobblestone_medium",
                new BlockMineTrigger(Material.COBBLESTONE),
                JobSiteType.QUARRY,
                6,
                500,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 100)
                )),
                Material.COBBLESTONE,
                "Stone Hauler",
                List.of("Mine a large amount of cobblestone.")
        ));

        daily.add(new ContractDefinition(
                "quarry_coal_basic",
                new BlockMineTrigger(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
                JobSiteType.QUARRY,
                1,
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
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
                120,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(900, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 110)
                )),
                Material.COAL_BLOCK,
                "Coal Collector",
                List.of("Mine a large amount of coal ore.")
        ));

        daily.add(new ContractDefinition(
                "quarry_iron_basic",
                new BlockMineTrigger(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
                JobSiteType.QUARRY,
                1,
                30,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 60)
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
                80,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 140)
                )),
                Material.IRON_BLOCK,
                "Iron Collector",
                List.of("Mine a large amount of iron ore.")
        ));

        daily.add(new ContractDefinition(
                "quarry_diamond_basic",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                1,
                10,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 100)
                )),
                Material.DIAMOND,
                "Diamond Hunter",
                List.of("Mine diamond ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_diamond_medium",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                12,
                30,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 240)
                )),
                Material.DIAMOND_BLOCK,
                "Diamond Collector",
                List.of("Mine a large amount of diamond ore.")
        ));

        /*
         * =========================
         * DAILY - STONE VARIETIES (Level 5)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_granite_basic",
                new BlockMineTrigger(Material.GRANITE),
                JobSiteType.QUARRY,
                5,
                60,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
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
                60,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
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
                60,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 50)
                )),
                Material.ANDESITE,
                "Andesite Gatherer",
                List.of("Mine andesite in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_stone_mixed",
                new BlockMineTrigger(Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE),
                JobSiteType.QUARRY,
                7,
                200,
                Map.of("unlock_stone_varieties", 1),
                new CompositeReward(List.of(
                        new MoneyReward(900, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 110)
                )),
                Material.STONE,
                "Stone Mason",
                List.of("Mine any decorative stone types.")
        ));

        /*
         * =========================
         * DAILY - COPPER COLLECTION (Level 10)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_copper_basic",
                new BlockMineTrigger(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE),
                JobSiteType.QUARRY,
                10,
                50,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
                )),
                Material.RAW_COPPER,
                "Copper Miner",
                List.of("Mine copper ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_copper_medium",
                new BlockMineTrigger(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER_BLOCK),
                JobSiteType.QUARRY,
                14,
                120,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 170)
                )),
                Material.COPPER_BLOCK,
                "Copper Collector",
                List.of("Mine large amounts of copper.")
        ));

        /*
         * =========================
         * DAILY - PRECIOUS METALS (Level 15)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_gold_basic",
                new BlockMineTrigger(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                15,
                40,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 100)
                )),
                Material.RAW_GOLD,
                "Gold Miner",
                List.of("Mine gold ore in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_gold_medium",
                new BlockMineTrigger(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                18,
                100,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1800, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 210)
                )),
                Material.GOLD_BLOCK,
                "Gold Collector",
                List.of("Mine large amounts of gold ore.")
        ));

        daily.add(new ContractDefinition(
                "quarry_emerald_basic",
                new BlockMineTrigger(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE),
                JobSiteType.QUARRY,
                15,
                20,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 120)
                )),
                Material.EMERALD,
                "Emerald Seeker",
                List.of("Mine emerald ore in the quarry.")
        ));

        /*
         * =========================
         * DAILY - DEEP MINERALS (Level 20)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_deepslate_basic",
                new BlockMineTrigger(Material.DEEPSLATE, Material.COBBLED_DEEPSLATE),
                JobSiteType.QUARRY,
                20,
                150,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
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
                25,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(750, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 90)
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
                25,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 180)
                )),
                Material.DEEPSLATE_DIAMOND_ORE,
                "Deep Diamond Hunter",
                List.of("Mine deepslate diamond ore.")
        ));

        daily.add(new ContractDefinition(
                "quarry_redstone_basic",
                new BlockMineTrigger(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE),
                JobSiteType.QUARRY,
                20,
                20,
                Map.of("unlock_deep_minerals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 85)
                )),
                Material.REDSTONE,
                "Redstone Miner",
                List.of("Mine redstone ore in the quarry.")
        ));

        /*
         * =========================
         * DAILY - NETHER RESOURCES (Level 26)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_netherrack_basic",
                new BlockMineTrigger(Material.NETHERRACK, Material.BLACKSTONE),
                JobSiteType.QUARRY,
                26,
                150,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 85)
                )),
                Material.NETHERRACK,
                "Nether Miner",
                List.of("Mine nether blocks in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_nether_gold",
                new BlockMineTrigger(Material.NETHER_GOLD_ORE, Material.GILDED_BLACKSTONE),
                JobSiteType.QUARRY,
                26,
                50,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1200, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 140)
                )),
                Material.NETHER_GOLD_ORE,
                "Nether Gold Hunter",
                List.of("Mine nether gold in the quarry.")
        ));

        daily.add(new ContractDefinition(
                "quarry_quartz_basic",
                new BlockMineTrigger(Material.NETHER_QUARTZ_ORE),
                JobSiteType.QUARRY,
                26,
                50,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(1000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 120)
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
                12,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(3000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 350)
                )),
                Material.ANCIENT_DEBRIS,
                "Debris Hunter",
                List.of("Mine ancient debris in the quarry.")
        ));

        /*
         * =========================
         * DAILY - GEODES (Always Available)
         * =========================
         */

        daily.add(new ContractDefinition(
                "quarry_geode_basic",
                new GeodeMineTrigger(),
                JobSiteType.QUARRY,
                1,
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 75)
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
                40,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 180)
                )),
                Material.AMETHYST_BLOCK,
                "Crystal Collector",
                List.of("Find and mine many geode crystals.")
        ));

        /*
         * =========================
         * WEEKLY CONTRACTS
         * =========================
         */

        weekly.add(new ContractDefinition(
                "quarry_cobblestone_weekly",
                new BlockMineTrigger(Material.COBBLESTONE),
                JobSiteType.QUARRY,
                1,
                1500,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 300)
                )),
                Material.COBBLESTONE,
                "Stone Hauler",
                List.of("Mine massive amounts of cobblestone this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_coal_weekly",
                new BlockMineTrigger(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
                JobSiteType.QUARRY,
                1,
                300,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(3500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 400)
                )),
                Material.COAL_BLOCK,
                "Coal Baron",
                List.of("Mine massive amounts of coal ore this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_iron_weekly",
                new BlockMineTrigger(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
                JobSiteType.QUARRY,
                5,
                200,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(5000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 550)
                )),
                Material.IRON_BLOCK,
                "Iron Tycoon",
                List.of("Mine massive amounts of iron ore this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_diamond_weekly",
                new BlockMineTrigger(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                JobSiteType.QUARRY,
                8,
                75,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(7000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 750)
                )),
                Material.DIAMOND_BLOCK,
                "Diamond Magnate",
                List.of("Mine massive amounts of diamond ore this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_copper_weekly",
                new BlockMineTrigger(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK),
                JobSiteType.QUARRY,
                10,
                300,
                Map.of("unlock_copper_collection", 1),
                new CompositeReward(List.of(
                        new MoneyReward(5500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 600)
                )),
                Material.COPPER_BLOCK,
                "Copper Kingpin",
                List.of("Mine massive amounts of copper this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_gold_weekly",
                new BlockMineTrigger(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
                JobSiteType.QUARRY,
                15,
                150,
                Map.of("unlock_precious_metals", 1),
                new CompositeReward(List.of(
                        new MoneyReward(7500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 800)
                )),
                Material.GOLD_BLOCK,
                "Gold Tycoon",
                List.of("Mine massive amounts of gold ore this week.")
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
                        new MoneyReward(6500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 700)
                )),
                Material.DEEPSLATE,
                "Deepslate Master",
                List.of("Mine massive amounts of deepslate materials this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_nether_weekly",
                new BlockMineTrigger(Material.NETHERRACK, Material.BLACKSTONE, Material.NETHER_GOLD_ORE,
                        Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS, Material.GILDED_BLACKSTONE),
                JobSiteType.QUARRY,
                26,
                400,
                Map.of("unlock_nether_resources", 1),
                new CompositeReward(List.of(
                        new MoneyReward(9000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 950)
                )),
                Material.NETHERRACK,
                "Nether Excavator",
                List.of("Mine massive amounts of nether materials this week.")
        ));

        weekly.add(new ContractDefinition(
                "quarry_geode_weekly",
                new GeodeMineTrigger(),
                JobSiteType.QUARRY,
                5,
                120,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(5500, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 600)
                )),
                Material.AMETHYST_BLOCK,
                "Crystal Master",
                List.of("Find and mine many geode crystals this week.")
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
                400,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(10000, 0.35),
                        new JobSiteXpReward(JobSiteType.QUARRY, 1100)
                )),
                Material.DIAMOND_PICKAXE,
                "Master Miner",
                List.of("Mine any ore type in the quarry this week.")
        ));
    }

    private static void loadGraveyardContracts(List<ContractDefinition> daily, List<ContractDefinition> weekly) {

        /*
         * =========================
         * GRAVEYARD CONTRACT DESIGN
         * =========================
         * - Night-only activity means ~10 minutes of Minecraft night
         * - With 4 starting tombstones at 30s spawn = ~20 mobs per night
         * - With 15 tombstones at 15s spawn = ~100+ mobs per night
         * - Contracts should scale from achievable in 1-2 nights to 4-5 nights
         */

        /*
         * =========================
         * DAILY - BASIC UNDEAD (Always Available)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_zombie_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE),
                JobSiteType.GRAVEYARD,
                1,
                12,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 50)
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
                30,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(950, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 115)
                )),
                Material.ZOMBIE_HEAD,
                "Zombie Slayer",
                List.of("Slay many zombies in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_skeleton_basic",
                new UndeadKillTrigger(EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                12,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(400, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 50)
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
                30,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(950, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 115)
                )),
                Material.SKELETON_SKULL,
                "Skeleton Slayer",
                List.of("Slay many skeletons in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_undead_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(550, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 70)
                )),
                Material.BONE,
                "Undead Purge",
                List.of("Slay any basic undead in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_undead_medium",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                10,
                50,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1300, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 160)
                )),
                Material.BONE_BLOCK,
                "Graveyard Cleansing",
                List.of("Slay many undead in your graveyard.")
        ));

        /*
         * =========================
         * DAILY - TIER 2 MOBS (Level 8)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_husk_basic",
                new UndeadKillTrigger(EntityType.HUSK),
                JobSiteType.GRAVEYARD,
                UndeadMobType.HUSK.getRequiredLevel(),
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 85)
                )),
                Material.SAND,
                "Desert Wanderers",
                List.of("Slay husks in your graveyard.", "Requires Husk attunement.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_husk_medium",
                new UndeadKillTrigger(EntityType.HUSK),
                JobSiteType.GRAVEYARD,
                12,
                35,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 180)
                )),
                Material.SANDSTONE,
                "Husk Hunter",
                List.of("Slay many husks in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_stray_basic",
                new UndeadKillTrigger(EntityType.STRAY),
                JobSiteType.GRAVEYARD,
                UndeadMobType.STRAY.getRequiredLevel(),
                15,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(700, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 85)
                )),
                Material.POWDER_SNOW_BUCKET,
                "Frozen Archers",
                List.of("Slay strays in your graveyard.", "Requires Stray attunement.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_stray_medium",
                new UndeadKillTrigger(EntityType.STRAY),
                JobSiteType.GRAVEYARD,
                12,
                35,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 180)
                )),
                Material.PACKED_ICE,
                "Stray Slayer",
                List.of("Slay many strays in your graveyard.")
        ));

        /*
         * =========================
         * DAILY - TIER 3 MOBS (Level 16)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_zombie_villager_basic",
                new UndeadKillTrigger(EntityType.ZOMBIE_VILLAGER),
                JobSiteType.GRAVEYARD,
                UndeadMobType.ZOMBIE_VILLAGER.getRequiredLevel(),
                12,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1100, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 130)
                )),
                Material.EMERALD,
                "Fallen Merchants",
                List.of("Slay zombie villagers in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_zombie_villager_medium",
                new UndeadKillTrigger(EntityType.ZOMBIE_VILLAGER),
                JobSiteType.GRAVEYARD,
                20,
                28,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2400, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 280)
                )),
                Material.EMERALD_BLOCK,
                "Villager Purge",
                List.of("Slay many zombie villagers in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_drowned_basic",
                new UndeadKillTrigger(EntityType.DROWNED),
                JobSiteType.GRAVEYARD,
                UndeadMobType.DROWNED.getRequiredLevel(),
                12,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1100, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 130)
                )),
                Material.TRIDENT,
                "Sunken Dead",
                List.of("Slay drowned in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_drowned_medium",
                new UndeadKillTrigger(EntityType.DROWNED),
                JobSiteType.GRAVEYARD,
                20,
                28,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2400, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 280)
                )),
                Material.HEART_OF_THE_SEA,
                "Drowned Hunter",
                List.of("Slay many drowned in your graveyard.")
        ));

        /*
         * =========================
         * DAILY - TIER 4 MOBS (Level 24)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_wither_skeleton_basic",
                new UndeadKillTrigger(EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                8,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(2000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 240)
                )),
                Material.WITHER_SKELETON_SKULL,
                "Wither's Servants",
                List.of("Slay wither skeletons in your graveyard.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_wither_skeleton_medium",
                new UndeadKillTrigger(EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                28,
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(4500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 520)
                )),
                Material.NETHER_STAR,
                "Wither Slayer",
                List.of("Slay many wither skeletons in your graveyard.")
        ));

        /*
         * =========================
         * DAILY - MAUSOLEUM SPIDERS (Level 15)
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_spider_basic",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                ConfigLoader.getStructures().getMausoleumRequiredLevel(),
                10,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(600, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 75)
                )),
                Material.SPIDER_EYE,
                "Crypt Crawlers",
                List.of("Slay spiders from the Mausoleum.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_spider_medium",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                18,
                25,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1400, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 170)
                )),
                Material.FERMENTED_SPIDER_EYE,
                "Arachnid Exterminator",
                List.of("Slay many spiders from the Mausoleum.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_spider_large",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                24,
                50,
                Map.of("mausoleum_horde_size", 3),
                new CompositeReward(List.of(
                        new MoneyReward(2800, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 330)
                )),
                Material.COBWEB,
                "Horde Clearer",
                List.of("Slay massive amounts of spiders.", "Requires Horde Size level 3+")
        ));

        /*
         * =========================
         * DAILY - SOULS
         * =========================
         */

        daily.add(new ContractDefinition(
                "graveyard_souls_basic",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                5,
                8,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(800, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 100)
                )),
                Material.SOUL_LANTERN,
                "Soul Collector",
                List.of("Collect souls from slain undead.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_souls_medium",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                12,
                20,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(1800, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 220)
                )),
                Material.SOUL_CAMPFIRE,
                "Soul Harvester",
                List.of("Collect many souls from slain undead.")
        ));

        daily.add(new ContractDefinition(
                "graveyard_souls_large",
                new SoulCollectTrigger(),
                JobSiteType.GRAVEYARD,
                20,
                40,
                Map.of("soul_harvest", 3),
                new CompositeReward(List.of(
                        new MoneyReward(3500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 420)
                )),
                Material.SOUL_TORCH,
                "Soul Reaper",
                List.of("Collect massive amounts of souls.", "Requires Soul Harvest level 3+")
        ));

        /*
         * =========================
         * WEEKLY CONTRACTS
         * =========================
         */

        weekly.add(new ContractDefinition(
                "graveyard_undead_weekly",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.SKELETON),
                JobSiteType.GRAVEYARD,
                1,
                120,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(4000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 450)
                )),
                Material.BONE_BLOCK,
                "Graveyard Keeper",
                List.of("Slay many undead throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_tier2_weekly",
                new UndeadKillTrigger(EntityType.HUSK, EntityType.STRAY),
                JobSiteType.GRAVEYARD,
                UndeadMobType.HUSK.getRequiredLevel(),
                100,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(6000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 680)
                )),
                Material.WITHER_ROSE,
                "Variant Hunter",
                List.of("Slay husks and strays throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_zombie_weekly",
                new UndeadKillTrigger(EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED),
                JobSiteType.GRAVEYARD,
                UndeadMobType.ZOMBIE_VILLAGER.getRequiredLevel(),
                150,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(8000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 900)
                )),
                Material.ZOMBIE_HEAD,
                "Zombie Exterminator",
                List.of("Slay all types of zombies throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_skeleton_weekly",
                new UndeadKillTrigger(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                120,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(9000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 1000)
                )),
                Material.SKELETON_SKULL,
                "Skeleton Exterminator",
                List.of("Slay all types of skeletons throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_spider_weekly",
                new SpiderKillTrigger(),
                JobSiteType.GRAVEYARD,
                ConfigLoader.getStructures().getMausoleumRequiredLevel(),
                80,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(5500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 620)
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
                60,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(7500, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 850)
                )),
                Material.SOUL_SAND,
                "Soul Collector",
                List.of("Collect many souls throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_elite_weekly",
                new UndeadKillTrigger(EntityType.WITHER_SKELETON),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                40,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(12000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 1350)
                )),
                Material.NETHER_STAR,
                "Elite Hunter",
                List.of("Slay the most dangerous undead throughout the week.")
        ));

        weekly.add(new ContractDefinition(
                "graveyard_mixed_weekly",
                new UndeadKillTrigger(
                        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.STRAY,
                        EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED, EntityType.WITHER_SKELETON
                ),
                JobSiteType.GRAVEYARD,
                UndeadMobType.WITHER_SKELETON.getRequiredLevel(),
                250,
                Map.of(),
                new CompositeReward(List.of(
                        new MoneyReward(15000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 1700)
                )),
                Material.DIAMOND_SWORD,
                "Master of the Dead",
                List.of("Prove your mastery over all undead.")
        ));

        // Combined Undead + Spider Weekly (endgame)
        weekly.add(new ContractDefinition(
                "graveyard_nightmare_weekly",
                new UndeadKillTrigger(
                        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.STRAY,
                        EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED, EntityType.WITHER_SKELETON
                ),
                JobSiteType.GRAVEYARD,
                26,
                350,
                Map.of("mausoleum_spawn_speed", 5, "spawn_speed", 5),
                new CompositeReward(List.of(
                        new MoneyReward(22000, 0.35),
                        new JobSiteXpReward(JobSiteType.GRAVEYARD, 2500)
                )),
                Material.NETHERITE_SWORD,
                "Nightmare Slayer",
                List.of("Annihilate everything the graveyard throws at you.",
                        "Requires Spawn Speed 5+ and Horde Frequency 5+")
        ));
    }
}
