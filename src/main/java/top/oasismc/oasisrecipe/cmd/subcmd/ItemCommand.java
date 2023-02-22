package top.oasismc.oasisrecipe.cmd.subcmd;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisrecipe.api.cmd.ISubCommand;
import top.oasismc.oasisrecipe.cmd.AbstractSubCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.item.ItemGiveCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.item.ItemSaveCommand;

import java.util.List;

public final class ItemCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new ItemCommand();

    private ItemCommand() {
        super("item");
        regSubCommand(ItemSaveCommand.INSTANCE);
        regSubCommand(ItemGiveCommand.INSTANCE);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            sendNotEnoughCmdParamMsg(sender, 2 - args.size());
            return true;
        }
        return super.onCommand(sender, args);
    }
}
