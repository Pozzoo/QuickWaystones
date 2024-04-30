package com.github.pozzoo.quickwaystones;

import com.github.pozzoo.quickwaystones.data.WaystoneData;
import com.github.pozzoo.quickwaystones.events.OnBlockBreak;
import com.github.pozzoo.quickwaystones.events.OnPlayerInteract;
import com.github.pozzoo.quickwaystones.managers.CraftManager;
import com.github.pozzoo.quickwaystones.managers.DataManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class QuickWaystones extends JavaPlugin {
    private static QuickWaystones plugin;
    private DataManager dataManager;
    private static Map<Location, WaystoneData> waystonesMap;
    private static int lastWaystoneID = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        CraftManager craftManager = new CraftManager();
        craftManager.registerRecipes();

        new OnPlayerInteract(plugin);
        new OnBlockBreak(plugin);

        waystonesMap = new HashMap<>();

        dataManager = new DataManager();
        dataManager.loadWaystonesData();

        lastWaystoneID = waystonesMap.size();
    }

    @Override
    public void onDisable() {
        System.out.println(waystonesMap.toString());

        dataManager.saveWaystoneData(waystonesMap.values());
        // Plugin shutdown logic
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
}
