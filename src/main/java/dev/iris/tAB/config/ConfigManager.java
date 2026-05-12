package dev.iris.tAB.config;

import dev.iris.tAB.TAB;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final TAB plugin;

    public ConfigManager(TAB plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration get() {
        return plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public List<String> getHeader() {
        return get().getStringList("tab.header");
    }

    public List<String> getFooter() {
        return get().getStringList("tab.footer");
    }

    public long getUpdateTicks() {
        return get().getLong("tab.update-ticks");
    }
}
