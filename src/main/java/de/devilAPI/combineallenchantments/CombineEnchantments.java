package de.devilAPI.combineallenchantments;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.nbt.NBTTagCompound; 
import net.minecraft.world.item.ItemStack; // Using fully qualified NMS ItemStack

import java.util.Map;

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

        // Convert to NMS ItemStack to directly modify NBT data
        net.minecraft.world.item.ItemStack nmsFirst = CraftItemStack.asNMSCopy(firstItem);
        net.minecraft.world.item.ItemStack nmsSecond = CraftItemStack.asNMSCopy(secondItem);

        // Get NBT tags from the NMS items
        NBTTagCompound firstTag = nmsFirst.getOrCreateTag(); // Accessing NBT data correctly
        NBTTagCompound secondTag = nmsSecond.getOrCreateTag();

        // Combine enchantments from both items
        if (secondTag != null && secondTag.contains("Enchantments")) {
            NBTTagCompound enchantments = secondTag.getCompound("Enchantments");
            firstTag.merge(enchantments); // Merging the enchantments correctly
        }

        // Set the combined tag back to the first item
        nmsFirst.setTag(firstTag); 

        // Convert back to Bukkit ItemStack and set as result
        ItemStack resultItem = CraftItemStack.asBukkitCopy(nmsFirst);
        event.setResult(resultItem);

        // Calculate repair cost
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
