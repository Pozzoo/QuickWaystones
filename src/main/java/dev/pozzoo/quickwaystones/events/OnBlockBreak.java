package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


public class OnBlockBreak implements Listener {
    private final QuickWaystones plugin;

    public OnBlockBreak(QuickWaystones plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.LODESTONE) return;

        Player player = event.getPlayer();
        WaystoneData waystone = QuickWaystones.getWaystonesMap().get(event.getBlock().getLocation());

        if (waystone == null) return;

        if (player.isOp() || player.getUniqueId().equals(waystone.getOwner())) {
            QuickWaystones.removeWaystone(event.getBlock().getLocation());
            return;
        }

        event.setCancelled(true);
        player.sendMessage(StringUtils.formatString("<Red>" + this.plugin.getConfig().getString("Messages.WaystoneBrokenByOther")));
    }
}
