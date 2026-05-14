package dev.iris.tAB.tab;

import dev.iris.tAB.TAB;
import dev.iris.tAB.config.ConfigManager;
import dev.iris.tAB.utils.ColorUtil;
import dev.iris.tAB.utils.TPSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class TabManager {

    private TabManager() {
    }

    public static void update(Player player) {
        ConfigManager configManager = TAB.getInstance().getConfigManager();

        if (!configManager.isEnabled()) {
            return;
        }

        String playerName = player.getName();
        String onlinePlayers = String.valueOf(Bukkit.getOnlinePlayers().size());
        String playerPing = String.valueOf(player.getPing());
        String tps = TPSUtil.getTPS();

        player.setPlayerListHeaderFooter(
                parseLines(configManager.getHeader(), playerName, onlinePlayers, playerPing, tps),
                parseLines(configManager.getFooter(), playerName, onlinePlayers, playerPing, tps)
        );

        player.setPlayerListName(playerName);
    }

    private static String parseLines(
            List<String> lines,
            String playerName,
            String onlinePlayers,
            String playerPing,
            String tps
    ) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                builder.append('\n');
            }

            builder.append(parse(lines.get(index), playerName, onlinePlayers, playerPing, tps));
        }

        return builder.toString();
    }

    private static String parse(
            String text,
            String playerName,
            String onlinePlayers,
            String playerPing,
            String tps
    ) {
        return ColorUtil.color(
                text
                        .replace("%player%", playerName)
                        .replace("%online%", onlinePlayers)
                        .replace("%ping%", playerPing)
                        .replace("%tps%", tps)
        );
    }
}
