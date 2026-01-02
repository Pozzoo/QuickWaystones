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

        Block block = event.getClickedBlock();

        event.setCancelled(true);

        QuickWaystones.getPlayerAccess().computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (!QuickWaystones.getWaystonesMap().containsKey(block.getLocation())) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneActivated")));
            QuickWaystones.createWaystone(block.getLocation(), new WaystoneData(block.getLocation(), player.getUniqueId()));

            QuickWaystones.getPlayerAccess().get(player.getUniqueId()).add(QuickWaystones.getWaystonesMap().get(block.getLocation()).getId());

            return;
        }

        if (event.getItem() != null) {
            if (event.getItem().getType() == Material.NAME_TAG) {
                TextComponent textComponent = (TextComponent) event.getItem().getItemMeta().displayName();

                if (textComponent == null) return;
                if (textComponent.content().equals(QuickWaystones.getWaystonesMap().get(block.getLocation()).getName()))
                    return;

                QuickWaystones.getWaystone(block.getLocation()).setName(textComponent.content());
                player.getInventory().getItemInMainHand().subtract();

                return;
            }

            if (event.getItem().getType() == Material.PAPER && this.plugin.getConfig().getBoolean("Settings.EnableWaystonePass")) {
                player.getInventory().getItemInMainHand().subtract();
                WaystoneData waystone = QuickWaystones.getWaystone(block.getLocation());
                HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(QuickWaystones.getWaystonePass().createItem(waystone.getId()));

                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystonePassBound") + " " + waystone.getName()));

                toDrop.forEach((integer, itemStack) -> player.dropItem(itemStack));
                return;
            }
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

        WaystoneGUI.runGUI(player);
    }
}
