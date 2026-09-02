package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.gui.DirectionGUI;
import dev.pozzoo.quickwaystones.gui.WaystoneGUI;
import dev.pozzoo.quickwaystones.managers.PotionManager;
import dev.pozzoo.quickwaystones.utils.Utils;
import java.util.HashMap;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

public class OnPlayerInteract implements Listener {

    private final QuickWaystones plugin;
    NamespacedKey key;

    public OnPlayerInteract(QuickWaystones plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.key = new NamespacedKey(plugin, "quickwaystones_potion");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (this.plugin.getConfig().getBoolean("Settings.EnableWaystonePass") &&
            item.getType() == Material.PAPER &&
            QuickWaystones.getWaystonePass().checkKey(item) &&
            (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                event.getAction() == Action.RIGHT_CLICK_AIR)
        ) {
            int waystoneID = QuickWaystones.getWaystonePass().getWaystoneID(
                item
            );

            if (QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).contains(waystoneID)) {
                player.playSound(
                    player,
                    Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,
                    0.1f,
                    1
                );
                player.sendMessage(Utils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneAlreadyDiscovered")));
                return;
            }

            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(Utils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneDiscovered")));
            QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).add(
                waystoneID
            );

            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.LODESTONE) return;
        if (event.getPlayer().isSneaking()) return;

        Location location = event.getClickedBlock().getLocation();

        event.setCancelled(true);

        QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId());

        if (!QuickWaystones.existsInMap(location)) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(Utils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneActivated")));
            QuickWaystones.createWaystone(
                location,
                new WaystoneData(location, player.getUniqueId())
            );

            QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).add(
                QuickWaystones.getWaystonesMap().get(location).getId()
            );

            QuickWaystones.saveData();

            return;
        }

        if (event.getItem() != null) {
            if (event.getItem().getType() == Material.COMPASS) {
                DirectionGUI.runGUI(
                    player,
                    QuickWaystones.getWaystonesMap().get(location)
                );
                return;
            }

            if (event.getItem().getType() == Material.NAME_TAG) {
                TextComponent textComponent = (TextComponent) event
                    .getItem()
                    .getItemMeta()
                    .displayName();

                if (textComponent == null) return;
                if (textComponent.content().equals(QuickWaystones.getWaystonesMap().get(location).getName())) return;

                QuickWaystones.getWaystone(location).setName(textComponent.content());
                player.getInventory().getItemInMainHand().subtract();

                return;
            }

            if (event.getItem().getType() == Material.POTION) {
                PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
                String state = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (state != null && !state.equals("undrinkable")) return;

                player.getInventory().setItemInMainHand(PotionManager.getActivePotion(QuickWaystones.getWaystone(location), plugin, key));
                player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

                String message = QuickWaystones.getInstance().getConfig().getString("Messages.TeleportationPotionActivated", "Potion was linked to {waystone}!");
                message = message.replace("{waystone}", QuickWaystones.getWaystone(location).getName());
                player.sendMessage(Utils.formatString("<gold>" + message));

                return;
            }

            if (event.getItem().getType() == Material.PAPER && this.plugin.getConfig().getBoolean("Settings.EnableWaystonePass")) {
                player.getInventory().getItemInMainHand().subtract();
                WaystoneData waystone = QuickWaystones.getWaystone(location);
                HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(QuickWaystones.getWaystonePass().createItem(waystone.getId()));

                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(Utils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystonePassBound") + " " + waystone.getName()));

                toDrop.forEach((integer, itemStack) ->
                    player.dropItem(itemStack)
                );
                return;
            }
        }

        if (this.plugin.getConfig().getBoolean("Settings.HideUndiscoveredWaystones")) {
            if (!QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).contains(QuickWaystones.getWaystonesMap().get(location).getId())) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(Utils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneDiscovered"))
                );
                QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(location).getId());
            }
        } else {
            QuickWaystones.getOrCreatePlayerAccess(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(location).getId());
        }

        WaystoneGUI.runGUI(
            player,
            QuickWaystones.getWaystonesMap().get(location)
        );
    }
}
