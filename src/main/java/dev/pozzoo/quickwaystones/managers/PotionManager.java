package dev.pozzoo.quickwaystones.managers;

import dev.pozzoo.quickwaystones.data.WaystoneData;
import dev.pozzoo.quickwaystones.utils.Utils;
import io.papermc.paper.potion.PotionMix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

import static dev.pozzoo.quickwaystones.utils.Utils.applyGlint;

public class PotionManager {

    private final JavaPlugin plugin;
    NamespacedKey key;

    public PotionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "quickwaystones_potion");
    }

    public void registerPotion() {
        RecipeChoice input = PotionMix.createPredicateChoice(itemStack -> {
            if (itemStack.getType() != Material.POTION) return false;
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            if (meta == null) return false;
            return meta.getBasePotionType() == PotionType.AWKWARD;
        });

        RecipeChoice ingredient = new RecipeChoice.MaterialChoice(Material.ENDER_PEARL);

        List<String> lore = new ArrayList<>();

        lore.add("<italic><bold>" + this.plugin.getConfig().getString("Messages.Inactive") + "</bold></italic>");
        lore.add(this.plugin.getConfig().getString("Messages.TeleportationPotionLore1"));
        lore.add(this.plugin.getConfig().getString("Messages.TeleportationPotionLore2"));

        ItemStack result = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) result.getItemMeta();
        meta.setBasePotionType(PotionType.THICK);
        meta.setColor(Color.PURPLE);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "undrinkable");
        meta.itemName(Utils.formatItemName(this.plugin.getConfig().getString("Messages.TeleportationPotion")));
        meta.customName(Utils.formatItemName(this.plugin.getConfig().getString("Messages.TeleportationPotion")));
        meta.lore(Utils.formatStringList(lore));
        result.setItemMeta(meta);

        PotionMix mix = new PotionMix(key, result, input, ingredient);
        Bukkit.getPotionBrewer().addPotionMix(mix);
    }

    public static ItemStack getActivePotion(WaystoneData waystone, Plugin plugin, NamespacedKey key) {
        List<String> lore = new ArrayList<>();

        lore.add("<italic><bold>" + plugin.getConfig().getString("Messages.LinkedTo") + "</bold></italic> " + waystone.getName());

        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.THICK);
        meta.setColor(Color.PURPLE);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "drinkable");
        meta.itemName(Utils.formatItemName((plugin.getConfig().getString("Messages.LinkedTeleportationPotion"))));
        meta.customName(Utils.formatItemName((plugin.getConfig().getString("Messages.LinkedTeleportationPotion"))));
        meta.lore(Utils.formatStringList(lore));

        applyGlint(meta);

        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "potion_waystone_id"), PersistentDataType.INTEGER, waystone.getId());

        potion.setItemMeta(meta);

        return potion;
    }
}
