package com.github.yufiriamazenta.craftorithm.cmd.sub;

import com.github.yufiriamazenta.craftorithm.recipe.RecipeFactory;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeType;
import com.github.yufiriamazenta.craftorithm.util.CollectionsUtil;
import com.github.yufiriamazenta.craftorithm.util.ItemUtils;
import com.github.yufiriamazenta.craftorithm.util.LangUtil;
import crypticlib.CrypticLib;
import crypticlib.config.impl.YamlConfigWrapper;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import crypticlib.ui.menu.StoredMenu;
import crypticlib.util.FileUtil;
import crypticlib.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CreateRecipeCommand extends AbstractSubCommand {

    public static final CreateRecipeCommand INSTANCE = new CreateRecipeCommand();
    private final List<String> recipeTypeList;
    private final Pattern recipeNamePattern = Pattern.compile("[a-z0-9/._-]+");

    private CreateRecipeCommand() {
        super("create", "craftorithm.command.create");
        recipeTypeList = Arrays.stream(RecipeType.values()).map(RecipeType::name).map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        List<String> unsupportedRecipeTypeList = new ArrayList<>();
        unsupportedRecipeTypeList.add("random_cooking");
        unsupportedRecipeTypeList.add("unknown");
        if (CrypticLib.minecraftVersion() < 11400) {
            unsupportedRecipeTypeList.add("stone_cutting");
            unsupportedRecipeTypeList.add("smithing");
            unsupportedRecipeTypeList.add("cooking");
        }
        if (!RecipeManager.supportPotionMix()) {
            unsupportedRecipeTypeList.add("potion");
        }
        recipeTypeList.removeAll(unsupportedRecipeTypeList);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!checkSenderIsPlayer(sender))
            return true;
        if (args.size() < 2) {
            sendNotEnoughCmdParamMsg(sender, 2 - args.size());
            return true;
        }
        String recipeTypeStr = args.get(0).toLowerCase(Locale.ROOT);
        if (!recipeTypeList.contains(recipeTypeStr)) {
            LangUtil.sendLang(sender, "command.create.unsupported_recipe_type");
            return true;
        }
        String recipeName = args.get(1);
        Matcher matcher = recipeNamePattern.matcher(recipeName);
        if (!matcher.matches()) {
            LangUtil.sendLang(sender, "command.create.unsupported_recipe_name");
            return true;
        }
        if (RecipeManager.recipeGroupMap().containsKey(recipeName) || RecipeManager.potionMixGroupMap().containsKey(recipeName)) {
            LangUtil.sendLang(sender, "command.create.name_used");
            return true;
        }
        RecipeType recipeType = RecipeType.valueOf(recipeTypeStr.toUpperCase(Locale.ROOT));
        switch (recipeType) {
            case SHAPED:
            case SHAPELESS:
                openCraftingRecipeCreator((Player) sender, recipeType, recipeName);
                break;
            case COOKING:
                openCookingRecipeCreator((Player) sender, recipeType, recipeName);
                break;
            case SMITHING:
                openSmithingRecipeCreator((Player) sender, recipeType, recipeName);
                break;
            case STONE_CUTTING:
                openStoneCuttingRecipeCreator((Player) sender, recipeType, recipeName);
                break;
            case POTION:
                openPotionMixCreator((Player) sender, recipeType, recipeName);
                break;
            default:
                LangUtil.sendLang(sender, "command.create.unsupported_recipe_type");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            List<String> tabList = new ArrayList<>(recipeTypeList);
            filterTabList(tabList, args.get(0));
            return tabList;
        }
        return Collections.singletonList("<recipe_name>");
    }

    private void openCraftingRecipeCreator(Player player, RecipeType recipeType, String recipeName) {
        StoredMenu craftingRecipeCreator = new StoredMenu(player, new MenuDisplay(
            LangUtil.langMsg("menu.recipe_creator.title")
                .replace("<recipe_type>", recipeType.name())
                .replace("<recipe_name>", recipeName),
            new MenuLayout(Arrays.asList(
                "#########",
                "#   #***#",
                "#   A* *#",
                "#   #***#",
                "#########"
            ), () -> {
                Map<Character, Icon> layoutMap = new HashMap<>();
                layoutMap.put('#', getFrameIcon());
                layoutMap.put('*', getResultFrameIcon());
                layoutMap.put('A', new Icon(
                    Material.CRAFTING_TABLE,
                    LangUtil.langMsg("menu.recipe_creator.icon.confirm"),
                    event -> {
                        StoredMenu creator = (StoredMenu) Objects.requireNonNull(event.getClickedInventory()).getHolder();
                        Map<Integer, ItemStack> storedItems = Objects.requireNonNull(creator).storedItems();
                        ItemStack result = storedItems.get(24);
                        if (ItemUtil.isAir(result)) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_result");
                            return;
                        }
                        String resultName = ItemUtils.getItemName(result, false);
                        int[] sourceSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
                        List<String> sourceList = new ArrayList<>();
                        for (int slot : sourceSlots) {
                            ItemStack source = storedItems.get(slot);
                            if (ItemUtil.isAir(source)) {
                                sourceList.add("");
                                continue;
                            }
                            String sourceName = ItemUtils.getItemName(source, true);
                            sourceList.add(sourceName);
                        }
                        YamlConfigWrapper recipeConfig = createRecipeConfig(recipeName);
                        switch (recipeType) {
                            case SHAPED:
                                List<String> shape = new ArrayList<>(Arrays.asList("abc", "def", "ghi"));
                                Map<Character, String> itemNameMap = new HashMap<>();
                                char[] tmp = "abcdefghi".toCharArray();
                                for (int i = 0; i < sourceList.size(); i++) {
                                    if (sourceList.get(i).isEmpty()) {
                                        continue;
                                    }
                                    itemNameMap.put(tmp[i], sourceList.get(i));
                                }
                                //删除无映射的字符
                                for (int i = 0; i < shape.size(); i++) {
                                    String s = shape.get(i);
                                    for (char c : s.toCharArray()) {
                                        if (!itemNameMap.containsKey(c)) {
                                            s = s.replace(c, ' ');
                                        }
                                    }
                                    shape.set(i, s);
                                }
                                shape.removeIf(s -> s.trim().isEmpty());
                                recipeConfig.set("type", "shaped");
                                recipeConfig.set("shape", shape);
                                recipeConfig.set("source", itemNameMap);
                                break;
                            case SHAPELESS:
                                sourceList.removeIf(String::isEmpty);
                                recipeConfig.set("type", "shapeless");
                                recipeConfig.set("source", sourceList);
                                break;
                        }
                        recipeConfig.set("result", resultName);
                        recipeConfig.saveConfig();
                        recipeConfig.reloadConfig();
                        Recipe[] recipes = RecipeFactory.newRecipe(recipeConfig.config(), recipeName);
                        System.out.println(Arrays.toString(recipes));
                        RecipeManager.regRecipes(recipeName, Arrays.asList(recipes), recipeConfig);
                        RecipeManager.recipeConfigWrapperMap().put(recipeName, recipeConfig);
                        event.getWhoClicked().closeInventory();
                        sendSuccessMsg(event.getWhoClicked(), recipeType, recipeName);
                    })
                );
                return layoutMap;
            }))
        );
        craftingRecipeCreator.openMenu();
    }

    private void openCookingRecipeCreator(Player player, RecipeType recipeType, String recipeName) {
        StoredMenu cookingRecipeCreator = new StoredMenu(player, new MenuDisplay(
            LangUtil.langMsg("menu.recipe_creator.title")
                .replace("<recipe_type>", recipeType.name())
                .replace("<recipe_name>", recipeName),
            new MenuLayout(Arrays.asList(
                "#########",
                "#***#%%%#",
                "#* *A% %#",
                "#***#%%%#",
                "##BC#DE##"
            ), () -> {
                Map<Character, Icon> layoutMap = new HashMap<>();
                layoutMap.put('#', getFrameIcon());
                layoutMap.put('%', getResultFrameIcon());
                layoutMap.put('*', new Icon(Material.CYAN_STAINED_GLASS_PANE, LangUtil.langMsg("menu.recipe_creator.icon.cooking_frame")));
                layoutMap.put('A', new Icon(
                    Material.FURNACE,
                    LangUtil.langMsg("menu.recipe_creator.icon.confirm"),
                    event -> {
                        StoredMenu creator = (StoredMenu) Objects.requireNonNull(event.getClickedInventory()).getHolder();
                        ItemStack source = Objects.requireNonNull(creator).storedItems().get(20);
                        ItemStack result = creator.storedItems().get(24);
                        if (ItemUtil.isAir(source)) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_source");
                            return;
                        }
                        if (ItemUtil.isAir(result)) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_result");
                            return;
                        }
                        String sourceName = ItemUtils.getItemName(source, true);
                        String resultName = ItemUtils.getItemName(result, false);
                        YamlConfigWrapper recipeConfig = createRecipeConfig(recipeName);
                        recipeConfig.set("type", "cooking");
                        recipeConfig.set("result", resultName);
                        recipeConfig.set("multiple", true);
                        List<Map<String, String>> sourceList = new ArrayList<>();
                        int[] toggleSlots = {38, 39, 41, 42};
                        for (int toggleSlot : toggleSlots) {
                            ItemStack item = event.getClickedInventory().getItem(toggleSlot);
                            Material block = item.getType();
                            boolean toggle = !item.getEnchantments().isEmpty();
                            if (toggle) {
                                Map<String, String> sourceMap = new HashMap<>();
                                sourceMap.put("block", block.name().toLowerCase());
                                sourceMap.put("item", sourceName);
                                sourceList.add(sourceMap);
                            }
                        }
                        recipeConfig.set("source", sourceList);
                        recipeConfig.saveConfig();
                        recipeConfig.reloadConfig();
                        Recipe[] multipleRecipes = RecipeFactory.newMultipleRecipe(recipeConfig.config(), recipeName);
                        RecipeManager.regRecipes(recipeName, Arrays.asList(multipleRecipes), recipeConfig);
                        RecipeManager.recipeConfigWrapperMap().put(recipeName, recipeConfig);
                        event.getWhoClicked().closeInventory();
                        sendSuccessMsg(event.getWhoClicked(), recipeType, recipeName);
                    })
                );
                layoutMap.put('B', new Icon(
                    Material.FURNACE,
                    LangUtil.langMsg("menu.recipe_creator.icon.furnace_toggle"),
                    event -> setIconGlowing(event.getSlot(), event)
                ));
                layoutMap.put('C', new Icon(
                    Material.BLAST_FURNACE,
                    LangUtil.langMsg("menu.recipe_creator.icon.blasting_toggle"),
                    event -> setIconGlowing(event.getSlot(), event)
                ));
                layoutMap.put('D', new Icon(
                    Material.SMOKER,
                    LangUtil.langMsg("menu.recipe_creator.icon.smoking_toggle"),
                    event -> setIconGlowing(event.getSlot(), event)
                ));
                layoutMap.put('E', new Icon(
                    Material.CAMPFIRE,
                    LangUtil.langMsg("menu.recipe_creator.icon.campfire_toggle"),
                    event -> setIconGlowing(event.getSlot(), event)
                ));
                return layoutMap;
            })
        ));
        cookingRecipeCreator.openMenu();
    }

    private void openSmithingRecipeCreator(Player player, RecipeType recipeType, String recipeName) {
        StoredMenu smithingCreator = new StoredMenu(player, new MenuDisplay(
            LangUtil.langMsg("menu.recipe_creator.title")
                .replace("<recipe_type>", recipeType.name())
                .replace("<recipe_name>", recipeName),
            new MenuLayout(
                () -> {
                    if (CrypticLib.minecraftVersion() < 12000) {
                        return Arrays.asList(
                            "#########",
                            "#***#%%%#",
                            "# * A% %#",
                            "#***#%%%#",
                            "#########"
                        );
                    } else {
                        return Arrays.asList(
                            "#########",
                            "#***#%%%#",
                            "#   A% %#",
                            "#***#%%%#",
                            "#########"
                        );
                    }
                },
                () -> {
                    Map<Character, Icon> layoutMap = new HashMap<>();
                    layoutMap.put('#', getFrameIcon());
                    layoutMap.put('*', new Icon(Material.CYAN_STAINED_GLASS_PANE, LangUtil.langMsg("menu.recipe_creator.icon.smithing_frame")));
                    layoutMap.put('%', getResultFrameIcon());
                    layoutMap.put('A', new Icon(
                        Material.SMITHING_TABLE,
                        LangUtil.langMsg("menu.recipe_creator.icon.confirm"),
                        event -> {
                            StoredMenu creator = (StoredMenu) Objects.requireNonNull(event.getClickedInventory()).getHolder();
                            ItemStack result = Objects.requireNonNull(creator).storedItems().get(24);
                            if (ItemUtil.isAir(result)) {
                                LangUtil.sendLang(event.getWhoClicked(), "command.create.null_result");
                                return;
                            }
                            String resultName = ItemUtils.getItemName(result, false);
                            ItemStack base, addition, template;
                            String baseName, additionName, templateName = null;
                            if (CrypticLib.minecraftVersion() < 12000) {
                                base = creator.storedItems().get(19);
                                addition = creator.storedItems().get(21);
                            } else {
                                template = creator.storedItems().get(19);
                                base = creator.storedItems().get(20);
                                addition = creator.storedItems().get(21);
                                templateName = ItemUtils.getItemName(template, true);
                            }
                            baseName = ItemUtils.getItemName(base, true);
                            additionName = ItemUtils.getItemName(addition, true);
                            YamlConfigWrapper recipeConfig = createRecipeConfig(recipeName);
                            recipeConfig.set("result", resultName);
                            recipeConfig.set("source.base", baseName);
                            recipeConfig.set("source.addition", additionName);
                            recipeConfig.set("type", "smithing");
                            if (CrypticLib.minecraftVersion() >= 12000) {
                                recipeConfig.set("source.type", "transform");
                                recipeConfig.set("source.template", templateName);
                            }
                            recipeConfig.saveConfig();
                            recipeConfig.reloadConfig();
                            Recipe[] recipes = RecipeFactory.newRecipe(recipeConfig.config(), recipeName);
                            RecipeManager.regRecipes(recipeName, Arrays.asList(recipes), recipeConfig);
                            RecipeManager.recipeConfigWrapperMap().put(recipeName, recipeConfig);
                            event.getWhoClicked().closeInventory();
                            sendSuccessMsg(event.getWhoClicked(), recipeType, recipeName);
                        })
                    );
                    return layoutMap;
                }
            ))
        );
        smithingCreator.openMenu();
    }

    private void openStoneCuttingRecipeCreator(Player player, RecipeType recipeType, String recipeName) {
        StoredMenu stoneCuttingCreator = new StoredMenu(player, new MenuDisplay(
            LangUtil.langMsg("menu.recipe_creator.title")
                .replace("<recipe_type>", recipeType.name())
                .replace("<recipe_name>", recipeName),
            new MenuLayout(Arrays.asList(
                "#########",
                "#       #",
                "####A####",
                "#       #",
                "#########"
            ), () -> {
                Map<Character, Icon> layoutMap = new HashMap<>();
                layoutMap.put('#', getFrameIcon());
                layoutMap.put('A', new Icon(Material.STONECUTTER, LangUtil.langMsg("menu.recipe_creator.icon.confirm"),
                    event -> {
                        StoredMenu creator = (StoredMenu) event.getClickedInventory().getHolder();
                        List<String> sourceList = new ArrayList<>();
                        List<String> resultList = new ArrayList<>();
                        for (int i = 10; i < 17; i++) {
                            ItemStack source = Objects.requireNonNull(creator).storedItems().get(i);
                            if (ItemUtil.isAir(source))
                                continue;
                            sourceList.add(ItemUtils.getItemName(source, true));
                        }
                        for (int i = 28; i < 35; i++) {
                            ItemStack result = creator.storedItems().get(i);
                            if (ItemUtil.isAir(result))
                                continue;
                            resultList.add(ItemUtils.getItemName(result, false));
                        }
                        if (sourceList.isEmpty()) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_source");
                            return;
                        }
                        if (resultList.isEmpty()) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_result");
                            return;
                        }
                        YamlConfigWrapper recipeConfig = createRecipeConfig(recipeName);
                        recipeConfig.set("multiple", true);
                        recipeConfig.set("result", resultList);
                        recipeConfig.set("type", "stone_cutting");
                        recipeConfig.set("source", sourceList);
                        recipeConfig.saveConfig();
                        recipeConfig.reloadConfig();
                        Recipe[] recipes = RecipeFactory.newMultipleRecipe(recipeConfig.config(), recipeName);
                        RecipeManager.regRecipes(recipeName, Arrays.asList(recipes), recipeConfig);
                        RecipeManager.recipeConfigWrapperMap().put(recipeName, recipeConfig);
                        event.getWhoClicked().closeInventory();
                        sendSuccessMsg(event.getWhoClicked(), recipeType, recipeName);
                    })
                );
                return layoutMap;
            })
        ));
        stoneCuttingCreator.openMenu();
    }

    private void openPotionMixCreator(Player player, RecipeType recipeType, String recipeName) {
        StoredMenu potionMixCreator = new StoredMenu(player, new MenuDisplay(
            LangUtil.langMsg("menu.recipe_creator.title")
                .replace("<recipe_type>", recipeType.name())
                .replace("<recipe_name>", recipeName),
            new MenuLayout(Arrays.asList(
                "#########",
                "#***#%%%#",
                "# * A% %#",
                "#***#%%%#",
                "#########"
            ), () -> {
                Map<Character, Icon> layoutMap = new HashMap<>();
                layoutMap.put('#', getFrameIcon());
                layoutMap.put('%', getResultFrameIcon());
                layoutMap.put('*', new Icon(Material.CYAN_STAINED_GLASS_PANE, LangUtil.langMsg("menu.recipe_creator.icon.potion_frame")));
                layoutMap.put('A', new Icon(Material.BREWING_STAND, LangUtil.langMsg("menu.recipe_creator.icon.confirm"),
                    event -> {
                        StoredMenu creator = (StoredMenu) event.getClickedInventory().getHolder();
                        ItemStack result = Objects.requireNonNull(creator).storedItems().get(24);
                        ItemStack input = creator.storedItems().get(19);
                        ItemStack ingredient = creator.storedItems().get(21);
                        if (ItemUtil.isAir(result)) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_result");
                            return;
                        }
                        if (ItemUtil.isAir(ingredient) || ItemUtil.isAir(input)) {
                            LangUtil.sendLang(event.getWhoClicked(), "command.create.null_source");
                            return;
                        }
                        String resultName = ItemUtils.getItemName(result, false);
                        String inputName = ItemUtils.getItemName(input, true);
                        String ingredientName = ItemUtils.getItemName(ingredient, true);
                        YamlConfigWrapper recipeConfig = createRecipeConfig(recipeName);
                        recipeConfig.set("type", "potion");
                        recipeConfig.set("source.input", inputName);
                        recipeConfig.set("source.ingredient", ingredientName);
                        recipeConfig.set("result", resultName);
                        recipeConfig.saveConfig();
                        recipeConfig.reloadConfig();
                        Recipe[] recipes = RecipeFactory.newRecipe(recipeConfig.config(), recipeName);
                        RecipeManager.regPotionMix(recipeName, Arrays.asList(recipes), recipeConfig);
                        RecipeManager.recipeConfigWrapperMap().put(recipeName, recipeConfig);
                        event.getWhoClicked().closeInventory();
                        sendSuccessMsg(player, recipeType, recipeName);
                    })
                );
                return layoutMap;
            })
        ));
        potionMixCreator.openMenu();
    }

    private void sendSuccessMsg(HumanEntity receiver, RecipeType recipeType, String recipeName) {
        LangUtil.sendLang(
            receiver,
            "command.create.success",
            CollectionsUtil.newStringHashMap("<recipe_type>", recipeType.name(), "<recipe_name>", recipeName)
        );
    }

    private YamlConfigWrapper createRecipeConfig(String recipeName) {
        File recipeFile = new File(RecipeManager.recipeFileFolder(), recipeName + ".yml");
        if (!recipeFile.exists()) {
            FileUtil.createNewFile(recipeFile);
        }
        return new YamlConfigWrapper(recipeFile);
    }

    private void setIconGlowing(int slot, InventoryClickEvent event) {
        ItemStack display = event.getCurrentItem();
        if (ItemUtil.isAir(display))
            return;
        if (!display.containsEnchantment(Enchantment.MENDING)) {
            display.addUnsafeEnchantment(Enchantment.MENDING, 1);
            ItemMeta meta = display.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            display.setItemMeta(meta);
            event.getClickedInventory().setItem(slot, display);
        } else {
            display.removeEnchantment(Enchantment.MENDING);
            event.getClickedInventory().setItem(slot, display);
        }
    }

    private Icon getFrameIcon() {
        return new Icon(Material.BLACK_STAINED_GLASS_PANE, LangUtil.langMsg("menu.recipe_creator.icon.frame"));
    }

    private Icon getResultFrameIcon() {
        return new Icon(Material.LIME_STAINED_GLASS_PANE, LangUtil.langMsg("menu.recipe_creator.icon.result_frame"));
    }

}
