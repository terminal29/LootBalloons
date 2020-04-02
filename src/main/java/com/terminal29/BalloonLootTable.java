package com.terminal29;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BalloonLootTable implements LootTable {
    private List<ItemStack> _balloonLootStack;
    private Plugin _plugin;

    public BalloonLootTable(Plugin plugin, List<ItemStack> loot){
        _balloonLootStack = loot;
        _plugin = plugin;
    }
    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        ItemStack loot = _balloonLootStack.get(random.nextInt(_balloonLootStack.size()));
        return new ArrayList<>(Collections.singletonList(loot));
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        ItemStack loot = _balloonLootStack.get(random.nextInt(_balloonLootStack.size()));
        inventory.addItem(loot);
    }

    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(_plugin, "balloonLoot");
    }
}
