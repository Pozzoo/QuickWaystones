package dev.pozzoo.quickwaystones;

import dev.pozzoo.quickwaystones.commands.MainCommand;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.events.*;
import dev.pozzoo.quickwaystones.items.WaystonePass;
import dev.pozzoo.quickwaystones.managers.CraftManager;
import dev.pozzoo.quickwaystones.managers.DataManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

import dev.pozzoo.quickwaystones.managers.PotionManager;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickWaystones extends JavaPlugin {

    private static QuickWaystones plugin;
    private static DataManager dataManager;
    private static final Map<Location, WaystoneData> waystonesMap = new HashMap<>();
    private static final Map<UUID, Set<Integer>> playerAccess = new HashMap<>();
    private static final Map<UUID, List<Integer>> playerWaystoneOrder = new HashMap<>();
    private static int lastWaystoneID = 0;
    private static WaystonePass waystonePass;
    private static Metrics metrics;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        new UpdateChecker(this, "Pozzoo", "QuickWaystones", "quickwaystones.updatenotify", "quickwaystones");

        saveDefaultConfig();

        CraftManager craftManager = new CraftManager();
        craftManager.registerRecipes();

        PotionManager potionManager = new PotionManager(plugin);
        potionManager.registerPotion();

        new OnPlayerInteract(plugin);
        new OnBlockBreak(plugin);
        new OnConsume(plugin);
        new OnBlockPlace(plugin);
        new OnExplode(plugin);

        dataManager = new DataManager();
        lastWaystoneID = dataManager.loadData();

        waystonePass = new WaystonePass(
            plugin,
            "waystone_pass",
            "bound_waystone"
        );

        OptionalInt maxId = waystonesMap
            .values()
            .stream()
            .mapToInt(WaystoneData::getId)
            .max();

        if (maxId.isPresent()) {
            lastWaystoneID = maxId.getAsInt();
        }

        MainCommand mainCommand = new MainCommand(this);
        PluginCommand command = getCommand("quickWaystones");

        if (command != null) {
            command.setExecutor(mainCommand);
            command.setTabCompleter(mainCommand);
        }

        metrics = new Metrics(plugin, 22064);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveData();

        metrics.shutdown();
    }

    public static QuickWaystones getInstance() {
        return plugin;
    }

    public static int getAndIncrementLastWaystoneID() {
        lastWaystoneID++;
        return lastWaystoneID;
    }

    public static Map<Location, WaystoneData> getWaystonesMap() {
        return waystonesMap;
    }

    public static void removeWaystone(Location location) {
        waystonesMap.remove(location);
    }

    public static void removeAccess(Integer waystoneId) {
        playerAccess.values().forEach(access -> access.remove(waystoneId));
    }

    public static void createWaystone(
        Location location,
        WaystoneData waystoneData
    ) {
        waystonesMap.put(location, waystoneData);
    }

    public static WaystoneData getWaystone(Location location) {
        return waystonesMap.get(location);
    }

    public static WaystoneData getWaystone(int id) {
        for (Map.Entry<Location, WaystoneData> entry : waystonesMap.entrySet()) {
            if (entry.getValue().getId() == id) {
                return entry.getValue();
            }
        }

        return null;
    }


    public static Map<UUID, Set<Integer>> getPlayerAccess() {
        return playerAccess;
    }

    public static Set<Integer> getOrCreatePlayerAccess(UUID uuid) {
        return playerAccess.computeIfAbsent(uuid, ignored -> new HashSet<>());
    }

    public static Map<UUID, List<Integer>> getPlayerWaystoneOrder() {
        return playerWaystoneOrder;
    }

    public static WaystonePass getWaystonePass() {
        return waystonePass;
    }

    public static void saveData() {
        dataManager.saveData(waystonesMap.values(), playerAccess, playerWaystoneOrder);
    }

    public static boolean existsInMap(Location location) {
        return waystonesMap
            .keySet()
            .stream()
            .anyMatch(
                l ->
                    l.getWorld().equals(location.getWorld()) &&
                    l.getBlockX() == location.getBlockX() &&
                    l.getBlockY() == location.getBlockY() &&
                    l.getBlockZ() == location.getBlockZ()
            );
    }
}
