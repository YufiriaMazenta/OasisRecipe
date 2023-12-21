package com.github.yufiriamazenta.craftorithm.item;

import com.github.yufiriamazenta.craftorithm.config.PluginConfigs;
import com.github.yufiriamazenta.craftorithm.item.impl.CraftorithmItemProvider;
import com.google.common.base.Preconditions;
import crypticlib.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ItemManager {

    INSTANCE;

    private final Map<String, ItemProvider> itemProviderMap;
    private final Map<String, ItemStack> cannotCraftItems;

    ItemManager() {
        itemProviderMap = new LinkedHashMap<>();
        cannotCraftItems = new ConcurrentHashMap<>();
        reloadCannotCraftItems();
    }

    public void retDefaultProviders() {
        regItemProvider(CraftorithmItemProvider.INSTANCE);
    }

    public void regItemProvider(ItemProvider itemProvider) {
        Preconditions.checkArgument(
            !itemProvider.namespace().equalsIgnoreCase(NamespacedKey.MINECRAFT),
            "Item provider cannot use namespace minecraft"
        );
        itemProviderMap.put(itemProvider.namespace(), itemProvider);
    }

    /**
     * 根据名字获取一个物品
     * @param itemKey 包含命名空间的名字
     * @return 获取到的物品，如果为空则为不存在此物品
     */
    public @NotNull ItemStack matchItem(String itemKey) {
        ItemStack item;
        int lastSpaceIndex = itemKey.lastIndexOf(" ");
        int amountScale = 1;
        if (lastSpaceIndex > 0) {
            amountScale = Integer.parseInt(itemKey.substring(lastSpaceIndex + 1));
            itemKey = itemKey.substring(0, lastSpaceIndex);
        }
        itemKey = itemKey.replace(" ", "");
        if (!itemKey.contains(":")) {
            return matchVanillaItem(itemKey, amountScale);
        }

        int index = itemKey.indexOf(":");
        String namespace = itemKey.substring(0, index);
        String name = itemKey.substring(index + 1);

        ItemProvider provider = itemProviderMap.get(namespace);
        if (provider == null) {
            return matchVanillaItem(itemKey, amountScale);
        }

        item = provider.getItem(name);
        if (item == null)
            throw new IllegalArgumentException("Can not found item " + name + " from provider: " + namespace);
        item.setAmount(item.getAmount() * amountScale);
        return item;
    }

    /**
     * 获取一个物品的完整名字,包含命名空间和id
     * @param item 传入的物品
     * @param ignoreAmount 是否忽略数量
     * @return 传入的物品名字
     */
    @Nullable
    public String matchItemName(ItemStack item, boolean ignoreAmount) {
        if (ItemUtil.isAir(item))
            return null;

        for (Map.Entry<String, ItemProvider> itemProviderEntry : itemProviderMap.entrySet()) {
            String tmpName = itemProviderEntry.getValue().getItemName(item, ignoreAmount);
            if (tmpName != null)
                return itemProviderEntry.getKey() + ":" + tmpName;
        }

        return item.getType().getKey().toString();
    }

    /**
     * 获取原版物品
     * @param itemKey 物品名字
     * @param amount 物品数量
     * @return 物品
     */
    public ItemStack matchVanillaItem(String itemKey, int amount) {
        Material material = Material.matchMaterial(itemKey);
        if (material == null)
            throw new IllegalArgumentException("Can not found item " + itemKey);
        return new ItemStack(material, amount);
    }

    public void reloadCannotCraftItems() {
        cannotCraftItems.clear();
        for (String itemName : PluginConfigs.CANNOT_CRAFT_ITEMS.value()) {
            try {
                ItemStack item = matchItem(itemName);
                cannotCraftItems.put(itemName, item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isCannotCraftItem(ItemStack itemStack) {
        boolean result = false;
        for (Map.Entry<String, ItemStack> cannotCraftItems : cannotCraftItems.entrySet()) {
            if (cannotCraftItems.getValue().isSimilar(itemStack)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
