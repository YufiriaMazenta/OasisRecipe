package pers.yufiria.craftorithm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;
import pers.yufiria.craftorithm.bstat.Metrics;
import pers.yufiria.craftorithm.config.Languages;
import pers.yufiria.craftorithm.config.PluginConfigs;
import pers.yufiria.craftorithm.exception.UnsupportedVersionException;
import pers.yufiria.craftorithm.listener.hook.OtherPluginsListenerManager;
import pers.yufiria.craftorithm.recipe.RecipeManager;
import pers.yufiria.craftorithm.util.LangUtils;
import pers.yufiria.craftorithm.util.UpdateChecker;
import crypticlib.BukkitPlugin;
import crypticlib.CrypticLib;
import crypticlib.CrypticLibBukkit;
import crypticlib.MinecraftVersion;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@AutoTask(
    rules = {
        @TaskRule(lifeCycle = LifeCycle.ACTIVE, priority = 2),
        @TaskRule(lifeCycle = LifeCycle.RELOAD)
    }
)
public final class Craftorithm extends BukkitPlugin implements Listener, BukkitLifeCycleTask {

    private static Craftorithm INSTANCE;

    public Craftorithm() {
        INSTANCE = this;
    }

    @Override
    public void enable() {
        if (MinecraftVersion.current().before(MinecraftVersion.V1_19_4)) {
            BukkitMsgSender.INSTANCE.info("&c[Craftorithm] Unsupported Version");
            throw new UnsupportedVersionException();
        }
        CrypticLib.DEBUG = PluginConfigs.DEBUG.value();
        loadBStat();

        UpdateChecker.pullUpdateCheckRequest(Bukkit.getConsoleSender());
    }

    @Override
    public void disable() {
        RecipeManager.INSTANCE.resetRecipes();
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        CrypticLibBukkit.scheduler().runTask(this, () -> {
            RecipeManager.INSTANCE.reloadRecipeManager();
            OtherPluginsListenerManager.INSTANCE.convertOtherPluginsListeners();
            LangUtils.info(Languages.LOAD_FINISH);
        });
    }

    private void loadBStat() {
        if (!PluginConfigs.BSTATS.value())
            return;
        Metrics metrics = new Metrics(this, 17821);
        metrics.addCustomChart(new Metrics.SingleLineChart("recipes", () -> RecipeManager.INSTANCE.getRecipeGroups().size()));
    }

    public static Craftorithm instance() {
        return INSTANCE;
    }

    public static CraftorithmAPI api() {
        return CraftorithmAPI.INSTANCE;
    }

    @Override
    public void run(Plugin plugin, LifeCycle lifeCycle) {
        if (lifeCycle == LifeCycle.ACTIVE) {
            CrypticLibBukkit.scheduler().runTask(this, () -> {
                RecipeManager.INSTANCE.reloadRecipeManager();
                OtherPluginsListenerManager.INSTANCE.convertOtherPluginsListeners();
                LangUtils.info(Languages.LOAD_FINISH);
            });
        } else {
            CrypticLib.DEBUG = PluginConfigs.DEBUG.value();
        }
    }

}