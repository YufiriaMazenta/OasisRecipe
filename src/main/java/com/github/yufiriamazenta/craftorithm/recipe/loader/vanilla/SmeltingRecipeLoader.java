package com.github.yufiriamazenta.craftorithm.recipe.loader.vanilla;

import com.github.yufiriamazenta.craftorithm.Craftorithm;
import com.github.yufiriamazenta.craftorithm.item.ItemManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeLoader;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeType;
import com.github.yufiriamazenta.craftorithm.recipe.SimpleRecipeTypes;
import com.github.yufiriamazenta.craftorithm.recipe.exception.RecipeLoadException;
import com.github.yufiriamazenta.craftorithm.recipe.util.RecipeChoiceParser;
import crypticlib.MinecraftVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum SmeltingRecipeLoader implements RecipeLoader<CookingRecipe<?>> {

    INSTANCE;
    private final List<RecipeType> smeltingTypes = Arrays.asList(
        SimpleRecipeTypes.VANILLA_SMELTING_FURNACE,
        SimpleRecipeTypes.VANILLA_SMELTING_BLAST,
        SimpleRecipeTypes.VANILLA_SMELTING_SMOKER,
        SimpleRecipeTypes.VANILLA_SMELTING_CAMPFIRE
    );

    @Override
    public @NotNull CookingRecipe<?> loadRecipe(String recipeKey, ConfigurationSection recipeConfig) {
        try {
            String recipeTypeId = recipeConfig.getString("type");
            RecipeType recipeType = RecipeManager.INSTANCE.getRecipeType(recipeTypeId);
            if (recipeType == null || !smeltingTypes.contains(recipeType)) {
                throw new RecipeLoadException(recipeTypeId + " is not a smelting recipe.");
            }
            String resultId = recipeConfig.getString("result");
            ItemStack result = ItemManager.INSTANCE.matchItem(resultId);
            String ingredientId = recipeConfig.getString("ingredient");
            RecipeChoice ingredient = RecipeChoiceParser.parseChoice(ingredientId);
            NamespacedKey key = new NamespacedKey(Craftorithm.instance(), recipeKey);
            float exp = (float) recipeConfig.getDouble("exp", 0);
            int time;
            CookingRecipe<?> recipe;
            switch (recipeType) {
                case SimpleRecipeTypes.VANILLA_SHAPED -> {
                    time = recipeConfig.getInt("time", 200);
                    recipe = new FurnaceRecipe(key, result, ingredient, exp, time);
                }
                case SimpleRecipeTypes.VANILLA_SMELTING_BLAST -> {
                    time = recipeConfig.getInt("time", 100);
                    recipe = new BlastingRecipe(key, result, ingredient, exp, time);
                }
                case SimpleRecipeTypes.VANILLA_SMELTING_SMOKER -> {
                    time = recipeConfig.getInt("time", 100);
                    recipe = new SmokingRecipe(key, result, ingredient, exp, time);
                }
                case SimpleRecipeTypes.VANILLA_SMELTING_CAMPFIRE -> {
                    time = recipeConfig.getInt("time", 100);
                    recipe = new CampfireRecipe(key, result, ingredient, exp, time);
                }
                default -> throw new RecipeLoadException(recipeTypeId + " is not a smelting recipe.");
            }
            String group = recipeConfig.getString("group");
            if (group != null) {
                recipe.setGroup(group);
            }
            if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_19_3)) {
                if (recipeConfig.contains("recipe_book_category")) {
                    String categoryStr = Objects.requireNonNull(recipeConfig.getString("recipe_book_category")).toUpperCase();
                    CookingBookCategory category = CookingBookCategory.valueOf(categoryStr);
                    recipe.setCategory(category);
                }
            }
            return recipe;
        } catch (Throwable throwable) {
            throw new RecipeLoadException(throwable);
        }
    }

}
