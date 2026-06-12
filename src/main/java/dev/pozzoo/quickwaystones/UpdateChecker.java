package dev.pozzoo.quickwaystones;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Drop-in update checker for Paper plugins.

 * Usage - call once inside onEnable():
 *   new UpdateChecker(this, "GitHubOwner", "repo-name");

 * If GitHub is unreachable, it will automatically fall back to Modrinth
 * using the repo/plugin name as a slug guess.
 * If your Modrinth slug differs, use the 5-arg constructor to supply it.

 * Ops (or players with <pluginname>.updatenotify) are notified on join.
 * Add to plugin.yml:
 *   permissions:
 *     myplugin.updatenotify:
 *       description: Receive update notifications on join.
 *       default: op
 */
public class UpdateChecker implements Listener {

    private static final String RELEASE_API_URL =
            "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String TAGS_API_URL =
            "https://api.github.com/repos/%s/%s/tags";
    private static final String RELEASE_URL =
            "https://github.com/%s/%s/releases/latest";
    private static final String TAG_URL =
            "https://github.com/%s/%s/releases/tag/%s";
    private static final String MODRINTH_VERSIONS_URL =
            "https://api.modrinth.com/v2/project/%s/version";
    private static final String MODRINTH_PROJECT_URL =
            "https://modrinth.com/plugin/%s";

    private final JavaPlugin plugin;
    private final String owner;
    private final String repo;
    private final String permission;
    private final String modrinthProjectSlug;

    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = null;
    private volatile String latestDownloadUrl = null;
    private volatile String latestSource = null;

    public UpdateChecker(JavaPlugin plugin, String owner, String repo) {
        this(plugin, owner, repo, defaultPermissionNode(plugin), null);
    }

    public UpdateChecker(
            JavaPlugin plugin,
            String owner,
            String repo,
            String modrinthProjectSlug
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.repo = repo;
        this.modrinthProjectSlug = modrinthProjectSlug;
        this.permission = defaultPermissionNode(plugin);
    }

