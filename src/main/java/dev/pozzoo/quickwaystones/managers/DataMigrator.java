package dev.pozzoo.quickwaystones.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class DataMigrator {

    private static final String VERSION_PATH = "PluginVersion";

    private final Logger logger;
    private final Version currentVersion;

    public DataMigrator(Logger logger, String currentVersion) {
        this.logger = logger;
        this.currentVersion = Version.parse(currentVersion);
    }

    public MigrationResult migrate(YamlConfiguration source) {
        Schema schema = detectSchema(source);
        Version storedVersion = Version.parse(source.getString(VERSION_PATH));

        if (
            storedVersion != null &&
            storedVersion.compareTo(currentVersion) >= 0 &&
            (schema == Schema.CURRENT || schema == Schema.EMPTY)
        ) {
            return new MigrationResult(source, false);
        }

        YamlConfiguration migrated = new YamlConfiguration();
        migrated.set(VERSION_PATH, currentVersion.toString());

        switch (schema) {
            case CURRENT -> copyCurrent(source, migrated);
            case LEGACY_2_0 -> migrateLegacyNamedWaystones(
                source,
                migrated,
                true
            );
            case LEGACY_1_1 -> migrateLegacyNamedWaystones(
                source,
                migrated,
                false
            );
            case EMPTY -> {
            }
        }

        return new MigrationResult(migrated, true);
    }

    private Schema detectSchema(YamlConfiguration config) {
        ConfigurationSection waystonesSection = config.getConfigurationSection(
            "Waystones"
        );

        if (
            waystonesSection == null ||
            waystonesSection.getKeys(false).isEmpty()
        ) {
            return Schema.EMPTY;
        }

        boolean allNumericKeys = waystonesSection
            .getKeys(false)
            .stream()
            .allMatch(DataMigrator::isNumeric);
        if (allNumericKeys) {
            return Schema.CURRENT;
        }

        for (String key : waystonesSection.getKeys(false)) {
            String owner = config.getString("Waystones." + key + ".owner");
            if (isUuid(owner)) {
                return Schema.LEGACY_2_0;
            }
        }

        return Schema.LEGACY_1_1;
    }

    private void copyCurrent(
        YamlConfiguration source,
        YamlConfiguration migrated
    ) {
        ConfigurationSection waystonesSection = source.getConfigurationSection(
            "Waystones"
        );
        if (waystonesSection != null) {
            List<String> keys = new ArrayList<>(
                waystonesSection.getKeys(false)
            );
            keys.sort(Comparator.comparingInt(Integer::parseInt));

            for (String key : keys) {
                String basePath = "Waystones." + key;
                migrated.set(
                    basePath + ".name",
                    source.getString(basePath + ".name", "Waystone " + key)
                );
                migrated.set(
                    basePath + ".location",
                    source.getLocation(basePath + ".location")
                );
                migrated.set(
                    basePath + ".owner",
                    source.getString(basePath + ".owner")
                );
                migrated.set(
                    basePath + ".direction",
                    source.getInt(basePath + ".direction", 0)
                );
                migrated.set(
                        basePath + ".icon",
                        source.getString(basePath + ".icon", "ENDER_PEARL")
                );
            }
        }

        ConfigurationSection accessSection = source.getConfigurationSection(
            "Access"
        );
        if (accessSection != null) {
            for (String key : new TreeSet<>(accessSection.getKeys(false))) {
                migrated.set(
                        "Access." + key,
                        source.getIntegerList("Access." + key).toArray(new Integer[0])
                );
            }
        }

        if (source.contains("Order")) {
            migrated.set("Order", source.getIntegerList("Order"));
        }
    }

    private void migrateLegacyNamedWaystones(
        YamlConfiguration source,
        YamlConfiguration migrated,
        boolean ownerIsUuid
    ) {
        ConfigurationSection waystonesSection = source.getConfigurationSection(
            "Waystones"
        );
        if (waystonesSection == null) {
            return;
        }

        Set<Integer> validIds = new HashSet<>();
        Map<UUID, Set<Integer>> accessByPlayer = new HashMap<>();
        List<String> keys = new ArrayList<>(waystonesSection.getKeys(false));
        int nextId = 1;

        for (String key : keys) {
            String basePath = "Waystones." + key;
            String name = source.getString(basePath + ".name", key);
            Location location = source.getLocation(basePath + ".location");
            String ownerValue = source.getString(basePath + ".owner");

            if (location == null || ownerValue == null) {
                logger.warning(
                    "Skipping invalid legacy waystone entry: " + key
                );
                continue;
            }

            UUID owner = ownerIsUuid
                ? parseUuid(ownerValue)
                : resolveOwner(ownerValue);
            if (owner == null) {
                logger.warning(
                    "Skipping legacy waystone with invalid owner: " + key
                );
                continue;
            }

            migrated.set("Waystones." + nextId + ".name", name);
            migrated.set("Waystones." + nextId + ".location", location);
            migrated.set("Waystones." + nextId + ".owner", owner.toString());
            migrated.set("Waystones." + nextId + ".direction", 0);
            migrated.set("Waystones." + ".icon", source.getString(basePath + ".icon", "ENDER_PEARL"));

            validIds.add(nextId);
            accessByPlayer
                .computeIfAbsent(owner, ignored -> new LinkedHashSet<>())
                .add(nextId);
            nextId++;
        }

        ConfigurationSection accessSection = source.getConfigurationSection(
            "Access"
        );
        if (accessSection != null) {
            for (String key : accessSection.getKeys(false)) {
                UUID playerId = parseUuid(key);
                if (playerId == null) {
                    logger.warning("Skipping invalid access entry: " + key);
                    continue;
                }

                Set<Integer> migratedAccess = accessByPlayer.computeIfAbsent(
                    playerId,
                    ignored -> new LinkedHashSet<>()
                );
                for (Integer id : source.getIntegerList("Access." + key)) {
                    if (validIds.contains(id)) {
                        migratedAccess.add(id);
                    }
                }
            }
        }

        writeAccessSection(migrated, accessByPlayer);
    }

    private void writeAccessSection(
        YamlConfiguration migrated,
        Map<UUID, Set<Integer>> accessByPlayer
    ) {
        accessByPlayer
            .entrySet()
            .stream()
            .sorted(
                Map.Entry.comparingByKey(Comparator.comparing(UUID::toString))
            )
            .forEach(entry ->
                migrated.set(
                    "Access." + entry.getKey(),
                    entry
                        .getValue()
                        .stream()
                        .sorted()
                        .toArray(Integer[]::new)
                )
            );
    }

    private UUID resolveOwner(String ownerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerName);
        return offlinePlayer.getUniqueId();
    }

    private static UUID parseUuid(String value) {
        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isUuid(String value) {
        return parseUuid(value) != null;
    }

    private static boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public record MigrationResult(
        YamlConfiguration configuration,
        boolean migrated
    ) {}

    private enum Schema {
        CURRENT,
        LEGACY_2_0,
        LEGACY_1_1,
        EMPTY,
    }

    private record Version(
        int major,
        int minor,
        int patch
    ) implements Comparable<Version> {
        static Version parse(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            String[] split = value.split("-")[0].split("\\.");
            if (split.length < 3) {
                return null;
            }

            try {
                return new Version(
                    Integer.parseInt(split[0]),
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2])
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        @Override
        public int compareTo(Version other) {
            Objects.requireNonNull(other, "other");

            if (major != other.major) {
                return Integer.compare(major, other.major);
            }
            if (minor != other.minor) {
                return Integer.compare(minor, other.minor);
            }
            return Integer.compare(patch, other.patch);
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
