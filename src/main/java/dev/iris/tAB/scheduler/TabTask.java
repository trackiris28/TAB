package dev.iris.tAB.scheduler;

import dev.iris.tAB.TAB;
import dev.iris.tAB.tab.TabManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabTask {

    public void start() {

        long ticks =
                TAB.getInstance()
                        .getConfigManager()
                        .getUpdateTicks();

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(

                TAB.getInstance(),

                _ -> {

                    for (Player player : Bukkit.getOnlinePlayers()) {

                        player.getScheduler().run(

                                TAB.getInstance(),

                                _ ->
                                        TabManager.update(player),

                                null
                        );
                    }
                },

                20L,
                ticks
        );
    }
}