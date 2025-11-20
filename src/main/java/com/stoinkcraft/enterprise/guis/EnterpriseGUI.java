package com.stoinkcraft.enterprise.guis;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import com.stoinkcraft.enterprise.listeners.ChatDepositListener;
import com.stoinkcraft.enterprise.listeners.ChatInvestListener;
import com.stoinkcraft.enterprise.listeners.ChatWithdrawListener;
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

    private final Player opener;
    private final Enterprise enterprise;

    public EnterpriseGUI(Player opener, Enterprise enterprise){
        this.opener = opener;
        this.enterprise = enterprise;
    }

    public void openWindow(){
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
                        .setDisplayName("§8§l» §a§lEnterprise Help §8»")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lHow to earn money §8»")
                        .addLoreLines(" §a• §fWhen in an enterprise completing tasks makes money")
                        .addLoreLines("   §ffor you, and the enterprise!")
                        .addLoreLines(" §a• §fTo see the available tasks do §a/market")
                        .addLoreLines(" §a• §fCurrent Split: Player, §a%" + 100*SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE + " §fEnterprise, §a%" + (100-100*SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE))
                        .addLoreLines(" ")
                        .addLoreLines("§a§lCommands §8»")
                        .addLoreLines(" §a• §f/enterprise - opens this menu")
                        .addLoreLines(" §a• §f/enterprise resign - lets you leave an enterprise")
                        .addLoreLines(" §a• §f/enterprise warp [name] - teleports to an enterprise")
                        .addLoreLines(" §a• §f/enterprise info [name] - returns enterprise information")
                        .addLoreLines(" ")
                        .addLoreLines("§a§lCEO Commands §8»")
                        .addLoreLines(" §a• §f/enterprise setwarp - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise disband - sets the public warp for the enterprise")
                        .addLoreLines(" §a• §f/enterprise invite <player> - invite new members")
                        .addLoreLines(" ")
                        .addLoreLines("ID: " + enterprise.getID())
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

                        ItemBuilder openerhead = new ItemBuilder(openerskull)
                                .setDisplayName(" §aMembers List")
                                .addLoreLines(" ")
                                .addLoreLines(" §a(!) §fClick here to view the members list §a(!)");

                        return openerhead;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        openMembersList();
                    }
                })

                .addIngredient('D', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName(" §aNetworth §f(§a" + netWorth + "§f)")
                                .addLoreLines(" ")
                                .addLoreLines(" §a(!) §fClick here to invest bank funds into networth §a(!)");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        player.closeInventory();
                        if(enterprise.getMemberRole(player.getUniqueId()).equals(Role.CEO)){
                            player.sendMessage("§7Your enterprise bank currently has: §a" + balance);
                            player.sendMessage("§7Please enter the amount you would like to invest");
                            ChatInvestListener.awaitingInvestment.add(player.getUniqueId());
                        }else{
                            player.sendMessage("§cYou must be the CEO to invest enterprise funds.");
                        }
                    }
                })
                .addIngredient('S', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.BRICKS);

                        ItemBuilder openerhead = new ItemBuilder(item)
                                .setDisplayName("§aTeleport to Skyrise");

                        return openerhead;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        enterprise.getJobSiteManager().getSkyriseSite().teleportPlayer(player);
                    }
                })
        .addIngredient('Q', new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);

                ItemBuilder openerhead = new ItemBuilder(item)
                        .setDisplayName("§aTeleport to Quarry");

                return openerhead;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                enterprise.getJobSiteManager().getQuarrySite().teleportPlayer(player);
            }
        })
                .addIngredient('G', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.SKELETON_SKULL);

                        ItemBuilder openerhead = new ItemBuilder(item)
                                .setDisplayName("§aGraveyard Coming soon...");

                        return openerhead;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        //enterprise.getJobSiteManager().getFarmlandSite().teleportPlayer(player);
                    }
                })
                .addIngredient('F', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        ItemStack item = new ItemStack(Material.WHEAT);

                        ItemBuilder openerhead = new ItemBuilder(item)
                                .setDisplayName("§aTeleport to Farmland");

                        return openerhead;
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                        enterprise.getJobSiteManager().getFarmlandSite().teleportPlayer(player);
                    }
                })
                .build();

        if(enterprise.isBoosted()){
            gui.setItem(4, 4, new SimpleItem(new ItemBuilder(Material.FIRE_CHARGE)
                    .setDisplayName(" §6§l" + enterprise.getActiveBooster().getMultiplier() + "x booster active!")
                    .addLoreLines(" ")
                    .addLoreLines(" §7• Time Left: ")
                    .addLoreLines(" ")));
        }

        Window window = Window.single()
                .setViewer(opener)
                .setTitle("§8" + enterprise.getName())
                .setGui(gui)
                .build();
        window.open();
    }

    @NotNull
    private AutoUpdateItem getBankBalanceItem() {
        String balance = ChatUtils.formatMoney(enterprise.getBankBalance());
        AutoUpdateItem chestItem = new AutoUpdateItem(20, () -> new ItemBuilder(Material.CHEST)) {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.CHEST)
                        .setDisplayName(" §aBank Balance: §f(§a$" + balance + "§f)")
                        .addLoreLines(" ")
                        .addLoreLines(" §a• §fBank balance will be taxed daily at, %" + (SCConstants.ENTERPRISE_DAILY_TAX*100))
                        .addLoreLines("   §fTime until next taxation: " + EnterpriseManager.getTimeUntilNextTaxation())
                        .addLoreLines(" §a• §fBank balance after daily tax: §c$" + ChatUtils.formatMoney(enterprise.getBankBalance()*(1-SCConstants.ENTERPRISE_DAILY_TAX)))
                        .addLoreLines(" ")
                        .addLoreLines(" §a(!) §bLeft §fclick here to §bwithdraw §fbank funds §a(!)")
                        .addLoreLines(" §a(!) §bRight §fclick here to §bdeposit §fbank funds §a(!)");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                player.closeInventory();
                if(clickType.equals(ClickType.RIGHT)){
                    player.sendMessage("§7Your enterprise bank currently has: §a" + balance);
                    player.sendMessage("§7Please enter the amount you would like to deposit");
                    ChatDepositListener.awaitingDeposit.add(player.getUniqueId());
                }else if(clickType.equals(ClickType.LEFT)){
                    if(enterprise.getMemberRole(player.getUniqueId()).equals(Role.CEO)){
                        player.sendMessage("§7Your enterprise bank currently has: §a" + balance);
                        player.sendMessage("§7Please enter the amount you would like to withdraw");
                        ChatWithdrawListener.awaitingWithdrawal.add(player.getUniqueId());
                    }else{
                        player.sendMessage("§cYou must be the CEO to withdraw enterprise funds.");
                    }
                }
            }
        };
        chestItem.start();
        return chestItem;
    }

    public void openMembersList(){
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . X X X X #",
                        "# X X X X X X X #",
                        "# # # # < # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setDisplayName(" ")))
                .addIngredient('X', new SimpleItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .setDisplayName(" §cThis member slot is locked ")))
                .addIngredient('<', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.BARRIER)
                                .setDisplayName(" §cReturn to Enterprise Menu ");
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
                .setDisplayName(" §aYOU ")
                .addLoreLines(" ")
                .addLoreLines(" §a• §fRole: " + enterprise.getMemberRole(opener.getUniqueId()))
                .addLoreLines(" ");

        gui.addItems(new SimpleItem(openerhead));

        // Add top enterprises to slots 1–6
        for (UUID uuid : enterprise.getMembers().keySet()) {
            if(uuid.equals(opener.getUniqueId()) || uuid.equals(SCConstants.serverCEO)) continue;
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);

            ItemStack memberSkull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberSkullMeta = (SkullMeta) memberSkull.getItemMeta();
            memberSkullMeta.setOwningPlayer(member);
            memberSkull.setItemMeta(memberSkullMeta);

            ItemBuilder memberHead = new ItemBuilder(memberSkull)
                    .setDisplayName(" §a" + member.getName())
                    .addLoreLines(" ")
                    .addLoreLines(" §a• §fRole: " + enterprise.getMemberRole(uuid))
                    .addLoreLines(" ");

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
