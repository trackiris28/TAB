package dev.iris.tAB.config;

import dev.iris.tAB.TAB;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private static final long DEFAULT_UPDATE_TICKS = 20L;
    private static final long MIN_UPDATE_TICKS = 1L;

    private final TAB plugin;
    private List<String> header = List.of();
    private List<String> footer = List.of();
    private long updateTicks = DEFAULT_UPDATE_TICKS;
    private boolean enabled = true;

    public ConfigManager(TAB plugin) {
        this.plugin = plugin;
        load();
    }

    public FileConfiguration get() {
        return plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<String> getFooter() {
        return footer;
    }

    public long getUpdateTicks() {
        return updateTicks;
    }

    private void load() {
        FileConfiguration config = get();

        enabled = config.getBoolean("tab.enabled", true);
        header = List.copyOf(config.getStringList("tab.header"));
        footer = List.copyOf(config.getStringList("tab.footer"));
        updateTicks = Math.max(MIN_UPDATE_TICKS, config.getLong("tab.update-ticks", DEFAULT_UPDATE_TICKS));
    }
}
