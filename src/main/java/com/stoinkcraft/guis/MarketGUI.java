package com.stoinkcraft.guis;

import com.stoinkcraft.market.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;

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
                        .setDisplayName(" * Next Product Rotation: 23h 22m 10s *")
                        .addLoreLines(" ")
                        .addLoreLines("The four items below are currently")
                        .addLoreLines("boosted in value! To see all other")
                        .addLoreLines("available jobs select a category below.")))
                .addIngredient('F', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.COD)
                                .setDisplayName(" §aFishing Jobs ")
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
                                .addLoreLines("§a(!) Click here to view values (!)");
                    }
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openJobValues(MarketManager.JobType.RESOURCE_COLLECTION);
                    }
                })
                .build();

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Task Availability")
                .setGui(gui)
                .build();
        window.open();
    }

    private void openJobValues(MarketManager.JobType jobType){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.CLOCK)
                                .setDisplayName(" * Task Availability *")
                                .addLoreLines(" ")
                                .addLoreLines("(!) Click to return to main menu (!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        new MarketGUI(player).openWindow();
                    }
                })
                .build();

        HashMap<Material, Double> materialItemValues = new HashMap<>();
        Map<String, Double> stringItemValues;
        switch(jobType) {
            case HUNTING:
                stringItemValues = MarketManager.getPrices(HUNTING);
                break;

            case FISHING:
                stringItemValues = MarketManager.getPrices(FISHING);
                break;

            case RESOURCE_COLLECTION:
                stringItemValues = MarketManager.getPrices(RESOURCE_COLLECTION);
                break;
            default:
                stringItemValues = null;
        }
        try{
            stringItemValues.keySet()
                    .stream()
                    .forEach(item -> materialItemValues.put(Material.valueOf(item), stringItemValues.get(item)));
        }catch(NullPointerException e){}

        materialItemValues.keySet().stream()
                .forEach(item -> {
                    gui.addItems(new SimpleItem(new ItemBuilder(item)
                            .setDisplayName(item.name())
                            .addLoreLines(" ")
                            .addLoreLines(" Value: " + materialItemValues.get(item))));
                } );

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Task Values for " + jobType.name())
                .setGui(gui)
                .build();
        window.open();
    }
}
