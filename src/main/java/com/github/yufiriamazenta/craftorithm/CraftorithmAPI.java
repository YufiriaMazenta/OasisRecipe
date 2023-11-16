package com.github.yufiriamazenta.craftorithm;

import com.github.yufiriamazenta.craftorithm.arcenciel.ArcencielDispatcher;
import com.github.yufiriamazenta.craftorithm.item.ItemManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum CraftorithmAPI {

    INSTANCE;

    private final Map<String, List<Recipe>> pluginRecipeMap = new ConcurrentHashMap<>();
    private boolean loadedOtherPluginRecipe = false;

    public ItemStack getCraftorithmItem(String itemName) {
        return ItemManager.getCraftorithmItem(itemName);
    }

    public Map<String, List<Recipe>> getPluginRegRecipeMap() {
        return pluginRecipeMap;
    }

    public void regRecipes(String plugin, List<Recipe> recipes) {
        pluginRecipeMap.put(plugin, recipes);
    }

    public ArcencielDispatcher getArcencielDispatcher() {
        return ArcencielDispatcher.INSTANCE;
    }

    public RecipeType getRecipeType(Recipe recipe) {
        return RecipeManager.getRecipeType(recipe);
    }

    public void setLoadedOtherPluginRecipe(boolean loadedOtherPluginRecipe) {
        this.loadedOtherPluginRecipe = loadedOtherPluginRecipe;
    }

    public boolean isLoadedOtherPluginRecipe() {
        return loadedOtherPluginRecipe;
    }

}
