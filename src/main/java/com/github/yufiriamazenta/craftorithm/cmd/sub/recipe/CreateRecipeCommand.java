package com.github.yufiriamazenta.craftorithm.cmd.sub.recipe;

import com.github.yufiriamazenta.craftorithm.config.Languages;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeType;
import com.github.yufiriamazenta.craftorithm.util.CommandUtils;
import com.github.yufiriamazenta.craftorithm.util.LangUtils;
import crypticlib.command.BukkitSubcommand;
import crypticlib.command.CommandInfo;
import crypticlib.perm.PermInfo;
import crypticlib.ui.menu.Menu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CreateRecipeCommand extends BukkitSubcommand {

    public static final CreateRecipeCommand INSTANCE = new CreateRecipeCommand();
    private final Pattern recipeNamePattern = Pattern.compile("^[a-z0-9._-]+$");
    private final Map<RecipeType, Function<Player, Menu>> recipeCreatorMap = new HashMap<>();

    private CreateRecipeCommand() {
        super(CommandInfo
            .builder("create")
            .permission(new PermInfo("craftorithm.command.create"))
            .usage("&r/craftorithm create <recipe_type> [recipe_name]")
            .build()
        );
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!CommandUtils.checkSenderIsPlayer(sender))
            return;
        if (args.isEmpty()) {
            sendDescriptions(sender);
            return;
        }
        String recipeTypeStr = args.get(0);
        String recipeName;
        if (args.size() < 2)
            recipeName = UUID.randomUUID().toString();
        else
            recipeName = args.get(1);

        Matcher matcher = recipeNamePattern.matcher(recipeName);
        if (!matcher.matches()) {
            LangUtils.sendLang(sender, Languages.COMMAND_CREATE_UNSUPPORTED_RECIPE_NAME);
            return;
        }
        if (RecipeManager.INSTANCE.containsRecipe(recipeName)) {
            LangUtils.sendLang(sender, Languages.COMMAND_CREATE_NAME_USED);
            return;
        }
        RecipeType recipeType = RecipeManager.INSTANCE.getRecipeType(recipeTypeStr);
        if (recipeType == null) {
            LangUtils.sendLang(sender, Languages.COMMAND_CREATE_UNSUPPORTED_RECIPE_TYPE);
            return;
        }
        Function<Player, Menu> creatorFunc = recipeCreatorMap.get(recipeType);
        if (creatorFunc == null) {
            LangUtils.sendLang(sender, Languages.COMMAND_CREATE_UNSUPPORTED_RECIPE_TYPE);
            return;
        }
        Player player = (Player) sender;
        creatorFunc.apply(player).openMenu();
    }

    @Override
    public List<String> tab(@NotNull CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return recipeCreatorMap.keySet().stream().map(RecipeType::typeId).toList();
        }
        return Collections.singletonList("<recipe_name>");
    }

    public void addRecipeCreator(RecipeType recipeType, Function<Player, Menu> creatorFunc) {
        recipeCreatorMap.put(recipeType, creatorFunc);
    }

}
