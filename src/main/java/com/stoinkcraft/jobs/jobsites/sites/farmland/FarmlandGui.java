package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class FarmlandGui {

    private final FarmlandSite farmlandSite;
    private final Player opener;

    public FarmlandGui(FarmlandSite farmlandSite, Player opener){
        this.farmlandSite = farmlandSite;
        this.opener = opener;
    }

    public void openWindow(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# W C P B . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('W', new AbstractItem() {

                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.WHEAT);

                        ItemBuilder wheatItem = new ItemBuilder(item);
                        if(farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.WHEAT)){
                            wheatItem.setDisplayName("Selected!");
                        }else{
                            wheatItem.setDisplayName("Click to switch crops to wheat");
                        }


                        return wheatItem;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(!farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.WHEAT)){
                            farmlandSite.getCropGenerator().setCropGeneratorType(CropGenerator.CropGeneratorType.WHEAT);
                            player.closeInventory();
                        }
                    }
                })
                .addIngredient('C', new AbstractItem() {

                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.CARROT);

                        ItemBuilder carrotUpgrade = new ItemBuilder(item);
                        if(!farmlandSite.getData().isCarrotUnlocked()){
                            carrotUpgrade.setDisplayName("Click to unlock carrots, $25k");
                        }else if(farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.CARROT)){
                            carrotUpgrade.setDisplayName("Selected!");
                        }else{
                            carrotUpgrade.setDisplayName("Click to switch crops to carrots");
                        }


                        return carrotUpgrade;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(!farmlandSite.getData().isCarrotUnlocked()){
                            StoinkCore.getEconomy().withdrawPlayer(player, 25000);
                            farmlandSite.getData().setCarrotUnlocked(true);
                        }else if(!farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.CARROT)){
                            farmlandSite.getCropGenerator().setCropGeneratorType(CropGenerator.CropGeneratorType.CARROT);
                        }
                        player.closeInventory();
                    }
                })
                .addIngredient('P', new AbstractItem() {

                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.POTATO);

                        ItemBuilder potatoUpgrade = new ItemBuilder(item);
                        if(!farmlandSite.getData().isPotatoUnlocked()){
                            potatoUpgrade.setDisplayName("Click to unlock potatoes, $75k");
                        }else if(farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.POTATO)){
                            potatoUpgrade.setDisplayName("Selected!");
                        }else{
                            potatoUpgrade.setDisplayName("Click to switch crops to potatos");
                        }

                        return potatoUpgrade;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(!farmlandSite.getData().isPotatoUnlocked()){
                            StoinkCore.getEconomy().withdrawPlayer(player, 75000);
                            farmlandSite.getData().setPotatoUnlocked(true);
                        }else if(!farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.POTATO)){
                            farmlandSite.getCropGenerator().setCropGeneratorType(CropGenerator.CropGeneratorType.POTATO);
                        }
                        player.closeInventory();
                    }
                })
                .addIngredient('B', new AbstractItem() {

                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.BEETROOT);

                        ItemBuilder carrotUpgrade = new ItemBuilder(item);
                        if(!farmlandSite.getData().isBeetrootUnlocked()){
                            carrotUpgrade.setDisplayName("Click to unlock beetroots, $125k");
                        }else if(farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.BEETROOT)){
                            carrotUpgrade.setDisplayName("Selected!");
                        }else{
                            carrotUpgrade.setDisplayName("Click to switch crops to beetroots");
                        }


                        return carrotUpgrade;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        if(!farmlandSite.getData().isBeetrootUnlocked()){
                            StoinkCore.getEconomy().withdrawPlayer(player, 125000);
                            farmlandSite.getData().setBeetrootUnlocked(true);
                        }else if(!farmlandSite.getData().getCurrentType().equals(CropGenerator.CropGeneratorType.BEETROOT)){
                            farmlandSite.getCropGenerator().setCropGeneratorType(CropGenerator.CropGeneratorType.BEETROOT);
                        }
                        player.closeInventory();
                    }
                })
                .build();

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8Farmland Menu")
                .setGui(gui)
                .build();
        window.open();
    }
}
