package com.github.pozzoo.quickwaystones.managers;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import com.github.pozzoo.quickwaystones.data.WaystoneData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class DataManager {
    private File file;
    private YamlConfiguration config;
    private YamlConfiguration configOverwrite;
    private Set<String> keys;

    public DataManager() {
        keys = new HashSet<>();
        checkFile();
    }

    private void checkFile() {
        file = new File(QuickWaystones.getInstance().getDataFolder(), "waystones.yml");

        if (!file.exists()) {
            QuickWaystones.getInstance().getLogger().info("Creating waystones.yml");
            QuickWaystones.getInstance().saveResource("waystones.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        config.options().parseComments(true);

        if (config.getKeys(true).isEmpty()) return;

        keys = config.getConfigurationSection("Waystones.").getKeys(true);
    }

    public void loadWaystonesData() {

        try {
            config.load(file);

            for (String key : keys) {
                WaystoneData waystoneData = new WaystoneData(key, config.getLocation("Waystones." + key));
                QuickWaystones.getWaystonesMap().put(waystoneData.getLocation(), waystoneData);
            }

        } catch (InvalidConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveWaystoneData(Collection<WaystoneData> waystones) {
        configOverwrite = new YamlConfiguration();

        for (WaystoneData waystone : waystones) {
            configOverwrite.set("Waystones." + waystone.getName(), waystone.getLocation());
        }

        save();
    }

    public void save() {
        QuickWaystones.getInstance().saveResource("waystones.yml", true);

        try {
            configOverwrite.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
