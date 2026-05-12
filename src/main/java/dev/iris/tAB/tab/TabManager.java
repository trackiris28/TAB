package dev.iris.tAB.tab;

import dev.iris.tAB.TAB;
import dev.iris.tAB.utils.ColorUtil;
import dev.iris.tAB.utils.TPSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TabManager {

    public static void update(Player player) {

        List<String> headerList =
                TAB.getInstance()
                        .getConfigManager()
                        .getHeader();

        List<String> footerList =
                TAB.getInstance()
                        .getConfigManager()
                        .getFooter();

        String header = headerList.stream()
                .map(line -> parse(player, line))
                .collect(Collectors.joining("\n"));

        String footer = footerList.stream()
                .map(line -> parse(player, line))
                .collect(Collectors.joining("\n"));

        player.setPlayerListHeaderFooter(
                header,
                footer
        );

        player.setPlayerListName(
                player.getName()
        );
    }

    private static String parse(Player player, String text) {

        return ColorUtil.color(

                text
                        .replace("%player%",
                                player.getName())

                        .replace("%online%",
                                String.valueOf(
                                        Bukkit.getOnlinePlayers().size()
                                ))

                        .replace("%ping%",
                                String.valueOf(
                                        player.getPing()
                                ))

                        .replace("%tps%",
                                TPSUtil.getTPS())
        );
    }
}