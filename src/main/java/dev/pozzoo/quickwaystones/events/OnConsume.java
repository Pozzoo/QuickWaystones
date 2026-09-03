package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class OnConsume implements Listener {
    NamespacedKey potionKey;
    NamespacedKey potionWaystoneKey;

    public OnConsume(QuickWaystones plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.potionKey = new NamespacedKey(plugin, "quickwaystones_potion");
        this.potionWaystoneKey = new NamespacedKey(plugin, "potion_waystone_id");
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {

        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String state = meta.getPersistentDataContainer().get(potionKey, PersistentDataType.STRING);

        assert state != null;
        if (state.equals("undrinkable")) {
            event.setCancelled(true);
            return;
        }

        if (state.equals("drinkable")) {
            onDrinkableConsumed(event.getPlayer(), meta);
        }
    }

    private void onDrinkableConsumed(Player player, ItemMeta meta) {
        player.getInventory().setItemInMainHand(null);
        player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));

        int waystoneId = meta.getPersistentDataContainer().getOrDefault(potionWaystoneKey, PersistentDataType.INTEGER, -1);

        WaystoneData data = QuickWaystones.getWaystone(waystoneId);

        if (data == null) {
            player.sendMessage(QuickWaystones.getInstance().getConfig().getString("Messages.WaystoneNotFound", "Waystone not found!"));
            player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1f, 1);

            return;
        }

        Utils.teleportPlayer(data, player);
    }

}
