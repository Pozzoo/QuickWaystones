package dev.pozzoo.quickwaystones.gui;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

import static dev.pozzoo.quickwaystones.utils.Utils.applyGlint;

public class WaystoneGUI implements Listener {

    private static final int ROWS = 4;
    private static final int PAGE_SIZE = 27; // top 3 rows
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final int PREV_SLOT = 27 + (3 - 1); // 29
    private static final int NEXT_SLOT = 27 + (7 - 1); // 33
    private static final int PAGE_SLOT = 27 + (5 - 1); // 31
    private static final int REORDER_SLOT = 27 + (9 - 1); // 35

    private static volatile boolean registered = false;
    private static final Set<UUID> reorderingPlayers = new HashSet<>();
    private static final Map<UUID, Integer> firstSelections = new HashMap<>();
    private static final Set<UUID> reopeningPlayers = new HashSet<>();

    public static void runGUI(Player player, WaystoneData waystoneData) {
        ensureRegistered();
        openPage(player, 0, waystoneData);
    }

    private static void ensureRegistered() {
        if (registered) return;
        synchronized (WaystoneGUI.class) {
            if (registered) return;
            Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickWaystones");
            if (plugin == null) {
                throw new IllegalStateException("QuickWaystones plugin not found for GUI event registration.");
            }
            Bukkit.getPluginManager().registerEvents(new WaystoneGUI(), plugin);
            registered = true;
        }
    }

    private static void openPage(Player player, int page, WaystoneData waystoneData) {
        Map<Location, WaystoneData> waystonesMap = new HashMap<>(QuickWaystones.getWaystonesMap());
        Map<UUID, Set<Integer>> playerAccess = QuickWaystones.getPlayerAccess();

        if (QuickWaystones.getInstance().getConfig().getBoolean("Settings.HideUndiscoveredWaystones")) {
            waystonesMap.entrySet().removeIf(entry -> !playerAccess.get(player.getUniqueId()).contains(entry.getValue().getId()));
        }

        List<WaystoneData> waystones = new ArrayList<>(waystonesMap.values());

        List<Integer> playerOrder = QuickWaystones.getPlayerWaystoneOrder().get(player.getUniqueId());
        if (playerOrder != null && !playerOrder.isEmpty()) {
            Map<Integer, Integer> orderIndex = new HashMap<>();
            for (int i = 0; i < playerOrder.size(); i++) {
                orderIndex.put(playerOrder.get(i), i);
            }
            waystones.sort(Comparator.comparingInt(ws -> orderIndex.getOrDefault(ws.getId(), Integer.MAX_VALUE)));
        } else {
            waystones.sort(Comparator.comparingInt(WaystoneData::getId));
        }

        int totalItems = waystones.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        Component title = Utils.formatString("Waystones - ").append(Utils.formatString(waystoneData.getName()));

        WaystoneHolder holder = new WaystoneHolder(currentPage, totalPages, waystoneData);
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE, title);

