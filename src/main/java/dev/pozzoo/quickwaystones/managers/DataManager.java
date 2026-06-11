package dev.pozzoo.quickwaystones.managers;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;


public class DataManager {
    private final Logger logger;
    private File file;
    private YamlConfiguration config;
    private YamlConfiguration configOverwrite;
    private Set<String> waystoneKeys;
    private Set<String> accessKeys;

    public DataManager() {
        logger = QuickWaystones.getInstance().getLogger();
        waystoneKeys = new HashSet<>();
        accessKeys = new HashSet<>();
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

        waystoneKeys = Objects.requireNonNull(config.getConfigurationSection("Waystones.")).getKeys(false);
        accessKeys = Objects.requireNonNull(config.getConfigurationSection("Access.")).getKeys(false);
    }

    public int loadData() {
        try {
            config.load(file);
            int lastComputedId = 0;

            for (String key : waystoneKeys) {
                WaystoneData waystoneData = new WaystoneData(Integer.parseInt(key), config.getString("Waystones." + key + ".name"), config.getLocation("Waystones." + key + ".location"), UUID.fromString(Objects.requireNonNull(config.getString("Waystones." + key + ".owner"))), Integer.parseInt(Objects.requireNonNull(config.getString("Waystones." + key + ".direction"))));
                QuickWaystones.getWaystonesMap().put(waystoneData.getLocation(), waystoneData);

                if (waystoneData.getId() > lastComputedId) lastComputedId = waystoneData.getId();
            }

            for (String key : accessKeys) {
                QuickWaystones.getPlayerAccess().put(UUID.fromString(key), new HashSet<>(Arrays.asList(Objects.requireNonNull(config.getIntegerList("Access." + key)).toArray(new Integer[0]))));
            }

            return lastComputedId;
        } catch (InvalidConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveData(Collection<WaystoneData> waystones, Map<UUID, Set<Integer>> playerAccess) {
        configOverwrite = new YamlConfiguration();

        for (WaystoneData waystone : waystones) {
            configOverwrite.set("Waystones." + waystone.getId() + ".name", waystone.getName());
            configOverwrite.set("Waystones." + waystone.getId() + ".location", waystone.getLocation());
            configOverwrite.set("Waystones." + waystone.getId() + ".owner", waystone.getOwner().toString());
            configOverwrite.set("Waystones." + waystone.getId() + ".direction", waystone.getDirection());
        }

        for (Map.Entry<UUID, Set<Integer>> entry : playerAccess.entrySet()) {
            configOverwrite.set("Access." + entry.getKey().toString(), entry.getValue().toArray(new Integer[0]));
        }

        save();
    }

    public void save() {
        QuickWaystones.getInstance().saveResource("waystones.yml", true);

        try {
            configOverwrite.save(file);
        } catch (Exception e) {
            logger.severe("Failed to save waystones.yml: " + e.getMessage());
            logger.severe("Stack trace: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
