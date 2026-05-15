package dev.iris.tAB.config;

import dev.iris.tAB.TAB;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private static final long DEFAULT_UPDATE_TICKS = 20L;
    private static final long MIN_UPDATE_TICKS = 1L;
    private static final int DEFAULT_FAKE_PING_BARS = 5;
    private static final int MIN_FAKE_PING_BARS = 1;
    private static final int MAX_FAKE_PING_BARS = 5;

    private final TAB plugin;
    private List<String> header = List.of();
    private List<String> footer = List.of();
    private long updateTicks = DEFAULT_UPDATE_TICKS;
    private boolean enabled = true;
    private int fakePingBars = DEFAULT_FAKE_PING_BARS;

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

    public int getFakePingBars() {
        return fakePingBars;
    }

    public int getFakePingLatency() {
        return switch (fakePingBars) {
            case 5 -> 0;
            case 4 -> 150;
            case 3 -> 300;
            case 2 -> 600;
            default -> 1000;
        };
    }

    private void load() {
        FileConfiguration config = get();

        enabled = config.getBoolean("tab.enabled", true);
        header = List.copyOf(config.getStringList("tab.header"));
        footer = List.copyOf(config.getStringList("tab.footer"));
        updateTicks = Math.max(MIN_UPDATE_TICKS, config.getLong("tab.update-ticks", DEFAULT_UPDATE_TICKS));
        fakePingBars = clamp(
                config.getInt("fake-ping-bars", DEFAULT_FAKE_PING_BARS),
                MIN_FAKE_PING_BARS,
                MAX_FAKE_PING_BARS
        );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
