package com.codisimus.plugins.phatloots.events;

import com.codisimus.plugins.phatloots.PhatLootChest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * Called when a PhatLootChest is opened
 *
 * @author anjoismysign
 */
public class ChestOpenEvent extends PhatLootChestEvent{
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Inventory inventory;
    private final Player player;

    public ChestOpenEvent(PhatLootChest chest,
                          Inventory inventory,
                          Player player) {
        this.chest = chest;
        this.inventory = inventory;
        this.player = player;
    }

    /**
     * Returns the Inventory of the chest that was opened
     *
     * @return The Inventory of the chest that was opened
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the Player who opened the chest
     *
     * @return The Player who opened the chest
     */
    public Player getPlayer() {
        return player;
    }
}
