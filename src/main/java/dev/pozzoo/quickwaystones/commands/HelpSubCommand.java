package dev.pozzoo.quickwaystones.commands;

import org.bukkit.command.CommandSender;

import java.util.Map;

public class HelpSubCommand implements SubCommand {

    private final Map<String, SubCommand> subCommands;

    public HelpSubCommand(Map<String, SubCommand> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show available commands";
    }

    @Override
    public String getUsage() {
        return "/quickWaystones help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        sender.sendMessage("§6quickWaystones Commands:");

        for (SubCommand sub : subCommands.values()) {
            sender.sendMessage("§e" + sub.getUsage() + " §7- " + sub.getDescription());
        }
    }
}
