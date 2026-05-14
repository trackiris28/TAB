package dev.iris.tAB.listeners;

import dev.iris.tAB.TAB;
import dev.iris.tAB.tab.TabManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final TAB plugin;

    public PlayerJoinListener(TAB plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getScheduler().run(
                plugin,
                task -> TabManager.update(event.getPlayer()),
                null
        );
    }
}
