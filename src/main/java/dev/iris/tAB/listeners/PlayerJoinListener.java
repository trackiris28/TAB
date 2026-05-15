package dev.iris.tAB.listeners;

import dev.iris.tAB.TAB;
import dev.iris.tAB.tab.TabManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final TAB plugin;

    public PlayerJoinListener(TAB plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getFakePingPacketManager().inject(event.getPlayer());

        event.getPlayer().getScheduler().run(
                plugin,
                task -> {
                    TabManager.update(event.getPlayer());
                    plugin.getFakePingPacketManager().sendLatencyUpdate();
                },
                null
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getFakePingPacketManager().uninject(event.getPlayer());
    }
}
