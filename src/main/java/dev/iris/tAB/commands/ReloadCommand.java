package dev.iris.tAB.commands;

import dev.iris.tAB.TAB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {

        TAB.getInstance()
                .getConfigManager()
                .reload();

        sender.sendMessage("Reloaded config!");

        return true;
    }
}