    public UpdateChecker(
            JavaPlugin plugin,
            String owner,
            String repo,
            String permission,
            String modrinthProjectSlug
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.repo = repo;
        this.permission = permission;
        this.modrinthProjectSlug = modrinthProjectSlug;

        plugin
                .getLogger()
                .info(
                        "[UpdateChecker] Initialized for GitHub " +
                                owner +
                                "/" +
                                repo +
                                (modrinthProjectSlug != null
                                        ? " and Modrinth " + modrinthProjectSlug
                                        : "")
                );

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::check);
    }

    // -------------------------------------------------------------------------
    // Check logic
    // -------------------------------------------------------------------------

    private void check() {
        try {
            plugin.getLogger().info("[UpdateChecker] Checking for updates...");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String version = null;
            String source = null;
            String downloadUrl = null;

            try {
                String modrinthVersion = fetchLatestModrinthVersion(client);
                if (modrinthVersion != null) {
                    version = modrinthVersion;
                    source = "Modrinth";
                    downloadUrl = latestDownloadUrl;
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                plugin
                        .getLogger()
                        .fine(
                                "[UpdateChecker] Modrinth check failed; trying GitHub."
                        );
            }

            try {
                String githubVersion = fetchLatestGitHubVersion(client);
                if (
                        githubVersion != null &&
                                (version == null || isNewer(githubVersion, version))
                ) {
                    version = githubVersion;
                    source = "GitHub";
                    downloadUrl = latestDownloadUrl;
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                plugin
                        .getLogger()
                        .fine(
                                "[UpdateChecker] GitHub check failed; continuing with Modrinth result."
                        );
            }

            if (version == null) {
                plugin
                        .getLogger()
                        .warning(
                                "[UpdateChecker] No update source could be resolved from GitHub or Modrinth."
                        );
                return;
            }

            latestVersion = stripLeadingV(version);
            latestSource = source;
            latestDownloadUrl = downloadUrl;

            String current = plugin.getDescription().getVersion();

            plugin
                    .getLogger()
                    .info(
                            "[UpdateChecker] Resolved latest version from " +
                                    latestSource +
                                    ": v" +
                                    latestVersion
                    );

            if (isNewer(latestVersion, current)) {
                updateAvailable = true;
                plugin
                        .getLogger()
                        .warning("┌─────────────────────────────────────────┐");
                plugin
                        .getLogger()
                        .warning("│  " + plugin.getName() + " update available!");
                plugin.getLogger().warning("│  Running : v" + current);
                plugin.getLogger().warning("│  Latest  : v" + latestVersion);
                plugin.getLogger().warning("│  Source  : " + latestSource);
                plugin.getLogger().warning("│  " + latestDownloadUrl);
                plugin
                        .getLogger()
                        .warning("└─────────────────────────────────────────┘");
            } else {
                plugin
                        .getLogger()
                        .info(
                                "[UpdateChecker] " +
                                        plugin.getName() +
                                        " is up to date (v" +
                                        current +
                                        ")."
                        );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin
                    .getLogger()
                    .log(
                            Level.FINE,
                            "[UpdateChecker] Failed to check for updates.",
                            e
                    );
        }
    }

    // -------------------------------------------------------------------------
    // Op join notification
    // -------------------------------------------------------------------------

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;
        Player player = event.getPlayer();
        if (!player.hasPermission(permission) && !player.isOp()) return;

        UUID playerId = player.getUniqueId();
        String url =
                latestDownloadUrl != null
                        ? latestDownloadUrl
                        : String.format(RELEASE_URL, owner, repo);
        String current = plugin.getDescription().getVersion();

        // 2-second delay so the message isn't buried in login spam
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    Player onlinePlayer = Bukkit.getPlayer(playerId);
                    if (onlinePlayer == null || !onlinePlayer.isOnline()) return;

                    onlinePlayer.sendMessage(
                            Component.text()
                                    .append(
                                            Component.text(
                                                    "[" + plugin.getName() + "] ",
                                                    NamedTextColor.GOLD
                                            )
                                    )
                                    .append(
                                            Component.text(
                                                    "Update available  ",
                                                    NamedTextColor.YELLOW
                                            )
                                    )
                                    .append(
                                            Component.text("v" + current, NamedTextColor.RED)
                                    )
                                    .append(Component.text(" → ", NamedTextColor.GRAY))
                                    .append(
                                            Component.text(
                                                    "v" + latestVersion,
                                                    NamedTextColor.GREEN
                                            )
                                    )
                                    .append(Component.newline())
                                    .append(
                                            Component.text(
                                                            "    ↳ Download",
                                                            NamedTextColor.AQUA
                                                    )
                                                    .decorate(TextDecoration.UNDERLINED)
                                                    .clickEvent(ClickEvent.openUrl(url))
                                    )
                                    .build()
                    );
                },
                40L
        );
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Whether a newer version was found on GitHub. */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /** The latest version string from GitHub (e.g. "1.2.3"), or null if not yet checked. */
    public String getLatestVersion() {
        return latestVersion;
    }

    /** The URL used in the update notification, or null if not yet checked. */
    public String getLatestDownloadUrl() {
        return latestDownloadUrl;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns true if {@code remote} is strictly greater than {@code local}
     * using numeric segment comparison (1.10.0 > 1.9.0).
     */
    private boolean isNewer(String remote, String local) {
        int[] r = parseVersion(remote);
        int[] l = parseVersion(local);
        int len = Math.max(r.length, l.length);
        for (int i = 0; i < len; i++) {
            int rv = (i < r.length) ? r[i] : 0;
            int lv = (i < l.length) ? l[i] : 0;
            if (rv != lv) return rv > lv;
        }
        return false;
    }

    private int[] parseVersion(String v) {
        String[] parts = v.replaceAll("[^0-9.]", "").split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                nums[i] = 0;
            }
        }
        return nums;
    }

    private String fetchLatestGitHubVersion(HttpClient client)
            throws Exception {
        String releaseTag = fetchJsonField(client, RELEASE_API_URL, "tag_name");
        if (releaseTag != null && !releaseTag.isBlank()) {
            latestDownloadUrl = String.format(RELEASE_URL, owner, repo);
            return releaseTag;
        }

        String tagsBody = fetchJsonBody(client, TAGS_API_URL);
        if (tagsBody == null || tagsBody.isBlank()) {
            return null;
        }

        List<String> tagNames = extractJsonFieldValues(tagsBody, "name");
        String bestTag = pickBestVersionTag(tagNames);
        if (bestTag == null) {
            return null;
        }

        latestDownloadUrl = String.format(TAG_URL, owner, repo, bestTag);
        return bestTag;
    }

    private String fetchLatestModrinthVersion(HttpClient client)
            throws Exception {
        for (String candidate : modrinthProjectCandidates()) {
            String versionsBody = fetchJsonBody(
                    client,
                    String.format(MODRINTH_VERSIONS_URL, candidate)
            );
            if (versionsBody == null || versionsBody.isBlank()) {
                continue;
            }

            List<String> versionNumbers = extractJsonFieldValues(
                    versionsBody,
                    "version_number"
            );
            String bestVersion = pickBestVersionTag(versionNumbers);
            if (bestVersion == null) {
                continue;
            }

            latestDownloadUrl = String.format(
                    MODRINTH_PROJECT_URL,
                    normalizeModrinthSlug(candidate)
            );
            return bestVersion;
        }

        return null;
    }

    private String pickBestVersionTag(List<String> tags) {
        String bestTag = null;
        String bestVersion = null;
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) continue;
            String version = stripLeadingV(tag);
            if (bestVersion == null || isNewer(version, bestVersion)) {
                bestTag = tag;
                bestVersion = version;
            }
        }
        return bestTag;
    }

    private List<String> modrinthProjectCandidates() {
        List<String> candidates = new ArrayList<>();
        addCandidate(candidates, modrinthProjectSlug);
        addCandidate(candidates, normalizeModrinthSlug(repo));
        addCandidate(candidates, normalizeModrinthSlug(plugin.getName()));
        return candidates;
    }

    private void addCandidate(List<String> candidates, String candidate) {
        if (
                candidate == null ||
                        candidate.isBlank() ||
                        candidates.contains(candidate)
        ) {
            return;
        }
        candidates.add(candidate);
    }

    private String normalizeModrinthSlug(String value) {
        String slug = value.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("-+", "-");
        return slug.replaceAll("^-|-$", "");
    }

    private String fetchJsonBody(HttpClient client, String apiUrl)
            throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(apiUrl, owner, repo)))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", plugin.getName() + "-UpdateChecker")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 404) {
            return null;
        }

        if (response.statusCode() != 200) {
            plugin
                    .getLogger()
                    .fine(
                            "[UpdateChecker] GitHub API returned HTTP " +
                                    response.statusCode() +
                                    " for " +
                                    apiUrl
                    );
            return null;
        }

        return response.body();
    }

    private String fetchJsonField(
            HttpClient client,
            String apiUrl,
            String field
    ) throws Exception {
        String body = fetchJsonBody(client, apiUrl);
        return body == null ? null : extractJsonField(body, field);
    }

    private List<String> extractJsonFieldValues(String json, String field) {
        Pattern pattern = Pattern.compile(
                "\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\""
        );
        Matcher matcher = pattern.matcher(json);
        List<String> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    /** Minimal JSON field extractor - avoids adding a JSON library dep. */
    private String extractJsonField(String json, String field) {
        Pattern pattern = Pattern.compile(
                "\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\""
        );
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String defaultPermissionNode(JavaPlugin plugin) {
        String base = plugin
                .getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", ".");
        base = base.replaceAll("\\.+", ".");
        base = base.replaceAll("^\\.|\\.$", "");
        return (base.isEmpty() ? "plugin" : base) + ".updatenotify";
    }

    private String stripLeadingV(String tag) {
        return (tag.startsWith("v") || tag.startsWith("V"))
                ? tag.substring(1)
                : tag;
    }
}
