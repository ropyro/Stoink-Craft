package com.stoinkcraft.market;

import com.stoinkcraft.market.values.EntityValue;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;

import static com.stoinkcraft.market.MarketManager.JobType.*;

public class MarketGUI {

    private Player opener;

    public MarketGUI(Player opener){
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . # . # . # . #",
                        "# # # # # # # # #",
                        "# # F # H # R # #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.CLOCK)
                        .setDisplayName(" §a* Next Product Rotation: 23h 22m 10s *")
                        .addLoreLines(" ")
                        .addLoreLines("§7The four tasks below are currently")
                        .addLoreLines("§7boosted in value! To see all other")
                        .addLoreLines("§7available jobs select a category below.")))
                .addIngredient('?', getAutoUpdateItem())
                .addIngredient('F', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.SALMON)
                                .setDisplayName(" §aFishing Jobs ")
                                .addLoreLines(" ")
                                .addLoreLines("§7Fishing tasks require you,")
                                .addLoreLines("§7to fish these items with a rod!")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to view values (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openJobValues(FISHING);
                    }
                })
                .addIngredient('H', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.IRON_SWORD)
                                .setDisplayName(" §aHunting Jobs ")
                                .addLoreLines(" ")
                                .addLoreLines("§7Hunting tasks require you,")
                                .addLoreLines("§7to kill these entities!")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to view values (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openJobValues(HUNTING);
                    }
                })
                .addIngredient('R', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GRASS_BLOCK)
                                .setDisplayName(" §aResource Collection Jobs ")
                                .addLoreLines(" ")
                                .addLoreLines("§7Resource collection tasks require you,")
                                .addLoreLines("§7to collect these items!")
                                .addLoreLines("§7Mining/Farming/Wood Chopping")
                                .addLoreLines(" ")
                                .addLoreLines("§a(!) Click here to view values (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openJobValues(MarketManager.JobType.RESOURCE_COLLECTION);
                    }
                })
                .build();
        try{
            MarketManager.getBoostedPrices().keySet().stream()
                    .limit(4)
                    .forEach(item -> {
                        ItemStack boostedItemStack;
                        if (item instanceof EntityValue) {
                            boostedItemStack = new ItemStack(getItemFromEntity(((EntityValue) item).getEntityType()));
                        }else{
                            boostedItemStack = new ItemStack(item.getMaterialValue());
                        }

                        ItemMeta meta = boostedItemStack.getItemMeta();
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                        boostedItemStack.setItemMeta(meta);

                        ItemBuilder boostedItem = new ItemBuilder(boostedItemStack)
                                .setDisplayName(item.getDisplayName())
                                .addLoreLines(" ");

                        switch(MarketManager.getBoostedPrices().get(item)){
                            case FISHING -> boostedItem.addLoreLines("§7Fishing Job");
                            case HUNTING -> boostedItem.addLoreLines("§7Hunting Job");
                            case RESOURCE_COLLECTION -> boostedItem.addLoreLines("§7Resource Collection");
                        }

                        double value = 0.0;

                        if (item instanceof EntityValue) {
                            value = MarketManager.getEntityPrice(((EntityValue) item).getEntityType());
                        }else{
                            value = MarketManager.getItemPrice(item.getMaterialValue());
                        }

                        boostedItem.addLoreLines(" ");
                        boostedItem.addLoreLines("§7Boost Multiplier: §a" + SCConstants.PRICE_BOOST + "x");
                        boostedItem.addLoreLines(" ");
                        boostedItem.addLoreLines("§7Regular Value: §c§o$" + ChatUtils.formatMoney(value / SCConstants.PRICE_BOOST));
                        boostedItem.addLoreLines("§7Boosted Value: §a$" + ChatUtils.formatMoney(value));

                        gui.addItems(new SimpleItem(boostedItem));
                    });
        }catch(NullPointerException e){}


        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Task Availability")
                .setGui(gui)
                .build();
        window.open();
    }

    @NotNull
    private AutoUpdateItem getAutoUpdateItem() {
        AutoUpdateItem clockItem = new AutoUpdateItem(20, () -> new ItemBuilder(Material.CLOCK)
                .setDisplayName(" §a* Next Product Rotation: " + MarketManager.getTimeUntilNextRotation() + " *")
                .addLoreLines(" ")
                .addLoreLines("§7The four tasks below are currently")
                .addLoreLines("§7boosted in value! To see all other")
                .addLoreLines("§7available jobs select a category below."));
        clockItem.start();
        return clockItem;
    }

    private void openJobValues(MarketManager.JobType jobType){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # X # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('X', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.BARRIER)
                                .setDisplayName(" §c§lExit To Main Menu ");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new MarketGUI(player).openWindow();
                    }
                })
                .build();

        switch(jobType) {
            case HUNTING:
                MarketManager.getHuntingPrices().stream()
                        .forEach(entity -> gui.addItems(new SimpleItem(new ItemBuilder(getItemFromEntity(entity.getEntityType()))
                                .setDisplayName(entity.getDisplayName())
                                .addLoreLines(" ")
                                .addLoreLines(" §7Value: §a$" + ChatUtils.formatMoney(MarketManager.getPrice(entity))))));
                break;
            case FISHING:
                MarketManager.getFishingPrices().stream()
                        .forEach(item -> gui.addItems(new SimpleItem(new ItemBuilder(item.getMaterial())
                                .setDisplayName(item.getDisplayName())
                                .addLoreLines(" ")
                                .addLoreLines(" §7Value: §a$" + ChatUtils.formatMoney(MarketManager.getPrice(item))))));
                break;

            case RESOURCE_COLLECTION:
                MarketManager.getResourcePrices().stream()
                        .forEach(item -> gui.addItems(new SimpleItem(new ItemBuilder(item.getMaterial())
                                .setDisplayName(item.getDisplayName())
                                .addLoreLines(" ")
                                .addLoreLines(" §7Value: §a$" + ChatUtils.formatMoney(MarketManager.getPrice(item))))));
                break;
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Task Values for " + jobType.name())
                .setGui(gui)
                .build();
        window.open();
    }

    public boolean isEntity(String entityName){
        boolean isEntity = false;
        if(Arrays.stream(EntityType.values()).toList().stream().map(e -> e.name()).toList().contains(entityName)) isEntity = true;
        return isEntity;
    }

    public static Material getItemFromEntity(EntityType entityType) {
        try {
            return Material.valueOf(entityType.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException e) {
            return Material.AIR; // Fallback for entities without a spawn egg
        }
    }

}
