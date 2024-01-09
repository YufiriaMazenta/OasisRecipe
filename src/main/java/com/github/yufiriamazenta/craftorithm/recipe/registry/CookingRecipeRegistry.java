package com.github.yufiriamazenta.craftorithm.recipe.registry;

import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CookingRecipeRegistry extends UnlockableRecipeRegistry {

    protected RecipeChoice ingredient;
    protected int time;
    protected float exp;
    protected CookingBlock cookingBlock;


    public CookingRecipeRegistry(@NotNull String group, @NotNull NamespacedKey namespacedKey, @NotNull ItemStack result) {
        super(group, namespacedKey, result);
        this.time = 200;
        this.exp = 0;
    }

    @Override
    public void register() {
        CookingRecipe<?> cookingRecipe = generateCookingRecipe();
        RecipeManager.INSTANCE.regRecipe(group, cookingRecipe, RecipeType.COOKING);
        RecipeManager.INSTANCE.recipeUnlockMap().put(namespacedKey, unlock);
    }

    @Override
    public RecipeType recipeType() {
        return RecipeType.COOKING;
    }

    protected CookingRecipe<?> generateCookingRecipe() {
        Objects.requireNonNull(namespacedKey, "Recipe key cannot be null");
        Objects.requireNonNull(result, "Recipe key cannot be null");
        Objects.requireNonNull(ingredient, "Recipe ingredient cannot be null");
        CookingRecipe<?> cookingRecipe;
        switch (cookingBlock) {
            case FURNACE:
            default:
                cookingRecipe = new FurnaceRecipe(namespacedKey, result, ingredient, exp, time);
                break;
            case SMOKER:
                cookingRecipe = new SmokingRecipe(namespacedKey, result, ingredient, exp, time);
                break;
            case BLAST_FURNACE:
                cookingRecipe = new BlastingRecipe(namespacedKey, result, ingredient, exp, time);
                break;
            case CAMPFIRE:
                cookingRecipe = new CampfireRecipe(namespacedKey, result, ingredient, exp, time);
                break;
        }
        cookingRecipe.setGroup(group);
        return cookingRecipe;
    }

    public RecipeChoice source() {
        return ingredient;
    }

    public CookingRecipeRegistry setIngredient(RecipeChoice source) {
        this.ingredient = source;
        return this;
    }

    public int time() {
        return time;
    }

    public CookingRecipeRegistry setTime(int time) {
        this.time = time;
        return this;
    }

    public float exp() {
        return exp;
    }

    public CookingRecipeRegistry setExp(float exp) {
        this.exp = exp;
        return this;
    }

    public CookingBlock cookingBlock() {
        return cookingBlock;
    }

    public CookingRecipeRegistry setCookingBlock(String cookingBlock) {
        this.cookingBlock = CookingBlock.valueOf(cookingBlock.toUpperCase());
        return this;
    }

    public CookingRecipeRegistry setCookingBlock(CookingBlock cookingBlock) {
        this.cookingBlock = cookingBlock;
        return this;
    }

    public enum CookingBlock {
        FURNACE(Material.FURNACE),
        BLAST_FURNACE(Material.BLAST_FURNACE),
        SMOKER(Material.SMOKER),
        CAMPFIRE(Material.CAMPFIRE);

        private final Material blockMaterial;

        CookingBlock(Material blockMaterial) {
            this.blockMaterial = blockMaterial;
        }

        public Material blockMaterial() {
            return blockMaterial;
        }

    }


}
