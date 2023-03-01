package me.yufiria.craftorithm.recipe;

import me.yufiria.craftorithm.Craftorithm;
import me.yufiria.craftorithm.item.ItemManager;
import me.yufiria.craftorithm.recipe.builder.custom.AnvilRecipeBuilder;
import me.yufiria.craftorithm.recipe.builder.vanilla.*;
import me.yufiria.craftorithm.recipe.custom.AnvilRecipeItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.*;

import java.util.*;

public class RecipeFactory {

    public static Recipe shapedRecipe(YamlConfiguration config, String key) {
        Map<Character, RecipeChoice> recipeChoiceMap = getShapedRecipeChoiceMap(config.getConfigurationSection("source"));
        ItemStack result = getResultItem(config);
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);

        List<String> shapeStrList = config.getStringList("shape");
        if (shapeStrList.size() > 3) {
            shapeStrList = shapeStrList.subList(0, 3);
        }
        String[] shape = new String[shapeStrList.size()];
        shape = shapeStrList.toArray(shape);
        return ShapedRecipeBuilder.builder().key(namespacedKey).result(result).shape(shape).recipeChoiceMap(recipeChoiceMap).build();
    }

    public static Recipe[] multipleShapedRecipe(YamlConfiguration config, String key) {
        Map<Character, RecipeChoice> recipeChoiceMap = getShapedRecipeChoiceMap(config.getConfigurationSection("source"));
        ItemStack result = getResultItem(config);
        List<?> shapeList = config.getList("shape", new ArrayList<>());
        ShapedRecipe[] shapedRecipes = new ShapedRecipe[shapeList.size()];
        for (int i = 0; i < shapeList.size(); i++) {
            String fullKey = key + "." + i;
            NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
            List<String> shapeStrList = (List<String>) shapeList.get(i);
            if (shapeStrList.size() > 3) {
                shapeStrList = shapeStrList.subList(0, 3);
            }
            String[] shape = new String[shapeStrList.size()];
            shape = shapeStrList.toArray(shape);
            shapedRecipes[i] = ShapedRecipeBuilder.builder().key(namespacedKey).result(result).shape(shape).recipeChoiceMap(recipeChoiceMap).build();
            shapedRecipes[i].setGroup(key);
        }
        return shapedRecipes;
    }

    public static Recipe shapelessRecipe(YamlConfiguration config, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);
        ItemStack result = getResultItem(config);
        List<String> itemStrList = config.getStringList("source");
        List<RecipeChoice> recipeChoiceList = new ArrayList<>();
        for (String itemStr : itemStrList) {
            recipeChoiceList.add(getRecipeChoice(itemStr));
        }
        return ShapelessRecipeBuilder.builder().key(namespacedKey).result(result).choiceList(recipeChoiceList).build();
    }

    public static Recipe[] multipleShapelessRecipe(YamlConfiguration config, String key) {
        ItemStack result = getResultItem(config);
        List<?> itemsList = config.getList("source", new ArrayList<>());
        ShapelessRecipe[] shapelessRecipes = new ShapelessRecipe[itemsList.size()];

        for (int i = 0; i < itemsList.size(); i++) {
            String fullKey = key + "." + i;
            NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
            List<String> itemStrList = (List<String>) itemsList.get(i);
            List<RecipeChoice> choiceList = new ArrayList<>();
            for (String itemStr : itemStrList) {
                choiceList.add(getRecipeChoice(itemStr));
            }
            shapelessRecipes[i] = ShapelessRecipeBuilder.builder().key(namespacedKey).result(result).choiceList(choiceList).build();
            shapelessRecipes[i].setGroup(key);
        }
        return shapelessRecipes;
    }

    public static Recipe cookingRecipe(YamlConfiguration config, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);
        ItemStack result = getResultItem(config);
        String choiceStr = config.getString("source.item", "");
        String cookingBlock = config.getString("source.block", "furnace");
        RecipeChoice source = getRecipeChoice(choiceStr);
        int exp = config.getInt("exp", 0);
        int time = config.getInt("time", 200);
        return CookingRecipeBuilder.builder().key(namespacedKey).result(result).block(cookingBlock).source(source).exp(exp).time(time).build();
    }

    public static Recipe[] multipleCookingRecipe(YamlConfiguration config, String key) {
        ItemStack result = getResultItem(config);
        int globalExp = config.getInt("exp", 0);
        int globalTime = config.getInt("time", 0);

        List<Map<?, ?>> sourceList = config.getMapList("source");
        CookingRecipe<?>[] cookingRecipes = new CookingRecipe[sourceList.size()];
        for (int i = 0; i < sourceList.size(); i++) {
            Map<?, ?> map = sourceList.get(i);
            String fullKey = key + "." + i;
            NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
            RecipeChoice source = getRecipeChoice((String) map.get("item"));
            String cookingBlock = (String) map.get("block");
            int exp = map.containsKey("exp") ? (Integer) map.get("exp") : globalExp;
            int time = map.containsKey("time") ? (Integer) map.get("time") : globalTime;
            cookingRecipes[i] = CookingRecipeBuilder.builder().key(namespacedKey).result(result).block(cookingBlock).source(source).exp(exp).time(time).build();
            cookingRecipes[i].setGroup(key);
        }
        return cookingRecipes;
    }

    public static Recipe smithingRecipe(YamlConfiguration config, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);
        ItemStack result = getResultItem(config);
        RecipeChoice base = getRecipeChoice(config.getString("source.base", ""));
        RecipeChoice addition = getRecipeChoice(config.getString("source.addition", ""));
        return SmithingRecipeBuilder.builder().key(namespacedKey).result(result).base(base).addition(addition).build();
    }

    public static Recipe[] multipleSmithingRecipe(YamlConfiguration config, String key) {
        ItemStack result = getResultItem(config);
        List<Map<?, ?>> sourceList = config.getMapList("source");
        SmithingRecipe[] smithingRecipes = new SmithingRecipe[sourceList.size()];
        for (int i = 0; i < sourceList.size(); i++) {
            Map<?, ?> map = sourceList.get(i);
            String fullKey = key + "." + i;
            NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
            RecipeChoice base = getRecipeChoice((String) map.get("base"));
            RecipeChoice addition = getRecipeChoice((String) map.get("addition"));
            smithingRecipes[i] = SmithingRecipeBuilder.builder().key(namespacedKey).result(result).base(base).addition(addition).build();
        }
        return smithingRecipes;
    }

    public static Recipe stoneCuttingRecipe(YamlConfiguration config, String key) {
        RecipeChoice choice = getRecipeChoice(config.getString("source", ""));
        ItemStack result;
        if (config.isList("result")) {
            List<String> resultList = config.getStringList("result");
            result = ItemManager.matchCraftorithmItem(resultList.get(0));
            for (int i = 1; i < resultList.size(); i++) {
                ItemStack result1 = ItemManager.matchCraftorithmItem(resultList.get(i));
                String fullKey = key + "." + i;
                NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
                RecipeManager.regRecipe(namespacedKey, StoneCuttingRecipeBuilder.builder().key(namespacedKey).result(result1).source(choice).build(), config);
            }
        } else {
            result = getResultItem(config);
        }
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);
        return StoneCuttingRecipeBuilder.builder().key(namespacedKey).result(result).source(choice).build();
    }

    public static Recipe[] multipleStoneCuttingRecipe(YamlConfiguration config, String key) {
        boolean resultIsList = config.isList("result");
        if (resultIsList) {
            List<String> resultList = config.getStringList("result");
            List<String> sourceList = config.getStringList("source");
            StonecuttingRecipe[] stonecuttingRecipes = new StonecuttingRecipe[resultList.size() * sourceList.size()];
            for (int i = 0; i < resultList.size(); i++) {
                ItemStack result = ItemManager.matchCraftorithmItem(resultList.get(i));
                for (int j = 0; j < sourceList.size(); j++) {
                    String fullKey = String.format(key + ".%d.%d", i, j);
                    NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
                    RecipeChoice source = getRecipeChoice(sourceList.get(j));
                    int index = i * sourceList.size() + j;
                    stonecuttingRecipes[index] = StoneCuttingRecipeBuilder.builder().key(namespacedKey).result(result).source(source).build();
                    stonecuttingRecipes[index].setGroup(key);
                }
            }
            return stonecuttingRecipes;
        } else {
            ItemStack result = getResultItem(config);
            List<String> sourceList = config.getStringList("source");
            StonecuttingRecipe[] stonecuttingRecipes = new StonecuttingRecipe[sourceList.size()];
            for (int i = 0; i < sourceList.size(); i++) {
                String fullKey = key + "." + i;
                NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), fullKey);
                RecipeChoice source = getRecipeChoice(sourceList.get(i));
                stonecuttingRecipes[i] = StoneCuttingRecipeBuilder.builder().key(namespacedKey).result(result).source(source).build();
                stonecuttingRecipes[i].setGroup(key);
            }
            return stonecuttingRecipes;
        }
    }

    public static Recipe anvilRecipe(YamlConfiguration config, String key) {
        ItemStack result = getResultItem(config);
        String baseStr = config.getString("source.base", "");
        ItemStack baseItem = ItemManager.matchCraftorithmItem(baseStr);
        AnvilRecipeItem base = new AnvilRecipeItem(baseItem, baseStr.contains(":"));
        String additionStr = config.getString("source.addition", "");
        ItemStack additionItem = ItemManager.matchCraftorithmItem(additionStr);
        AnvilRecipeItem addition = new AnvilRecipeItem(additionItem, additionStr.contains(":"));
        NamespacedKey namespacedKey = new NamespacedKey(Craftorithm.getInstance(), key);
        int costLevel = config.getInt("cost_level", 0);
        return AnvilRecipeBuilder.builder().key(namespacedKey).result(result).base(base).addition(addition).costLevel(costLevel).build();
    }

    private static Map<Character, RecipeChoice> getShapedRecipeChoiceMap(ConfigurationSection section) {
        Map<Character, RecipeChoice> recipeChoiceMap = new HashMap<>();
        if (section == null)
            return recipeChoiceMap;
        for (String key : section.getKeys(false)) {
            char keyWord = key.toCharArray()[0];
            String itemStr = section.getString(key, "");
            if (itemStr.length() < 1) {
                throw new IllegalArgumentException("Empty recipe ingredient: " + key);
            }
            recipeChoiceMap.put(keyWord, getRecipeChoice(itemStr));
        }
        return recipeChoiceMap;
    }

    private static ItemStack getResultItem(YamlConfiguration config) {
        String resultStr;
        if (config.getString("type", "shaped").equals("random_cooking")) {
            String tmpStr = config.getStringList("result").get(0);
            tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf(" "));
            resultStr = tmpStr;
        }
        else {
            resultStr = config.getString("result", "");
        }

        if (resultStr.length() < 1) {
            throw new IllegalArgumentException("Empty recipe result");
        }
        return ItemManager.matchCraftorithmItem(resultStr);
    }

    public static RecipeChoice getRecipeChoice(String itemStr) {
        if (itemStr.startsWith("items:")) {
            ItemStack item = ItemManager.matchCraftorithmItem(itemStr);
            return new RecipeChoice.ExactChoice(item);
        } else {
            Material material = Material.matchMaterial(itemStr);
            if (material == null) {
                throw new IllegalArgumentException(itemStr + " is a not exist item type");
            }
            return new RecipeChoice.MaterialChoice(material);
        }
    }

}
