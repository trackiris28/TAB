package dev.iris.tAB.listeners;

import dev.iris.tAB.TAB;
import dev.iris.tAB.tab.TabManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        event.getPlayer().getScheduler().run(
                TAB.getInstance(),
                _ -> TabManager.update(event.getPlayer()),
                null
        );
    }
}
