package fun.pozzoo.quickwaystones.commands;

import fun.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MainCommand(QuickWaystones plugin) {

        // Register subcommands
        subCommands.put("setHideUndiscoveredWaystones", new SetHideUndiscoveredWaystones(plugin));
        subCommands.put("setEnableWaystonePass", new SetEnableWaystonePass(plugin));
        subCommands.put("help", new HelpSubCommand(subCommands));
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            subCommands.get("help").execute(sender, args);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0]);

        if (subCommand == null) {
            sender.sendMessage("§cUnknown subcommand.");
            subCommands.get("help").execute(sender, args);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args
    ) {

        if (!sender.isOp()) return Collections.emptyList();

        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }

        if (args.length == 2 && (args[0].equals("setHideUndiscoveredWaystones") || args[0].equals("setEnableWaystonePass"))) {
            return Stream.of("true", "false")
                    .filter(v -> v.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
