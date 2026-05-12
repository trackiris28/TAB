package dev.iris.tAB;

import dev.iris.tAB.commands.ReloadCommand;
import dev.iris.tAB.config.ConfigManager;
import dev.iris.tAB.listeners.PlayerJoinListener;
import dev.iris.tAB.scheduler.TabTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


public final class TAB extends JavaPlugin {

    private static TAB instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(),
                this
        );

        Objects.requireNonNull(getCommand("tabreload")).setExecutor(new ReloadCommand());

        new TabTask().start();

        getLogger().info("TAB Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TAB Disabled");
    }

    public static TAB getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}