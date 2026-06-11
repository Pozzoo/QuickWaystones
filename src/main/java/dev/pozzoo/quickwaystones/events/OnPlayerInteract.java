package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.gui.DirectionGUI;
import dev.pozzoo.quickwaystones.gui.WaystoneGUI;
import dev.pozzoo.quickwaystones.utils.StringUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (this.plugin.getConfig().getBoolean("Settings.EnableWaystonePass") && item.getType() == Material.PAPER && QuickWaystones.getWaystonePass().checkKey(item) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            int waystoneID = QuickWaystones.getWaystonePass().getWaystoneID(item);

            if (QuickWaystones.getPlayerAccess().get(player.getUniqueId()).contains(waystoneID)) {
                player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1f, 1);
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneAlreadyDiscovered")));
                return;
            }

            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneDiscovered")));
            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(waystoneID);

            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.LODESTONE) return;
        if (event.getPlayer().isSneaking()) return;

        Location location = event.getClickedBlock().getLocation();

        event.setCancelled(true);

        QuickWaystones.getPlayerAccess().computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (!QuickWaystones.existsInMap(location)) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneActivated")));
            QuickWaystones.createWaystone(location, new WaystoneData(location, player.getUniqueId()));

            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(location).getId());

            QuickWaystones.saveData();

            return;
        }

        if (event.getItem() != null) {
            if (event.getItem().getType() == Material.COMPASS) {
                DirectionGUI.runGUI(player, QuickWaystones.getWaystonesMap().get(location));
                return;
            }

            if (event.getItem().getType() == Material.NAME_TAG) {
                TextComponent textComponent = (TextComponent) event.getItem().getItemMeta().displayName();

                if (textComponent == null) return;
                if (textComponent.content().equals(QuickWaystones.getWaystonesMap().get(location).getName()))
                    return;

                QuickWaystones.getWaystone(location).setName(textComponent.content());
                player.getInventory().getItemInMainHand().subtract();

                return;
            }

            if (event.getItem().getType() == Material.PAPER && this.plugin.getConfig().getBoolean("Settings.EnableWaystonePass")) {
                player.getInventory().getItemInMainHand().subtract();
                WaystoneData waystone = QuickWaystones.getWaystone(location);
                HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(QuickWaystones.getWaystonePass().createItem(waystone.getId()));

                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystonePassBound") + " " + waystone.getName()));

                toDrop.forEach((integer, itemStack) -> player.dropItem(itemStack));
                return;
            }
        }

        if (this.plugin.getConfig().getBoolean("Settings.HideUndiscoveredWaystones")) {
            if (!QuickWaystones.getPlayerAccess().get(player.getUniqueId()).contains(QuickWaystones.getWaystonesMap().get(location).getId())) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneDiscovered")));
                QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(location).getId());
            }
        } else {
            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(location).getId());
        }

        WaystoneGUI.runGUI(player, QuickWaystones.getWaystonesMap().get(location));
    }
}
