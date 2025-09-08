package fun.pozzoo.quickwaystones.events;

import fun.pozzoo.quickwaystones.QuickWaystones;
import fun.pozzoo.quickwaystones.data.WaystoneData;
import fun.pozzoo.quickwaystones.gui.WaystoneGUI;
import fun.pozzoo.quickwaystones.utils.StringUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;

public class OnPlayerInteract implements Listener {
    private final QuickWaystones plugin;

    public OnPlayerInteract(QuickWaystones plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.LODESTONE) return;
        if (event.getPlayer().isSneaking()) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        QuickWaystones.getPlayerAccess().computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (!QuickWaystones.getWaystonesMap().containsKey(block.getLocation())) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneActivated")));
            QuickWaystones.createWaystone(block.getLocation(), new WaystoneData(block.getLocation(), player.getUniqueId()));

            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(block.getLocation()).getId());

            return;
        }

        if (this.plugin.getConfig().getBoolean("Settings.HideUndiscoveredWaystones")) {
            if (!QuickWaystones.getPlayerAccess().get(player.getUniqueId()).contains(QuickWaystones.getWaystonesMap().get(block.getLocation()).getId())) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneDiscovered")));
                QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(block.getLocation()).getId());
            }
        } else {
            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(block.getLocation()).getId());
        }

        if (event.getItem() == null || event.getItem().getType() != Material.NAME_TAG) {
            WaystoneGUI.runGUI(player);
        } else {
            TextComponent textComponent = (TextComponent) event.getItem().getItemMeta().displayName();

            if (textComponent == null) return;
            if (textComponent.content().equals(QuickWaystones.getWaystonesMap().get(block.getLocation()).getName())) return;

            QuickWaystones.getWaystone(block.getLocation()).setName(textComponent.content());
            player.getInventory().getItemInMainHand().subtract();
        }
    }
}
