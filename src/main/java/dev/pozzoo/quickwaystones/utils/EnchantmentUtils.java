package dev.pozzoo.quickwaystones.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("deprecation")
public class EnchantmentUtils {
    public static void applyGlint(ItemMeta meta) {
        try {
            meta.setEnchantmentGlintOverride(true);
        } catch (NoSuchMethodError ignored) {
            Enchantment unbreaking = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
            if (unbreaking != null) {
                meta.addEnchant(unbreaking, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    public static void applyGlint(ItemMeta meta, boolean glint) {
        try {
            meta.setEnchantmentGlintOverride(glint);
        } catch (NoSuchMethodError ignored) {
            Enchantment unbreaking = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
            if (unbreaking != null) {
                if (glint) {
                    meta.addEnchant(unbreaking, 1, true);
                } else {
                    meta.removeEnchant(unbreaking);
                }
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }
}
