package com.stoinkcraft.jobsites.sites.sites.farmland;

import com.stoinkcraft.jobsites.sites.JobSiteUpgrade;
import com.stoinkcraft.jobsites.sites.components.generators.CropGenerator;
import com.stoinkcraft.jobsites.sites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.utils.guis.JobSiteGuiHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import static com.stoinkcraft.utils.guis.JobSiteGuiHelper.*;

/**
 * GUI for managing an individual greenhouse.
 * Allows selecting crop type and upgrading growth speed.
 */
public class GreenhouseGui {

    private static final JobSiteGuiHelper.Theme THEME = JobSiteGuiHelper.Theme.FARMLAND;

    private final FarmlandSite farmlandSite;
    private final GreenhouseGenerator greenhouse;
    private final Player opener;
    private Window currentWindow;

    public GreenhouseGui(FarmlandSite farmlandSite, GreenhouseGenerator greenhouse, Player opener) {
        this.farmlandSite = farmlandSite;
        this.greenhouse = greenhouse;
        this.opener = opener;
    }

    public void openWindow() {
        int index = greenhouse.getGreenhouseIndex();

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# W C P B # # G #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createHelpItem())
                .addIngredient('W', createCropItem(Material.WHEAT, CropGenerator.CropGeneratorType.WHEAT, null))
                .addIngredient('C', createCropItem(Material.CARROT, CropGenerator.CropGeneratorType.CARROT, "unlock_carrot"))
                .addIngredient('P', createCropItem(Material.POTATO, CropGenerator.CropGeneratorType.POTATO, "unlock_potato"))
                .addIngredient('B', createCropItem(Material.BEETROOT, CropGenerator.CropGeneratorType.BEETROOT, "unlock_beetroot"))
                .addIngredient('G', createUpgradeItem(
                        farmlandSite,
                        greenhouse.getGrowthSpeedUpgradeKey(),
                        Material.WATER_BUCKET,
                        "Growth Speed",
                        "Increases how fast crops grow in this greenhouse",
                        THEME,
                        this::openWindow
                ))
                .build();

        open(gui, "§8Greenhouse " + index);
    }

    private AbstractItem createHelpItem() {
        int index = greenhouse.getGreenhouseIndex();
        int growthLevel = farmlandSite.getData().getLevel(greenhouse.getGrowthSpeedUpgradeKey());
        CropGenerator.CropGeneratorType cropType = farmlandSite.getData().getGreenhouseCropType(index);

        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName(header(THEME, "Greenhouse " + index))
                        .addLoreLines(DIVIDER)
                        .addLoreLines(subHeader(THEME, "Current Status"))
                        .addLoreLines(BULLET + "§fCrop: §a" + formatName(cropType.name()))
                        .addLoreLines(BULLET + "§fGrowth Level: §a" + growthLevel)
                        .addLoreLines(DIVIDER)
                        .addLoreLines(subHeader(THEME, "How It Works"))
                        .addLoreLines(BULLET + "§fSelect a crop type to grow")
                        .addLoreLines(BULLET + "§fUpgrade growth speed for faster harvests")
                        .addLoreLines(BULLET + "§fEach greenhouse is independent");
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                // Help item, no action
            }
        };
    }

    private AbstractItem createCropItem(Material mat, CropGenerator.CropGeneratorType type, String unlockUpgradeId) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(farmlandSite, unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;
                boolean selected = farmlandSite.getData().getGreenhouseCropType(greenhouse.getGreenhouseIndex()) == type;
                int jobsiteLevel = farmlandSite.getLevel();

                ItemBuilder item = new ItemBuilder(mat);

                if (!unlocked) {
                    int requiredLevel = upgrade.getRequiredJobsiteLevel(1);

                    item.setDisplayName("§c" + formatName(type.name()) + " §8(Locked)");
                    item.addLoreLines("§7Unlock this crop type");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + requiredLevel);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < requiredLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canPurchase(farmlandSite, 1)) {
                        item.addLoreLines(CROSS + "§cPrevious crop not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + formatName(type.name()) + " §8(Active)");
                    item.addLoreLines("§7Currently growing this crop");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aSelected");

                } else {
                    item.setDisplayName("§e" + formatName(type.name()));
                    item.addLoreLines("§7Click to grow this crop");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to select");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(farmlandSite, unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                if (!unlocked) {
                    if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, formatName(type.name()) + " unlocked!");
                    } else {
                        sendUpgradePurchaseError(p, farmlandSite, upgrade);
                    }
                } else {
                    greenhouse.setCropType(type);
                    sendSuccess(p, "Greenhouse " + greenhouse.getGreenhouseIndex() + " now growing " + formatName(type.name()) + "!");
                }

                openWindow();
            }
        };
    }

    private void open(Gui gui, String title) {
        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build();
        currentWindow.open();
    }
}
