package dev.pozzoo.quickwaystones.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Utils {
    public static Component formatString(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    public static List<Component> formatStringList(List<String> strings) {
        List<Component> components = new ArrayList<>();

        for (String string : strings) {
            components.add(formatString(string));
        }

        return components;
    }

    public static Component formatItemName(String itemName) {
        return MiniMessage.miniMessage().deserialize(itemName).decoration(TextDecoration.ITALIC, false);
    }

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
