package com.stoinkcraft.utils.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ClickableAutoUpdateItem extends AutoUpdateItem {

    private final BiConsumer<Player, InventoryClickEvent> clickHandler;

    public ClickableAutoUpdateItem(
            int period,
            Supplier<? extends ItemProvider> itemSupplier,
            BiConsumer<Player, InventoryClickEvent> clickHandler
    ) {
        super(period, itemSupplier);
        this.clickHandler = clickHandler;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(player, event);
        }
    }
}