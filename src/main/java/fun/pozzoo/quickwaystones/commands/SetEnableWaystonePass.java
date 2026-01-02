package fun.pozzoo.quickwaystones.commands;

import fun.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.command.CommandSender;

public class SetEnableWaystonePass implements SubCommand {

    private final QuickWaystones plugin;

    public SetEnableWaystonePass(QuickWaystones plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setEnableWaystonePass";
    }

    @Override
    public String getDescription() {
        return "Enable or disable waystone pass";
    }

    @Override
    public String getUsage() {
        return "/quickWaystones setEnableWaystonePass <true|false>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }

        boolean enabled;
        if (args[1].equalsIgnoreCase("true")) {
            enabled = true;
        } else if (args[1].equalsIgnoreCase("false")) {
            enabled = false;
        } else {
            sender.sendMessage("§cValue must be true or false.");
            return;
        }

        plugin.getConfig().set("Settings.EnableWaystonePass", enabled);
        plugin.saveConfig();

        sender.sendMessage("§aWaystone pass set to §e" + enabled);
    }
}
