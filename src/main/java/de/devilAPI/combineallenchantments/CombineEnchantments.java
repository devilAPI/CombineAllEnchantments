package de.devilAPI.combineallenchantments;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.stream.Collectors;

public class CombineEnchantments extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CombineEnchantments plugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CombineEnchantments plugin disabled.");
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null) {
            return;
        }

        // Clone the first item to use as the result
        ItemStack resultItem = firstItem.clone();

        // Merge enchantments from both items
        Map<Enchantment, Integer> combinedEnchantments = firstItem.getEnchantments().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::max));

        secondItem.getEnchantments().forEach((enchantment, level) ->
                combinedEnchantments.merge(enchantment, level, Integer::max));

        // Check if the second item is an enchanted book and merge its enchantments
        if (secondItem.getType().toString().endsWith("BOOK")) {
            Map<Enchantment, Integer> bookEnchantments = secondItem.getEnchantments();
            bookEnchantments.forEach((enchantment, level) ->
                    combinedEnchantments.merge(enchantment, level, Integer::max));
        }

        // Apply the combined enchantments to the result item
        combinedEnchantments.forEach((enchantment, level) ->
                resultItem.addUnsafeEnchantment(enchantment, level));

        // Set the result in the anvil inventory
        event.setResult(resultItem);

        // Calculate and set the repair cost based on vanilla mechanics
        int repairCost = calculateRepairCost(firstItem, secondItem);
        inventory.setRepairCost(repairCost);
    }

    private int calculateRepairCost(ItemStack firstItem, ItemStack secondItem) {
        int cost = 0;

        // Add base repair cost of both items
        cost += firstItem.getEnchantments().size() + secondItem.getEnchantments().size();

        // Add levels of all combined enchantments
        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            cost += entry.getValue();
        }
        for (Map.Entry<Enchantment, Integer> entry : secondItem.getEnchantments().entrySet()) {
            cost += entry.getValue();
        }

        // Multiply by a constant to approximate vanilla behavior (can be fine-tuned)
        cost *= 2;

        return cost;
    }
}