        // Fill page items (top 27 slots)
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);
        Integer selectedId = firstSelections.get(player.getUniqueId());
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            WaystoneData ws = waystones.get(i);
            ItemStack item = new ItemStack(ws.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Utils.formatItemName(ws.getName()));
                if (selectedId != null && selectedId == ws.getId()) {
                    applyGlint(meta);
                }
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            holder.slotToWaystone.put(slot, ws);
            slot++;
        }

        // Pagination controls if multiple pages
        if (totalPages > 1) {
            // Page indicator
            inv.setItem(PAGE_SLOT, named(Utils.formatString("Page " + (currentPage + 1) + "/" + totalPages)));

            // Previous
            if (currentPage > 0) {
                inv.setItem(PREV_SLOT, named(Utils.formatString("Previous")));
            }
            // Next
            if (currentPage < totalPages - 1) {
                inv.setItem(NEXT_SLOT, named(Utils.formatString("Next")));
            }
        }

        // Reorder button
        boolean reordering = reorderingPlayers.contains(player.getUniqueId());
        ItemStack reorderBtn = new ItemStack(Material.CHEST);
        ItemMeta btnMeta = reorderBtn.getItemMeta();
        if (btnMeta != null) {
            if (reordering) {
                btnMeta.displayName(Utils.formatString("<green>Reordering"));
                applyGlint(btnMeta);
            } else {
                btnMeta.displayName(Utils.formatString("Reorder"));
            }
            reorderBtn.setItemMeta(btnMeta);
        }
        inv.setItem(REORDER_SLOT, reorderBtn);

        reopeningPlayers.add(player.getUniqueId());
        player.openInventory(inv);
    }

    private static ItemStack named(Component name) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class WaystoneHolder implements InventoryHolder {
        final int page;
        final int totalPages;
        final WaystoneData waystoneData;
        final Map<Integer, WaystoneData> slotToWaystone = new HashMap<>();

        WaystoneHolder(int page, int totalPages, WaystoneData waystoneData) {
            this.page = page;
            this.totalPages = totalPages;
            this.waystoneData = waystoneData;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof WaystoneHolder holder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        // Only handle clicks inside the top inventory
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);

        // Pagination
        if (slot == PREV_SLOT && holder.page > 0) {
            openPage(player, holder.page - 1, holder.waystoneData);
            return;
        }
        if (slot == NEXT_SLOT && holder.page < holder.totalPages - 1) {
            openPage(player, holder.page + 1, holder.waystoneData);
            return;
        }

        // Reorder button
        if (slot == REORDER_SLOT) {
            UUID playerId = player.getUniqueId();
            if (reorderingPlayers.contains(playerId)) {
                reorderingPlayers.remove(playerId);
                firstSelections.remove(playerId);
            } else {
                reorderingPlayers.add(playerId);
            }
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
            openPage(player, holder.page, holder.waystoneData);
            return;
        }

        // Waystone interact
        WaystoneData ws = holder.slotToWaystone.get(slot);
        if (ws != null) {
            ItemStack cursor = event.getCursor();
            if (cursor.getType() != Material.AIR) {
                ws.setIcon(cursor.getType());
                QuickWaystones.saveData();
                String message = QuickWaystones.getInstance().getConfig().getString("Messages.WaystoneIconChanged", "Waystone icon changed!");
                player.sendMessage(Utils.formatString("<green>" + message));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                openPage(player, holder.page, holder.waystoneData);
                return;
            }
          
            UUID playerId = player.getUniqueId();
            if (reorderingPlayers.contains(playerId)) {
                Integer selected = firstSelections.get(playerId);
                if (selected == null) {
                    firstSelections.put(playerId, ws.getId());
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                } else if (selected == ws.getId()) {
                    firstSelections.remove(playerId);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                } else {
                    List<Integer> order = QuickWaystones.getPlayerWaystoneOrder()
                        .computeIfAbsent(playerId, ignored -> QuickWaystones.getPlayerAccess()
                            .getOrDefault(playerId, new HashSet<>())
                            .stream().sorted()
                            .collect(Collectors.toCollection(ArrayList::new)));
                    if (!order.contains(selected)) order.add(selected);
                    if (!order.contains(ws.getId())) order.add(ws.getId());
                    Collections.swap(order, order.indexOf(selected), order.indexOf(ws.getId()));
                    QuickWaystones.saveData();
                    firstSelections.remove(playerId);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                }
                openPage(player, holder.page, holder.waystoneData);
                return;
            }

            // Waystone teleport
            if (ws.getId() == holder.waystoneData.getId()) {
                String message = QuickWaystones.getInstance().getConfig().getString("Messages.SameWaystone", "You cannot teleport to the same waystone!");
                player.sendMessage(Utils.formatString("<red>" + message));
                player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1f, 1);
                return;
            }

            if (!Utils.consumePlayerXp(player)) return;

            Utils.teleportPlayer(ws, player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof WaystoneHolder)) {
            return;
        }
        boolean affectsGui = event.getRawSlots().stream().anyMatch(slot -> slot < INVENTORY_SIZE);
        if (affectsGui) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof WaystoneHolder)) {
            return;
        }
        UUID playerId = event.getPlayer().getUniqueId();
        if (reopeningPlayers.remove(playerId)) {
            return;
        }
        reorderingPlayers.remove(playerId);
        firstSelections.remove(playerId);
    }
}
