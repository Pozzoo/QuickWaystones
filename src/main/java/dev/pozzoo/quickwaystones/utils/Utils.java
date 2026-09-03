package dev.pozzoo.quickwaystones.utils;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.data.WaystoneData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

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

    public static boolean consumePlayerXp(Player player) {
        int xpCost = QuickWaystones.getInstance().getConfig().getInt("Settings.XpCost", 5);

        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            if (player.getLevel() < xpCost) {
                String message = QuickWaystones.getInstance().getConfig().getString("Messages.InsufficientXp", "You need {xp} XP level(s) to use this waystone!");
                message = message.replace("{xp}", String.valueOf(xpCost));
                player.sendMessage(Utils.formatString("<red>" + message));
                player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1f, 1);
                return false;
            }

            player.setLevel(player.getLevel() - xpCost);
        }

        return true;
    }

    public static void teleportPlayer(WaystoneData ws, Player player) {
        Vector locationModifier = new Vector(0.5, 0, 0.5);

        switch (ws.getDirection()) {
            case 0:
                locationModifier.setZ(1);
                break;
            case 90:
                locationModifier.setX(-1);
                break;
            case 180:
                locationModifier.setZ(-1);
                break;
            case -90:
                locationModifier.setX(1);
                break;
        }

        Location teleportLocation = ws.getLocation().clone().add(0.5, 0, 0.5);
        teleportLocation.setYaw(ws.getDirection());

        player.teleport(teleportLocation.add(locationModifier));
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 5);
        player.playSound(player, Sound.ENTITY_FOX_TELEPORT, 0.5f, 1f);
    }
}
