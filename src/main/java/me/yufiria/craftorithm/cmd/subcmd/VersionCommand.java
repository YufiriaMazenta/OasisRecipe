package me.yufiria.craftorithm.cmd.subcmd;

import me.yufiria.craftorithm.util.LangUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VersionCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new VersionCommand();

    private VersionCommand() {
        super("version", "craftorithm.command.version");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        LangUtil.sendMsg(sender, "command.version");
        return true;
    }
}
