package fun.pozzoo.quickwaystones.gui;

import fun.pozzoo.quickwaystones.QuickWaystones;
import fun.pozzoo.quickwaystones.data.WaystoneData;
import fun.pozzoo.quickwaystones.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaystoneGUI implements Listener {

    private static final int ROWS = 4;
    private static final int PAGE_SIZE = 27; // top 3 rows
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final int PREV_SLOT = 27 + (3 - 1); // 29
    private static final int NEXT_SLOT = 27 + (7 - 1); // 33

    private static volatile boolean registered = false;

    public static void runGUI(Player player) {
        ensureRegistered();
        openPage(player, 0);
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

    private static void openPage(Player player, int page) {
        Map<Location, WaystoneData> waystonesMap = QuickWaystones.getWaystonesMap();
        List<WaystoneData> waystones = new ArrayList<>(waystonesMap.values());

        int totalItems = waystones.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        Component title = StringUtils.formatString("Waystones").append(StringUtils.formatString((totalPages > 1 ? " (Page " + (currentPage + 1) + "/" + totalPages + ")" : "")));

        WaystoneHolder holder = new WaystoneHolder(currentPage, totalPages);
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE, title);

        // Fill page items (top 27 slots)
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            WaystoneData ws = waystones.get(i);
            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(StringUtils.formatItemName(ws.getName()));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            holder.slotToWaystone.put(slot, ws);
            slot++;
        }

        // Pagination controls if multiple pages
        if (totalPages > 1) {
            // Previous
            if (currentPage > 0) {
                inv.setItem(PREV_SLOT, named(StringUtils.formatString("Previous")));
            }
            // Next
            if (currentPage < totalPages - 1) {
                inv.setItem(NEXT_SLOT, named(StringUtils.formatString("Next")));
            }
        }

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
        final Map<Integer, WaystoneData> slotToWaystone = new HashMap<>();

        WaystoneHolder(int page, int totalPages) {
            this.page = page;
            this.totalPages = totalPages;
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
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        // Only handle clicks inside the top inventory
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            return;
        }

        // Pagination
        if (slot == PREV_SLOT && holder.page > 0) {
            openPage(player, holder.page - 1);
            return;
        }
        if (slot == NEXT_SLOT && holder.page < holder.totalPages - 1) {
            openPage(player, holder.page + 1);
            return;
        }

        // Waystone teleport
        WaystoneData ws = holder.slotToWaystone.get(slot);
        if (ws != null) {
            player.teleport(ws.getLocation().clone().add(0.5, 1, 0.5));
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 5);
            player.playSound(player, Sound.ENTITY_FOX_TELEPORT, 0.5f, 1f);
            player.closeInventory();
        }
    }
}
