package com.stoinkcraft.items.graveyard;

import com.stoinkcraft.items.StoinkItem;
import com.stoinkcraft.jobsites.sites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * A voucher that grants souls to the player's Graveyard when redeemed.
 * The soul amount is stored in PDC, allowing flexible amounts via commands.
 */
public class SoulVoucherItem extends StoinkItem {

    private static final String ITEM_ID = "soul_voucher";
    private static final NamespacedKey SOUL_AMOUNT_KEY = new NamespacedKey(StoinkCore.getInstance(), "soul_amount");

    private final int defaultSoulAmount;

    public SoulVoucherItem(int defaultSoulAmount) {
        this.defaultSoulAmount = defaultSoulAmount;
    }

    public SoulVoucherItem() {
        this(100); // Default amount if not specified
    }

    @Override
    public String getItemId() {
        return ITEM_ID;
    }

    @Override
    public Material getMaterial() {
        return Material.SKULL_BANNER_PATTERN;
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Soul Voucher";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "",
                ChatColor.GRAY + "Redeem this voucher to add",
                ChatColor.GRAY + "souls to your Graveyard!",
                "",
                ChatColor.GRAY + "Souls: " + ChatColor.LIGHT_PURPLE + "{amount}",
                "",
                ChatColor.DARK_GRAY + "Right-click to redeem"
        );
    }

    @Override
    public boolean canUse(Player player, PlayerInteractEvent event) {
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            ChatUtils.sendMessage(player, ChatColor.RED + "You must be in an enterprise to use this!");
            return false;
        }

        GraveyardSite graveyard = enterprise.getJobSiteManager().getGraveyardSite();
        if (graveyard == null || !graveyard.isBuilt()) {
            ChatUtils.sendMessage(player, ChatColor.RED + "Your enterprise doesn't have a Graveyard yet!");
            return false;
        }

        return true;
    }

    @Override
    public boolean onUse(Player player, PlayerInteractEvent event) {
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            return false;
        }

        GraveyardSite graveyard = enterprise.getJobSiteManager().getGraveyardSite();
        if (graveyard == null) {
            return false;
        }

        // Get soul amount from the item
        int soulAmount = getSoulAmount(event.getItem());

        // Add souls to graveyard
        graveyard.getData().addSouls(soulAmount);

        // Feedback
        ChatUtils.sendMessage(player,
                ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.GREEN + "Redeemed " +
                        ChatColor.LIGHT_PURPLE + soulAmount + " souls" +
                        ChatColor.GREEN + " to your Graveyard!");

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);

        return true; // Consume the item
    }

    /**
     * Creates a Soul Voucher with a specific soul amount.
     */
    public ItemStack createItemStack(int amount, int soulAmount) {
        ItemStack item = new ItemStack(getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Soul Voucher " +
                ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + formatSouls(soulAmount) + " ✦");

        // Build lore with actual amount
        List<String> lore = List.of(
                "",
                ChatColor.GRAY + "Redeem this voucher to add",
                ChatColor.GRAY + "souls to your Graveyard!",
                "",
                ChatColor.GRAY + "Souls: " + ChatColor.LIGHT_PURPLE + formatSouls(soulAmount),
                "",
                ChatColor.DARK_GRAY + "Right-click to redeem"
        );
        meta.setLore(lore);

        // Store item ID
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, getItemId());

        // Store soul amount
        pdc.set(SOUL_AMOUNT_KEY, PersistentDataType.INTEGER, soulAmount);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a single Soul Voucher with a specific soul amount.
     */

    @Override
    public ItemStack createItemStack(int amount) {
        return createItemStack(amount, defaultSoulAmount);
    }

    /**
     * Gets the soul amount stored in the item.
     */
    public static int getSoulAmount(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        return pdc.getOrDefault(SOUL_AMOUNT_KEY, PersistentDataType.INTEGER, 0);
    }

    /**
     * Formats soul amount with commas for display.
     */
    private String formatSouls(int amount) {
        return String.format("%,d", amount);
    }
}
