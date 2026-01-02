package fun.pozzoo.quickwaystones.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    String getName();

    String getDescription();

    String getUsage();

    void execute(CommandSender sender, String[] args);
}

