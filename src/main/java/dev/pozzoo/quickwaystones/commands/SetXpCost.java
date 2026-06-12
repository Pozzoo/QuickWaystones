package dev.pozzoo.quickwaystones.commands;

import dev.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.command.CommandSender;

public class SetXpCost implements SubCommand {

    private final QuickWaystones plugin;

    public SetXpCost(QuickWaystones plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setXpCost";
    }

    @Override
    public String getDescription() {
        return "Set the XP level cost for using waystones";
    }

    @Override
    public String getUsage() {
        return "/quickWaystones setXpCost <amount>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }

        int cost;
        try {
            cost = Integer.parseInt(args[1]);
            if (cost < 0) {
                sender.sendMessage("§cXP cost must be 0 or greater.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cXP cost must be a valid number.");
            return;
        }

        plugin.getConfig().set("Settings.XpCost", cost);
        plugin.saveConfig();

        sender.sendMessage("§aXP cost set to §e" + cost);
    }
}
