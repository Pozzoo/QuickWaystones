package dev.pozzoo.quickwaystones.managers;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import java.io.File;
import org.bukkit.Material;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager {

    private final Logger logger;
    private File file;
    private YamlConfiguration config;

    public DataManager() {
        logger = QuickWaystones.getInstance().getLogger();
        checkFile();
    }

    private void checkFile() {
        file = new File(
            QuickWaystones.getInstance().getDataFolder(),
            "waystones.yml"
        );

        if (!file.exists()) {
            QuickWaystones.getInstance()
                .getLogger()
                .info("Creating waystones.yml");
            QuickWaystones.getInstance().saveResource("waystones.yml", false);
        }
    }

    public int loadData() {
        config = YamlConfiguration.loadConfiguration(file);
        config.options().parseComments(true);

        String pluginVersion = QuickWaystones.getInstance()
            .getDescription()
            .getVersion();
        DataMigrator migration = new DataMigrator(logger, pluginVersion);
        DataMigrator.MigrationResult result = migration.migrate(config);
        if (result.migrated()) {
            logger.info(
                "Rewriting waystones.yml for plugin version " + pluginVersion
            );
            config = result.configuration();
            saveCurrentConfig(config);
        }

        QuickWaystones.getWaystonesMap().clear();
        QuickWaystones.getPlayerAccess().clear();

        int lastComputedId = 0;
        ConfigurationSection waystonesSection = config.getConfigurationSection(
            "Waystones"
        );
        if (waystonesSection != null) {
            List<String> keys = new ArrayList<>(
                waystonesSection.getKeys(false)
            );
            keys.sort(Comparator.comparingInt(Integer::parseInt));

            for (String key : keys) {
                String basePath = "Waystones." + key;
                Location location = config.getLocation(basePath + ".location");
                String ownerValue = config.getString(basePath + ".owner");
                if (location == null || ownerValue == null) {
                    logger.warning("Skipping invalid waystone entry: " + key);
                    continue;
                }

                int id = Integer.parseInt(key);
                String iconName = config.getString(basePath + ".icon", "ENDER_PEARL");
                Material icon;
                try {
                    icon = Material.valueOf(iconName);
                } catch (IllegalArgumentException e) {
                    icon = Material.ENDER_PEARL;
                }
                WaystoneData waystoneData = new WaystoneData(
                    id,
                    config.getString(basePath + ".name", "Waystone " + id),
                    location,
                    UUID.fromString(ownerValue),
                    config.getInt(basePath + ".direction", 0),
                    icon
                );
                QuickWaystones.getWaystonesMap().put(
                    waystoneData.getLocation(),
                    waystoneData
                );
                QuickWaystones.getPlayerAccess()
                    .computeIfAbsent(waystoneData.getOwner(), ignored ->
                        new LinkedHashSet<>()
                    )
                    .add(waystoneData.getId());

                if (waystoneData.getId() > lastComputedId) {
                    lastComputedId = waystoneData.getId();
                }
            }
        }

        ConfigurationSection accessSection = config.getConfigurationSection(
            "Access"
        );
        if (accessSection != null) {
            for (String key : accessSection.getKeys(false)) {
                UUID playerId = UUID.fromString(key);
                Set<Integer> access =
                    QuickWaystones.getPlayerAccess().computeIfAbsent(
                        playerId,
                        ignored -> new LinkedHashSet<>()
                    );
                access.addAll(config.getIntegerList("Access." + key));
            }
        }

        ConfigurationSection orderSection = config.getConfigurationSection("Order");
        if (orderSection != null) {
            for (String key : orderSection.getKeys(false)) {
                UUID playerId = UUID.fromString(key);
                List<Integer> order = new ArrayList<>(config.getIntegerList("Order." + key));
                QuickWaystones.getPlayerWaystoneOrder().put(playerId, order);
            }
        }

        return lastComputedId;
    }

    public void saveData(
        Iterable<WaystoneData> waystones,
        Map<UUID, Set<Integer>> playerAccess,
        Map<UUID, List<Integer>> playerWaystoneOrder
    ) {
        config = new YamlConfiguration();
        config.set(
            "PluginVersion",
            QuickWaystones.getInstance().getDescription().getVersion()
        );

        List<WaystoneData> orderedWaystones = new ArrayList<>();
        waystones.forEach(orderedWaystones::add);
        orderedWaystones.sort(Comparator.comparingInt(WaystoneData::getId));

        for (WaystoneData waystone : orderedWaystones) {
            String basePath = "Waystones." + waystone.getId();
            config.set(basePath + ".name", waystone.getName());
            config.set(basePath + ".location", waystone.getLocation());
            config.set(basePath + ".owner", waystone.getOwner().toString());
            config.set(basePath + ".direction", waystone.getDirection());
            config.set(basePath + ".icon", waystone.getIcon().name());
        }

        playerAccess
            .entrySet()
            .stream()
            .sorted(
                Map.Entry.comparingByKey(Comparator.comparing(UUID::toString))
            )
            .forEach(entry ->
                config.set(
                    "Access." + entry.getKey(),
                    entry
                        .getValue()
                        .stream()
                        .sorted()
                        .toArray(Integer[]::new)
                )
            );

        playerWaystoneOrder
            .entrySet()
            .stream()
            .sorted(
                Map.Entry.comparingByKey(Comparator.comparing(UUID::toString))
            )
            .forEach(entry ->
                config.set(
                    "Order." + entry.getKey(),
                    entry.getValue().stream().collect(Collectors.toList())
                )
            );

        save();
    }

    private void saveCurrentConfig(YamlConfiguration configToSave) {
        try {
            QuickWaystones.getInstance().saveResource("waystones.yml", true);
            configToSave.save(file);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to rewrite migrated waystones.yml",
                e
            );
        }
    }

    public void save() {
        QuickWaystones.getInstance().saveResource("waystones.yml", true);

        try {
            config.save(file);
        } catch (Exception e) {
            logger.severe("Failed to save waystones.yml: " + e.getMessage());
            logger.severe(
                "Stack trace: " + java.util.Arrays.toString(e.getStackTrace())
            );
        }
    }
}
