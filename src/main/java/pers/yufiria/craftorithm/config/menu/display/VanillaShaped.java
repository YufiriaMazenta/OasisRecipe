package pers.yufiria.craftorithm.config.menu.display;

import crypticlib.config.ConfigHandler;
import crypticlib.config.node.impl.bukkit.ConfigSectionConfig;
import crypticlib.config.node.impl.bukkit.StringConfig;
import crypticlib.config.node.impl.bukkit.StringListConfig;

import java.util.List;

@ConfigHandler(path = "menus/display/vanilla_shaped.yml")
public class VanillaShaped {

    public static final StringConfig TITLE = new StringConfig("title", "<translate:lang:recipe_type_name.vanilla_shaped>");
    public static final StringListConfig LAYOUT = new StringListConfig("layout", List.of("#########", "#ABC#####", "#DEF###R#", "#GHI#####", "#########"));
    public static final ConfigSectionConfig ICONS = new ConfigSectionConfig("icons");

}