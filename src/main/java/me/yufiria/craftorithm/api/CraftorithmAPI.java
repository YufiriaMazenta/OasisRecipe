package me.yufiria.craftorithm.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import me.yufiria.craftorithm.item.ItemManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CraftorithmAPI {

    INSTANCE;

    private final Map<Plugin, List<Recipe>> pluginRecipeMap = new HashMap<>();

    public ItemStack getOasisRecipeItem(String itemName) {
        return ItemManager.getCraftorithmItem(itemName);
    }

    public Map<Plugin, List<Recipe>> getPluginRegRecipeMap() {
        return Collections.unmodifiableMap(pluginRecipeMap);
    }

    public void regRecipes(Plugin plugin, List<Recipe> recipes) {
        pluginRecipeMap.put(plugin, recipes);
    }

}