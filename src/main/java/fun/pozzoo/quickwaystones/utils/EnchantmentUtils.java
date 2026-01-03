package fun.pozzoo.quickwaystones.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

@SuppressWarnings("deprecation")
public class EnchantmentUtils {
    public static Enchantment getInfinityEnchant() {
        try {
            // 1.20.3+
            return Registry.ENCHANTMENT.get(
                    NamespacedKey.minecraft("infinity")
            );
        } catch (NoClassDefFoundError ignored) {
            // 1.20.0–1.20.2 fallback
            return Enchantment.getByKey(
                    NamespacedKey.minecraft("arrow_infinite")
            );
        }
    }


}
