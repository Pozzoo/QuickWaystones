package fun.pozzoo.quickwaystones;

import fun.pozzoo.quickwaystones.data.WaystoneData;
import fun.pozzoo.quickwaystones.events.OnBlockBreak;
import fun.pozzoo.quickwaystones.events.OnPlayerInteract;
import fun.pozzoo.quickwaystones.managers.CraftManager;
import fun.pozzoo.quickwaystones.managers.DataManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class QuickWaystones extends JavaPlugin {
    private static QuickWaystones plugin;
    private static DataManager dataManager;
    private static final Map<Location, WaystoneData> waystonesMap = new HashMap<>();
    private static final Map<UUID, Set<Integer>> playerAccess = new HashMap<>();
    private static int lastWaystoneID = 0;
    private static Metrics metrics;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        saveDefaultConfig();

        CraftManager craftManager = new CraftManager();
        craftManager.registerRecipes();

        new OnPlayerInteract(plugin);
        new OnBlockBreak(plugin);

        dataManager = new DataManager();
        dataManager.loadData();

        OptionalInt maxId = waystonesMap.values().stream()
                .mapToInt(WaystoneData::getId)
                .max();

        if (maxId.isPresent()) {
            lastWaystoneID = maxId.getAsInt();
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
        saveData();
    }

    public static void createWaystone(Location location, WaystoneData waystoneData) {
        waystonesMap.put(location, waystoneData);
        saveData();
    }

    public static WaystoneData getWaystone(Location location) {
        return waystonesMap.get(location);
    }

    public static Map<UUID, Set<Integer>> getPlayerAccess() {
        return playerAccess;
    }

    public static void saveData() {
        dataManager.saveData(waystonesMap.values(), playerAccess);
    }
}
