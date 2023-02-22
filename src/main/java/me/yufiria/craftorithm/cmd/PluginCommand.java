package me.yufiria.craftorithm.cmd;

import me.yufiria.craftorithm.api.cmd.ISubCommand;
import me.yufiria.craftorithm.cmd.subcmd.ItemCommand;
import me.yufiria.craftorithm.cmd.subcmd.ReloadCommand;
import me.yufiria.craftorithm.cmd.subcmd.RemoveCommand;
import me.yufiria.craftorithm.cmd.subcmd.VersionCommand;
import me.yufiria.craftorithm.util.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import me.yufiria.craftorithm.util.MapUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum PluginCommand implements TabExecutor {

    INSTANCE;

    private final Map<String, ISubCommand> subCommandMap;

    PluginCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        regDefaultSubCommands();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() < 1) {
            LangUtil.sendMsg(sender, "command.not_enough_param", MapUtil.newHashMap("<number>", String.valueOf(1)));
            return true;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onCommand(sender, argList.subList(1, argList.size()));
        else {
            LangUtil.sendMsg(sender, "command.undefined_subcmd");
            return true;
        }
    }

    private void regDefaultSubCommands() {
        regSubCommand(ReloadCommand.INSTANCE);
        regSubCommand(VersionCommand.INSTANCE);
        regSubCommand(RemoveCommand.INSTANCE);
        regSubCommand(ItemCommand.INSTANCE);
    }

    public void regSubCommand(ISubCommand executor) {
        subCommandMap.put(executor.getSubCommand(), executor);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() <= 1) {
            List<String> returnList = new ArrayList<>(subCommandMap.keySet());
            returnList.removeIf(str -> !str.startsWith(args[0]));
            return returnList;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onTabComplete(sender, argList.subList(1, argList.size()));
        else
            return Collections.singletonList("");
    }

    public Map<String, ISubCommand> getSubCommandMap() {
        return subCommandMap;
    }


}