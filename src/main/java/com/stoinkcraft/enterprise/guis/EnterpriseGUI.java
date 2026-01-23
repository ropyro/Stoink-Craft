package com.stoinkcraft.enterprise.guis;

import com.stoinkcraft.jobsites.sites.JobSiteManager;
import com.stoinkcraft.jobsites.sites.JobSiteRequirements;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.Role;
import com.stoinkcraft.enterprise.listeners.chatactions.ChatDepositAction;
import com.stoinkcraft.enterprise.listeners.chatactions.ChatWithdrawAction;
import com.stoinkcraft.enterprise.reputation.ReputationCalculator;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.UUID;

public class EnterpriseGUI {

    // Styling constants (matching JobSiteGuiHelper)
    private static final String BULLET = " §7• ";
    private static final String CHECKMARK = "§a✔ ";
    private static final String CROSS = "§c✖ ";
    private static final String ARROW = "§e▶ ";
    private static final String DIVIDER = " ";

    private final Player opener;
    private final Enterprise enterprise;
    private Window currentWindow;

    public EnterpriseGUI(Player opener, Enterprise enterprise) {
        this.opener = opener;
        this.enterprise = enterprise;
    }

    public void openWindow() {
        String netWorth = ChatUtils.formatMoney(enterprise.getNetWorth());
        String balance = ChatUtils.formatMoney(enterprise.getBankBalance());

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # A B C D S # #",
                        "# # # F Q G # # #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('?', new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§8§l» §a§lEnterprise Help §8«")
                        .addLoreLines(DIVIDER)
                        .addLoreLines("§e§lHow to earn money")
                        .addLoreLines(BULLET + "§fComplete tasks at your job sites")
                        .addLoreLines(BULLET + "§fView available tasks with §a/market")
                        .addLoreLines(BULLET + "§fSplit: Player §a" + (int)(100*SCConstants.getPlayerPaySplit()) + "% §f| Enterprise §a" + (100-(int)(100*SCConstants.getPlayerPaySplit())) + "%")
                        .addLoreLines(DIVIDER)
                        .addLoreLines("§e§lJob Sites")
                        .addLoreLines(BULLET + "§fFarmland is free to start")
                        .addLoreLines(BULLET + "§fUnlock more sites as you level up")
                        .addLoreLines(DIVIDER)
                        .addLoreLines("§e§lCommands")
                        .addLoreLines(BULLET + "§f/enterprise §7- opens this menu")
                        .addLoreLines(BULLET + "§f/enterprise warp [name] §7- teleport")
                        .addLoreLines(BULLET + "§f/enterprise info [name] §7- view info")
                ))
                .addIngredient('A', new SimpleItem(new ItemBuilder(Material.BOOK)
                        .setDisplayName(" §aHiring coming soon... ")))
                .addIngredient('B', getBankBalanceItem())
                .addIngredient('C', new AbstractItem() {

                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack openerskull = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) openerskull.getItemMeta();
                        meta.setOwningPlayer(opener);
                        openerskull.setItemMeta(meta);

                        return new ItemBuilder(openerskull)
                                .setDisplayName("§aMembers List")
                                .addLoreLines(DIVIDER)
                                .addLoreLines("§7View all enterprise members")
                                .addLoreLines(DIVIDER)
                                .addLoreLines(ARROW + "Click to open");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openMembersList();
                    }
                })

                .addIngredient('D', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        String multiplier = ReputationCalculator.getMultiplierDisplay(enterprise.getReputation());
                        return new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("§aNetworth §f(§a" + netWorth + "§f)")
                                .addLoreLines(DIVIDER)
                                .addLoreLines(BULLET + "§7Reputation Multiplier: §e" + multiplier)
                                .addLoreLines(BULLET + "§7Bank Balance: §a$" + balance)
                                .addLoreLines(DIVIDER)
                                .addLoreLines("§7Complete contracts to increase reputation")
                                .addLoreLines("§7Expired contracts decrease reputation");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        // Informational only - no action
                    }
                })
                .addIngredient('S', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.BRICKS)
                                .setDisplayName("§aSkyrise")
                                .addLoreLines(DIVIDER)
                                .addLoreLines("§7Your enterprise headquarters")
                                .addLoreLines(DIVIDER)
                                .addLoreLines(ARROW + "Click to teleport");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        enterprise.getJobSiteManager().getSkyriseSite().teleportPlayer(player);
                    }
                })
                .addIngredient('F', createJobSiteItem(JobSiteType.FARMLAND))
                .addIngredient('Q', createJobSiteItem(JobSiteType.QUARRY))
                .addIngredient('G', createJobSiteItem(JobSiteType.GRAVEYARD))
                .build();

        if (enterprise.hasActiveBooster()) {
            gui.setItem(5, 1, new SimpleItem(new ItemBuilder(Material.FIRE_CHARGE)
                    .setDisplayName(" §6§l" + enterprise.getActiveBooster().getMultiplier() + "x booster active!")
                    .addLoreLines(DIVIDER)
                    .addLoreLines(BULLET + "§7Time Left: ?")
                    .addLoreLines(DIVIDER)));
        }

        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle("§8" + enterprise.getName())
                .setGui(gui)
                .build();
        currentWindow.open();
    }

    /**
     * Creates a job site item that shows locked/unlocked state with proper UX
     */
    private AbstractItem createJobSiteItem(JobSiteType type) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteManager manager = enterprise.getJobSiteManager();
                JobSiteRequirements req = JobSiteRequirements.forType(type);

                if (req == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUnknown Job Site");
                }

                boolean unlocked = manager.isJobSiteUnlocked(type);
                boolean canPurchase = manager.canPurchaseJobSite(type);
                JobSiteType prereqType = req.getPrerequisite();
                int prereqLevel = 0;
                if (prereqType != null && manager.getJobSite(prereqType) != null) {
                    prereqLevel = manager.getJobSite(prereqType).getLevel();
                }

                ItemBuilder item = new ItemBuilder(req.getIcon());

                if (unlocked) {
                    // Unlocked - show teleport option
                    item.setDisplayName("§a" + req.getDisplayName());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines("§7" + req.getDescriptionLine1());
                    if (req.getDescriptionLine2() != null && !req.getDescriptionLine2().isEmpty()) {
                        item.addLoreLines("§7" + req.getDescriptionLine2());
                    }
                    item.addLoreLines(DIVIDER);

                    // Show level for unlocked sites
                    int siteLevel = manager.getJobSite(type).getLevel();
                    item.addLoreLines(BULLET + "§fLevel: §a" + siteLevel);
                    item.addLoreLines(DIVIDER);

                    item.addLoreLines(CHECKMARK + "§aUnlocked");
                    item.addLoreLines(ARROW + "Click to teleport");

                } else if (canPurchase) {
                    // Can purchase - show buy option
                    item.setDisplayName("§e" + req.getDisplayName() + " §8(Available)");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines("§7" + req.getDescriptionLine1());
                    if (req.getDescriptionLine2() != null && !req.getDescriptionLine2().isEmpty()) {
                        item.addLoreLines("§7" + req.getDescriptionLine2());
                    }
                    item.addLoreLines(DIVIDER);

                    item.addLoreLines("§e§lPurchase Requirements");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", req.getCost()));
                    if (req.getRequiredPreReqLevel() > 0 && prereqType != null) {
                        String prereqName = prereqType.name().charAt(0) + prereqType.name().substring(1).toLowerCase();
                        item.addLoreLines(CHECKMARK + "§a" + prereqName + " Level " + req.getRequiredPreReqLevel() + " §7(yours: " + prereqLevel + ")");
                    }
                    item.addLoreLines(DIVIDER);

                    item.addLoreLines(CHECKMARK + "§aReady to purchase!");
                    item.addLoreLines(ARROW + "Click to buy");

                } else {
                    // Locked - show requirements
                    item.setMaterial(Material.GRAY_DYE); // Override icon to show locked
                    item.setDisplayName("§c" + req.getDisplayName() + " §8(Locked)");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines("§7" + req.getDescriptionLine1());
                    if (req.getDescriptionLine2() != null && !req.getDescriptionLine2().isEmpty()) {
                        item.addLoreLines("§7" + req.getDescriptionLine2());
                    }
                    item.addLoreLines(DIVIDER);

                    item.addLoreLines("§c§lRequirements");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", req.getCost()));

                    // Show level requirement with status
                    if (req.getRequiredPreReqLevel() > 0 && prereqType != null) {
                        String prereqName = prereqType.name().charAt(0) + prereqType.name().substring(1).toLowerCase();
                        if (prereqLevel >= req.getRequiredPreReqLevel()) {
                            item.addLoreLines(CHECKMARK + "§a" + prereqName + " Level " + req.getRequiredPreReqLevel());
                        } else {
                            item.addLoreLines(CROSS + "§c" + prereqName + " Level " + req.getRequiredPreReqLevel() + " §7(yours: " + prereqLevel + ")");
                        }
                    }

                    // Show prerequisite requirement
                    if (req.getPrerequisite() != null && !manager.isJobSiteUnlocked(req.getPrerequisite())) {
                        item.addLoreLines(CROSS + "§cRequires " + req.getPrerequisite().name() + " unlocked");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines("§8Level up Farmland to unlock");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                JobSiteManager manager = enterprise.getJobSiteManager();
                JobSiteRequirements req = JobSiteRequirements.forType(type);

                if (req == null) return;

                boolean unlocked = manager.isJobSiteUnlocked(type);

                if (unlocked) {
                    // Teleport to the job site
                    manager.getJobSite(type).teleportPlayer(player);
                } else if (manager.canPurchaseJobSite(type)) {
                    // Try to purchase
                    int cost = req.getCost();

                    if (!enterprise.hasManagementPermission(player.getUniqueId())) {
                        sendError(player, "Only Executives and the CEO can purchase job sites!");
                        return;
                    }

                    if (enterprise.getBankBalance() < cost) {
                        sendError(player, "Insufficient enterprise funds! Need §6$" + String.format("%,d", cost));
                        return;
                    }

                    if (manager.purchaseJobSite(type, player)) {
                        sendSuccess(player, req.getDisplayName() + " purchased and built!");
                        enterprise.sendEnterpriseMessage(
                                "",
                                "§a§l" + req.getDisplayName() + " Unlocked!",
                                "",
                                "§7" + player.getName() + " purchased a new job site.",
                                ""
                        );
                        openWindow(); // Refresh the GUI
                    } else {
                        sendError(player, "Unable to purchase job site.");
                    }
                } else {
                    // Show why it's locked
                    String reason = manager.getPurchaseBlockReason(type);
                    if (reason != null) {
                        sendError(player, reason);
                    } else {
                        sendError(player, "Requirements not met.");
                    }
                }
            }
        };
    }

    private void sendSuccess(Player p, String message) {
        ChatUtils.sendMessage(p, "§a✔ " + message);
    }

    private void sendError(Player p, String message) {
        ChatUtils.sendMessage(p, "§c✖ " + message);
    }

    @NotNull
    private AutoUpdateItem getBankBalanceItem() {
        String balance = ChatUtils.formatMoney(enterprise.getBankBalance());
        AutoUpdateItem chestItem = new AutoUpdateItem(20, () -> new ItemBuilder(Material.CHEST)) {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.CHEST)
                        .setDisplayName("§aBank Balance: §f(§a$" + balance + "§f)")
                        .addLoreLines(DIVIDER)
                        .addLoreLines(BULLET + "Balanced accrued through daily/weekly contracts")
                        .addLoreLines(DIVIDER)
                        .addLoreLines("§c[Left-Click]§e Withdraw §7(CEO only)")
                        .addLoreLines("§a[Right-Click]§e Deposit");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                player.closeInventory();
                if (clickType.equals(ClickType.RIGHT)) {
                    player.sendMessage("§7Your enterprise bank currently has: §a" + balance);
                    player.sendMessage("§7Please enter the amount you would like to deposit");
                    ChatDepositAction.awaitingDeposit.add(player.getUniqueId());
                } else if (clickType.equals(ClickType.LEFT)) {
                    if (enterprise.getMemberRole(player.getUniqueId()).equals(Role.CEO)) {
                        player.sendMessage("§7Your enterprise bank currently has: §a" + balance);
                        player.sendMessage("§7Please enter the amount you would like to withdraw");
                        ChatWithdrawAction.awaitingWithdrawal.add(player.getUniqueId());
                    } else {
                        player.sendMessage("§cYou must be the CEO to withdraw enterprise funds.");
                    }
                }
            }
        };
        chestItem.start();
        return chestItem;
    }

    public void openMembersList() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# # # # < # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('<', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.ARROW)
                                .setDisplayName("§c« Back")
                                .addLoreLines("§7Return to Enterprise Menu");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openWindow();
                    }
                })
                .build();

        ItemStack openerskull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) openerskull.getItemMeta();
        meta.setOwningPlayer(opener);
        openerskull.setItemMeta(meta);

        ItemBuilder openerhead = new ItemBuilder(openerskull)
                .setDisplayName("§a" + opener.getName() + " §7(You)")
                .addLoreLines(DIVIDER)
                .addLoreLines(BULLET + "§fRole: §a" + enterprise.getMemberRole(opener.getUniqueId()))
                .addLoreLines(DIVIDER);

        gui.addItems(new SimpleItem(openerhead));

        for (UUID uuid : enterprise.getMembers().keySet()) {
            if (uuid.equals(opener.getUniqueId()) || uuid.equals(SCConstants.serverCEO)) continue;
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);

            ItemStack memberSkull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberSkullMeta = (SkullMeta) memberSkull.getItemMeta();
            memberSkullMeta.setOwningPlayer(member);
            memberSkull.setItemMeta(memberSkullMeta);

            ItemBuilder memberHead = new ItemBuilder(memberSkull)
                    .setDisplayName("§a" + member.getName())
                    .addLoreLines(DIVIDER)
                    .addLoreLines(BULLET + "§fRole: §a" + enterprise.getMemberRole(uuid))
                    .addLoreLines(DIVIDER);

            gui.addItems(new SimpleItem(memberHead));
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8" + enterprise.getName() + " Members")
                .setGui(gui)
                .build();
        window.open();
    }
}
