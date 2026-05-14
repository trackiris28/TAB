package dev.iris.tAB.scheduler;

import dev.iris.tAB.TAB;
import dev.iris.tAB.tab.TabManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabTask {

    private final TAB plugin;
    private ScheduledTask scheduledTask;

    public TabTask(TAB plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();

        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }

        scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> updateOnlinePlayers(),
                20L,
                plugin.getConfigManager().getUpdateTicks()
        );
    }

    public void restart() {
        start();
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    private void updateOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(
                    plugin,
                    task -> TabManager.update(player),
                    null
            );
        }
    }
}
