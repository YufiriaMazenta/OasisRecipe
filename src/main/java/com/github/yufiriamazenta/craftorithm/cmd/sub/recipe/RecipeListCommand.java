package com.github.yufiriamazenta.craftorithm.cmd.sub.recipe;

import com.github.yufiriamazenta.craftorithm.cmd.sub.AbstractSubCommand;
import com.github.yufiriamazenta.craftorithm.menu.display.RecipeGroupListMenu;
import com.github.yufiriamazenta.craftorithm.menu.display.RecipeListMenu;
import com.github.yufiriamazenta.craftorithm.recipe.RecipeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static crypticlib.command.CommandManager.subcommand;

public final class RecipeListCommand extends AbstractSubCommand {

    public static final RecipeListCommand INSTANCE = new RecipeListCommand();
    private static final String CRAFTORITHM = "craftorithm", SERVER = "server";

    private RecipeListCommand() {
        super("list", "craftorithm.command.list");
        regSub(subcommand(SERVER)
            .setPermission("craftorithm.command.list.server")
            .setExecutor((sender, args) -> {
                new RecipeListMenu((Player) sender, RecipeManager.INSTANCE.serverRecipesCache().keySet()).openMenu();
                return true;
            }));
        regSub(subcommand(CRAFTORITHM)
            .setPermission("craftorithm.command.list.craftorithm")
            .setExecutor((sender, arg) -> {
                new RecipeGroupListMenu((Player) sender).openMenu();
                return true;
            }));
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!checkSenderIsPlayer(sender)) {
            return true;
        }
        if (args.isEmpty()) {
            args = new ArrayList<>(Collections.singletonList(CRAFTORITHM));
        }
        return super.onCommand(sender, args);
    }

}
