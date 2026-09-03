package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
        if (event.getBlock().getType() == Material.LODESTONE) {

            event.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);

            Player player = event.getPlayer();
            WaystoneData waystone = QuickWaystones.getWaystonesMap().get(event.getBlock().getLocation());

            if (waystone == null) return;

            if (player.isOp() || player.getUniqueId().equals(waystone.getOwner())) {
                QuickWaystones.removeWaystone(event.getBlock().getLocation());
                QuickWaystones.removeAccess(waystone.getId());

                QuickWaystones.saveData();
                return;
            }

            event.setCancelled(true);
            player.sendMessage(Utils.formatString("<Red>" + this.plugin.getConfig().getString("Messages.WaystoneBrokenByOther")));
        }

        if (event.getBlock().getType() == Material.BARRIER) {
            if (event.getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial() != Material.LODESTONE) return;

            Block down = event.getBlock().getRelative(BlockFace.DOWN);

            event.getBlock().getWorld().spawnParticle(
                    Particle.BLOCK_CRUMBLE,
                    down.getLocation().add(0.5, 0.5, 0.5),
                    30,
                    0.4,
                    0.3,
                    0.4,
                    2.0,
            down.getBlockData());

            down.setType(Material.AIR);

            WaystoneData waystone = QuickWaystones.getWaystonesMap().get(event.getBlock().getLocation());

            if (waystone == null) return;

            QuickWaystones.removeWaystone(event.getBlock().getLocation());
            QuickWaystones.removeAccess(waystone.getId());

            QuickWaystones.saveData();
        }
    }
}
