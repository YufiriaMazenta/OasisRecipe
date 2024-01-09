package com.github.yufiriamazenta.craftorithm.menu.display;

import com.github.yufiriamazenta.craftorithm.config.Languages;
import com.github.yufiriamazenta.craftorithm.menu.editor.RecipeGroupEditor;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeGroup;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import crypticlib.chat.TextProcessor;
import crypticlib.ui.display.Icon;
import crypticlib.ui.menu.Menu;
import crypticlib.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecipeGroupListMenu extends Menu {

    private int page;
    private int maxPage;
    private List<Map.Entry<String, ItemStack>> recipeGroupResultList;

    public RecipeGroupListMenu(Player player) {
        super(player);
        recipeGroupResultList = new CopyOnWriteArrayList<>();
        refreshRecipeResults();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        resetIcons();
        if (openedInventory == null) {
            openedInventory = Bukkit.createInventory(
                this,
                54,
                TextProcessor.color(Languages.MENU_RECIPE_GROUP_LIST_TITLE.value(player))
            );
        }
        refreshInventory();

        return openedInventory;
    }

    public void nextPage() {
        setPage(Math.min(page + 1, maxPage - 1)).resetIcons();
        refreshInventory();
    }

    public void previousPage() {
        setPage(Math.max(page - 1, 0)).resetIcons();
        refreshInventory();
    }

    public RecipeGroupListMenu refreshRecipeResults() {
        Map<String, ItemStack> recipeResultMap = new HashMap<>();
        RecipeManager.INSTANCE.recipeGroupMap().forEach(
            (groupName, recipeGroup) -> {
                if (recipeGroup.groupRecipeKeyMap().isEmpty())
                    return;
                NamespacedKey randomRecipeKey = recipeGroup.groupRecipeKeyMap().get(
                    new ArrayList<>(recipeGroup.groupRecipeKeyMap().keySet()).get(0)
                );
                Recipe firstRecipe = RecipeManager.INSTANCE.getRecipe(randomRecipeKey);
                if (firstRecipe == null)
                    return;
                recipeResultMap.put(groupName, firstRecipe.getResult());
            }
        );
        recipeGroupResultList = new CopyOnWriteArrayList<>(recipeResultMap.entrySet());
        page = 0;
        int recipeGroupNum = recipeResultMap.size();
        if (recipeGroupNum % 45 == 0) {
            maxPage = recipeGroupNum / 45;
        } else {
            maxPage = recipeGroupNum / 45 + 1;
        }
        if (maxPage == 0)
            maxPage ++;
        recipeGroupResultList.sort((o1, o2) -> {
            int sortId = RecipeManager.INSTANCE.getRecipeGroupSortId(o1.getKey());
            int sortId2 = RecipeManager.INSTANCE.getRecipeGroupSortId(o2.getKey());
            return Integer.compare(sortId, sortId2);
        });
        return this;
    }

    public RecipeGroupListMenu resetIcons() {
        slotMap.clear();
        int []frameSlots = {45, 47, 48, 49, 50, 51, 53};
        Icon frameIcon = new Icon(
            Material.BLACK_STAINED_GLASS_PANE,
            TextProcessor.color(Languages.MENU_RECIPE_GROUP_LIST_ICON_FRAME.value(player)
        ));
        for (int frameSlot : frameSlots) {
            slotMap.put(frameSlot, frameIcon);
        }
        slotMap.put(46, new Icon(
            Material.PAPER,
            TextProcessor.color(Languages.MENU_RECIPE_GROUP_LIST_ICON_PREVIOUS.value(player)),
            event -> previousPage()
        ));
        slotMap.put(52, new Icon(
            Material.PAPER,
            TextProcessor.color(Languages.MENU_RECIPE_GROUP_LIST_ICON_NEXT.value(player)),
            event -> nextPage()
        ));
        int recipeSlot = page * 45;
        for (int invSlot = 0; invSlot < 45 && recipeSlot < recipeGroupResultList.size(); invSlot++, recipeSlot++) {
            slotMap.put(invSlot, wrapIcon(recipeSlot));
        }
        return this;
    }

    public RecipeGroupListMenu refreshInventory() {
        openedInventory.clear();
        for (Integer slot : slotMap.keySet()) {
            openedInventory.setItem(slot, slotMap.get(slot).display());
        }
        return this;
    }

    @NotNull
    private Icon wrapIcon(int recipeSlot) {
        ItemStack display = recipeGroupResultList.get(recipeSlot).getValue().clone();
        String recipeGroupName = recipeGroupResultList.get(recipeSlot).getKey();
        Icon icon = new Icon(display, event -> {
            switch (event.getClick()) {
                case RIGHT:
                case SHIFT_RIGHT:
                    if (!player.hasPermission("craftorithm.edit_recipe")) {
                        return;
                    }
                    RecipeGroup recipeGroup = RecipeManager.INSTANCE.getRecipeGroup(recipeGroupName);
                    if (recipeGroup == null) {
                        throw new IllegalArgumentException("Can not find recipe group " + recipeGroupName);
                    }
                    new RecipeGroupEditor(player, recipeGroup, this).openMenu();
                    break;
                case LEFT:
                case SHIFT_LEFT:
                default:
                    RecipeGroup recipeGroup1 = RecipeManager.INSTANCE.getRecipeGroup(recipeGroupName);
                    if (recipeGroup1 == null) {
                        throw new IllegalArgumentException("Can not find recipe group " + recipeGroupName);
                    }
                    if (recipeGroup1.groupRecipeKeyMap().size() < 2) {
                        new RecipeDisplayMenu(
                            player,
                            RecipeManager.INSTANCE.getRecipe(new ArrayList<>(recipeGroup1.groupRecipeKeyMap().values()).get(0)),
                            this
                        ).openMenu();
                    } else {
                        new RecipeListMenu(
                            player,
                            recipeGroup1.groupRecipeKeyMap().values(),
                            this
                        ).openMenu();
                    }
                    break;
            }
        });
        ItemUtil.setLore(icon.display(), Languages.MENU_RECIPE_GROUP_LIST_ICON_ELEMENTS_LORE.value(player));
        return icon;
    }

    public int page() {
        return page;
    }

    public RecipeGroupListMenu setPage(int page) {
        this.page = page;
        return this;
    }

}
