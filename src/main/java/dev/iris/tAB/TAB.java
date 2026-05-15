package dev.iris.tAB;

import dev.iris.tAB.commands.ReloadCommand;
import dev.iris.tAB.config.ConfigManager;
import dev.iris.tAB.listeners.PlayerJoinListener;
import dev.iris.tAB.ping.FakePingPacketManager;
import dev.iris.tAB.scheduler.TabTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TAB extends JavaPlugin {

    private static TAB instance;

    private ConfigManager configManager;
    private TabTask tabTask;
    private FakePingPacketManager fakePingPacketManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        tabTask = new TabTask(this);
        fakePingPacketManager = new FakePingPacketManager(this);

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(this),
                this
        );

        Objects.requireNonNull(getCommand("tabreload"), "tabreload command is missing from plugin.yml")
                .setExecutor(new ReloadCommand(this));

        tabTask.start();
        fakePingPacketManager.refreshOnlinePlayers();

        getLogger().info("TAB Enabled");
    }

    @Override
    public void onDisable() {
        if (tabTask != null) {
            tabTask.stop();
        }

        if (fakePingPacketManager != null) {
            fakePingPacketManager.uninjectOnlinePlayers();
        }

        instance = null;

        getLogger().info("TAB Disabled");
    }

    public static TAB getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TabTask getTabTask() {
        return tabTask;
    }

    public FakePingPacketManager getFakePingPacketManager() {
        return fakePingPacketManager;
    }
}
