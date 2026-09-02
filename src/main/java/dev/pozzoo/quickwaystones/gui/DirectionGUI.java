package dev.pozzoo.quickwaystones.gui;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class DirectionGUI implements Listener {

    private static final int SIZE = 27;

    private static volatile boolean registered = false;

    public static void runGUI(Player player, WaystoneData waystoneData) {
        ensureRegistered();
        openPage(player, waystoneData);
    }

    private static void ensureRegistered() {
        if (registered) return;
        synchronized (DirectionGUI.class) {
            if (registered) return;
            Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickWaystones");
            if (plugin == null) {
                throw new IllegalStateException("QuickWaystones plugin not found for GUI event registration.");
            }
            Bukkit.getPluginManager().registerEvents(new DirectionGUI(), plugin);
            registered = true;
        }
    }

    private static void openPage(Player player, WaystoneData waystoneData) {
        DirectionHolder holder = new DirectionHolder(waystoneData);
        Inventory inv = Bukkit.createInventory(holder, SIZE, Component.text("Spawn Direction Selector"));

        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler);
        }

        Location loc = player.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();

        Location front = loc.clone().add(dir.clone().multiply(1000));
        Location back  = loc.clone().add(dir.clone().multiply(-1000));
        Vector rightDir = new Vector(-dir.getZ(), 0, dir.getX()); // rotate 90° CW
        Location right = loc.clone().add(rightDir.clone().multiply(1000));
        Location left  = loc.clone().add(rightDir.clone().multiply(-1000));

        ItemStack item = named(Material.COMPASS, Component.text("North"));
        CompassMeta meta = (CompassMeta) item.getItemMeta();

        meta.setLodestoneTracked(false);
        meta.setLodestone(front);
        Utils.applyGlint(meta, waystoneData.getDirection() == 180);

        item.setItemMeta(meta);
        inv.setItem(4, item);
        holder.slotToIndex.put(4, 1);

        item = named(Material.COMPASS, Component.text("West"));
        meta = (CompassMeta) item.getItemMeta();

        meta.setLodestoneTracked(false);
        meta.setLodestone(left);
        Utils.applyGlint(meta, waystoneData.getDirection() == 90);

        item.setItemMeta(meta);
        inv.setItem(12, item);
        holder.slotToIndex.put(12, 2);

        item = named(Material.LODESTONE, Component.text(waystoneData.getName()));
        inv.setItem(13, item);
        holder.slotToIndex.put(13, 3);

        item = named(Material.COMPASS, Component.text("East"));
        meta = (CompassMeta) item.getItemMeta();

        meta.setLodestoneTracked(false);
        meta.setLodestone(right);
        Utils.applyGlint(meta, waystoneData.getDirection() == -90);

        item.setItemMeta(meta);
        inv.setItem(14, item);
        holder.slotToIndex.put(14, 4);

        item = named(Material.COMPASS, Component.text("South"));
        meta = (CompassMeta) item.getItemMeta();

        meta.setLodestoneTracked(false);
        meta.setLodestone(back);
        Utils.applyGlint(meta, waystoneData.getDirection() == 0);

        item.setItemMeta(meta);
        inv.setItem(22, item);
        holder.slotToIndex.put(22, 5);


        player.openInventory(inv);
    }

    private static ItemStack named(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class DirectionHolder implements InventoryHolder {
        final Map<Integer, Integer> slotToIndex = new HashMap<>();
        final WaystoneData waystoneData;

        DirectionHolder(WaystoneData waystoneData) {
            this.waystoneData = waystoneData;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DirectionHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return; // ignore player inventory row

        Integer index = holder.slotToIndex.get(slot);
        if (index == null) return; // clicked a filler slot

        switch (index) {
            case 1:
                QuickWaystones.getWaystonesMap().get(holder.waystoneData.getLocation()).setDirection(180);
                player.sendMessage(Utils.formatString("<gold>" + holder.waystoneData.getName() + " " + QuickWaystones.getInstance().getConfig().getString("Messages.SetWaystoneNorth")));
                break;
            case 2:
                QuickWaystones.getWaystonesMap().get(holder.waystoneData.getLocation()).setDirection(90);
                player.sendMessage(Utils.formatString("<gold>" + holder.waystoneData.getName() + " " + QuickWaystones.getInstance().getConfig().getString("Messages.SetWaystoneWest")));

                break;
            case 4:
                QuickWaystones.getWaystonesMap().get(holder.waystoneData.getLocation()).setDirection(-90);
                player.sendMessage(Utils.formatString("<gold>" + holder.waystoneData.getName() + " " + QuickWaystones.getInstance().getConfig().getString("Messages.SetWaystoneEast")));

                break;
            case 5:
                QuickWaystones.getWaystonesMap().get(holder.waystoneData.getLocation()).setDirection(0);
                player.sendMessage(Utils.formatString("<gold>" + holder.waystoneData.getName() + " " + QuickWaystones.getInstance().getConfig().getString("Messages.SetWaystoneSouth")));
                break;
            default:
                return;
        }

        QuickWaystones.saveData();
        player.closeInventory();
    }
}