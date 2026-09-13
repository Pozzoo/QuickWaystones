package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class OnExplode implements Listener {

    public OnExplode(QuickWaystones plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    public void handleExplosion(List<Block> blockList) {
        for (Block block : blockList) {
            if (block.getType() == Material.LODESTONE) {
                Block above = block.getRelative(BlockFace.UP);
                if (above.getType() == Material.BARRIER) {
                    above.setType(Material.AIR);
                }
            }
        }
    }
}
