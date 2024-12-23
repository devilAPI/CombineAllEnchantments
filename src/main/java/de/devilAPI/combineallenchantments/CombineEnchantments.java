package de.devilAPI.combineallenchantments;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class CombineEnchantments extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CombineAllEnchantments plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CombineAllEnchantments plugin disabled!");
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if (firstItem != null && secondItem != null) {
            ItemStack result = firstItem.clone();
            ItemMeta resultMeta = result.getItemMeta();

            if (resultMeta != null) {
                Map<Enchantment, Integer> firstEnchantments = firstItem.getEnchantments();
                Map<Enchantment, Integer> secondEnchantments = secondItem.getEnchantments();

                // Add all enchantments from the first item
                for (Map.Entry<Enchantment, Integer> entry : firstEnchantments.entrySet()) {
                    resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                }

                // Add all enchantments from the second item, combining levels if necessary
                for (Map.Entry<Enchantment, Integer> entry : secondEnchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    if (resultMeta.hasEnchant(enchantment)) {
                        int existingLevel = resultMeta.getEnchantLevel(enchantment);
                        level = Math.max(level, existingLevel);
                    }
                    resultMeta.addEnchant(enchantment, level, true);
                }

                result.setItemMeta(resultMeta);
                event.setResult(result);
            }
        }
    }
}