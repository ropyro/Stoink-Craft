package com.stoinkcraft.items;

import com.stoinkcraft.items.booster.BoosterItem;
import com.stoinkcraft.items.booster.BoosterTier;
import com.stoinkcraft.items.farmland.FertilizerBomb;
import com.stoinkcraft.items.graveyard.SoulVoucherItem;
import com.stoinkcraft.items.graveyard.hound.GraveyardHoundItem;
import com.stoinkcraft.items.quarry.MineBomb;
import com.stoinkcraft.items.quarry.MineBombTier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoinkItemRegistry {

    private static final Map<String, StoinkItem> registeredItems = new HashMap<>();


    public static void registerItems(){
        register(new BoosterItem(BoosterTier.SMALL));
        register(new BoosterItem(BoosterTier.MEDIUM));
        register(new BoosterItem(BoosterTier.LARGE));
        register(new FertilizerBomb());
        register(new MineBomb(MineBombTier.SMALL));
        register(new MineBomb(MineBombTier.MEDIUM));
        register(new MineBomb(MineBombTier.LARGE));
        register(new SoulVoucherItem());
        register(new GraveyardHoundItem());
    }

    private static void register(StoinkItem item) {
        if (registeredItems.containsKey(item.getItemId())) {
            throw new IllegalArgumentException("Item with ID '" + item.getItemId() + "' is already registered!");
        }
        registeredItems.put(item.getItemId(), item);
    }

    @Nullable
    public static StoinkItem getById(String itemId) {
        return registeredItems.get(itemId);
    }

    @Nullable
    public static StoinkItem getFromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String itemId = pdc.get(StoinkItem.ITEM_ID_KEY, PersistentDataType.STRING);
        if (itemId == null) return null;

        return registeredItems.get(itemId);
    }

    public static boolean isCustomItem(ItemStack item) {
        return getFromItemStack(item) != null;
    }

    public static Collection<StoinkItem> getAllItems() {
        return registeredItems.values();
    }

    @Nullable
    public static ItemStack createItemStack(String itemId, int amount) {
        StoinkItem item = getById(itemId);
        if (item == null) return null;
        return item.createItemStack(amount);
    }

    public static void clear() {
        registeredItems.clear();
    }
}