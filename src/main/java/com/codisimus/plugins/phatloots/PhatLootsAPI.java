package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.events.ChestOpenEvent;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * API for the PhatLoots plugin
 *
 * @author Codisimus
 */
public class PhatLootsAPI {
    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block The given Block
     * @return true if the Block is a PhatLootChest
     */
    public static boolean isPhatLootChest(Block block) {
        return PhatLootChest.isPhatLootChest(block);
    }

    /**
     * Returns the PhatLoot of the given name
     *
     * @param phatLootName The name of the PhatLoot to find
     * @return The PhatLoot or null if there is none by that name
     */
    public static PhatLoot getPhatLoot(String phatLootName) {
        return PhatLoots.getPhatLoot(phatLootName);
    }

    /**
     * Rolls for loot of the specified loot tables.
     * An empty list is returned if the PhatLoot of the given name does not exist
     *
     * @param phatLootName The name of the specified loot tables
     * @return The loot that was given from the roll
     */
    public static LootBundle rollForLoot(String phatLootName) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot(phatLootName);
        return phatLoot == null
               ? new LootBundle()
               : phatLoot.rollForLoot();
    }

    /**
     * Returns all PhatLoots that are linked to the given Block
     *
     * @param block The given Block
     * @return a list of PhatLoots linked to the Block
     */
    public static LinkedList<PhatLoot> getLinkedPhatLoots(Block block) {
        return PhatLoots.getPhatLoots(block);
    }

    /**
     * Returns all PhatLoots
     *
     * @return A collection of all PhatLoots
     */
    public static Collection<PhatLoot> getAllPhatLoots() {
        return PhatLoots.getPhatLoots();
    }

    /**
     * Returns all PhatLootChests
     *
     * @return A collection of all PhatLootChests
     */
    public static Collection<PhatLootChest> getAllPhatLootChests() {
        return PhatLootChest.getChests();
    }

    /**
     * Returns all Blocks that are linked to a PhatLoot
     *
     * @return a List of Blocks that are PhatLootChests
     */
    public static List<Block> getAllPhatLootChestBlocks() {
        List<Block> blockList = new ArrayList<>();
        for (PhatLootChest chest : getAllPhatLootChests()) {
            blockList.add(chest.getBlock());
        }
        return blockList;
    }

    /**
     * Forces the given Player to Loot the given Block if it is a PhatLootChest
     *
     * @param block The Block which may be linked to a PhatLoot
     * @param player The given Player
     * @return true if the Block was linked and looted, false otherwise
     */
    public static boolean loot(Block block, Player player) {
        return loot(block, player, false);
    }

    /**
     * Forces the given Player to Loot the given Block if it is a PhatLootChest
     *
     * @param block The Block which may be linked to a PhatLoot
     * @param player The given Player
     * @param autoSpill true if the Chest should spill its loot
     * @return true if the Block was linked and looted, false otherwise
     */
    public static boolean loot(Block block, Player player, boolean autoSpill) {
        LinkedList<PhatLoot> phatLoots = PhatLoots.getPhatLoots(block, player);
        if (phatLoots.isEmpty()) {
            return false;
        }

        if (!PhatLoots.plugin.getPluginHookManager().getWorldGuardManager().isRegionLootableIfOwnerOrMembers(block)) {
            return false;
        }
        PhatLootChest plChest = PhatLootChest.getChest(block);

        //Roll for Loot of each linked PhatLoot
        boolean flagToBreak = true;
        for (PhatLoot phatLoot : phatLoots) {
            //AutoSpill only works with break and respawn
            //Break and respawn only works on global PhatLoots
            if (autoSpill && (!phatLoot.global || !phatLoot.breakAndRespawn)) {
                continue;
            }
            if (!phatLoot.rollForChestLoot(player, plChest, null, autoSpill)) {
                //Don't break the Chest if any PhatLoots return false
                flagToBreak = false;
            }
        }

        if (flagToBreak) {
            plChest.breakChest(player, plChest.getResetTime(phatLoots));
        } else {
            ChestOpenEvent event = new ChestOpenEvent(plChest,
                    player.getOpenInventory().getTopInventory(),
                    player);
            if (event.getInventory().getType() == InventoryType.PLAYER)
                return true;
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                player.closeInventory();
        }
        return true;
    }

    /**
     * Links the given ItemStack to the given PhatLoot
     *
     * @param item The ItemStack to link
     * @param phatLoot The PhatLoot to link the ItemStack to
     */
    public static void link(ItemStack item, PhatLoot phatLoot) {
        if (PhatLootsConfig.persistentDataContainerLinks) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer itemTagContainer = meta.getPersistentDataContainer();
            itemTagContainer.set(PhatLoot.LINK_TAG, PersistentDataType.STRING, phatLoot.name);
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(PhatLootsConfig.lootBagKeys.get(0) + phatLoot.name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    /**
     * Reloads the PhatLoot plugin.
     * This includes save data and config settings
     */
    public static void reload() {
        PhatLoots.rl();
    }

    /**
     * Saves all plugin information.
     * This includes loot tables, linked chests, and loot times.
     * This is a lot of data writing and should not be called too often.
     * It is better to save individual PhatLoots and only the modified information
     */
    public static void saveAll() {
        PhatLoots.saveAll();
    }
}
