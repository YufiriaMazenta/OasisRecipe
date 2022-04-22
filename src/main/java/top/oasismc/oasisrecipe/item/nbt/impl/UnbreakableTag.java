package top.oasismc.oasisrecipe.item.nbt.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisrecipe.item.nbt.api.NBTTag;

public class UnbreakableTag implements NBTTag {

    private static final UnbreakableTag TAG = new UnbreakableTag();

    private final String key;

    private UnbreakableTag() { key = "unbreakable"; }

    @Override
    public void importTag(String itemName, ItemStack item, YamlConfiguration config) {
        config.set(itemName + ".unbreakable", item.getItemMeta().isUnbreakable());
    }

    @Override
    public void loadTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(config.getBoolean(itemName + ".unbreakable"));
        item.setItemMeta(meta);
    }

    @Override
    public String getKey() {
        return key;
    }

    public static NBTTag getInstance() {
        return TAG;
    }

}
