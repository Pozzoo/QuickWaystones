package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnBlockPlace implements Listener {

    public OnBlockPlace(QuickWaystones plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        if (event.getBlock().getType() != org.bukkit.Material.LODESTONE) return;

        Block above = event.getBlock().getRelative(BlockFace.UP);

        if (above.getType() != org.bukkit.Material.AIR) {
            event.setCancelled(true);
            return;
        }

        above.setType(Material.BARRIER);
    }
}
