package com.github.pozzoo.quickwaystones.events;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


public class OnBlockBreak implements Listener {
    public OnBlockBreak(QuickWaystones plugin) { Bukkit.getPluginManager().registerEvents(this, plugin); }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.LODESTONE) return;

        QuickWaystones.getWaystonesMap().remove(event.getBlock().getLocation());
    }
}
