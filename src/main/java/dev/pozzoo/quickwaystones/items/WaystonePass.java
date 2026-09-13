package dev.pozzoo.quickwaystones.items;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

import static dev.pozzoo.quickwaystones.utils.Utils.applyGlint;

public class WaystonePass {

    private final ItemStack item;
    private final NamespacedKey pass_key;
    private final NamespacedKey bound_waystone_key;

    public WaystonePass(QuickWaystones plugin, String pass_key, String bound_waystone_key) {
        this.pass_key = new NamespacedKey(plugin, pass_key);
        this.bound_waystone_key = new NamespacedKey(plugin, bound_waystone_key);

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Utils.formatString("Waystone Pass"));
            meta.lore(Utils.formatStringList(Collections.singletonList("Right click to discover the assigned waystone!")));

            applyGlint(meta);

            meta.getPersistentDataContainer().set(this.pass_key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }

        this.item = item;
    }

    public ItemStack createItem(int waystoneID) {
        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(this.bound_waystone_key, PersistentDataType.INTEGER, waystoneID);
        item.setItemMeta(meta);

        return item;
    }

    public boolean checkKey(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(this.pass_key,  PersistentDataType.BYTE) != null;
    }

    public Integer getWaystoneID(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(this.bound_waystone_key, PersistentDataType.INTEGER);
    }
}
