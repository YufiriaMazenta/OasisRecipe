package com.github.yufiriamazenta.craftorithm.cmd.sub.recipe;

import com.github.yufiriamazenta.craftorithm.Craftorithm;
import com.github.yufiriamazenta.craftorithm.config.Languages;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeGroup;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.util.LangUtils;
import crypticlib.command.BukkitSubcommand;
import crypticlib.command.CommandInfo;
import crypticlib.perm.PermInfo;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 重构为只能删除本身插件的配方，取消其他插件配方移动到disable命令
 */
public final class RemoveRecipeCommand extends BukkitSubcommand {

    public static final RemoveRecipeCommand INSTANCE = new RemoveRecipeCommand();

    private RemoveRecipeCommand() {
        super(CommandInfo
            .builder("remove")
            .permission(new PermInfo("craftorithm.recipe.remove"))
            .usage("&r/craftorithm remove <recipe_name>")
            .build()
        );
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sendDescriptions(sender);
            return;
        }

        NamespacedKey recipeKey = new NamespacedKey(Craftorithm.instance(), args.get(0));
        if (RecipeManager.INSTANCE.removeCraftorithmRecipe(recipeKey, true)) {
            LangUtils.sendLang(sender, Languages.COMMAND_REMOVE_SUCCESS);
        }
        else
            LangUtils.sendLang(sender, Languages.COMMAND_REMOVE_NOT_EXIST);
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return RecipeManager.INSTANCE.craftorithmRecipes().keySet().stream().map(NamespacedKey::getKey).toList();
        }
        return Collections.emptyList();
    }



}
