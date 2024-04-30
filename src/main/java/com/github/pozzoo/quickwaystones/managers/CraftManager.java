package com.github.pozzoo.quickwaystones.managers;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CraftManager {
    public void registerRecipes() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(QuickWaystones.getInstance(), "waypoint"), new ItemStack(Material.LODESTONE));
        recipe.shape("SSS", "SES", "SSS");
        recipe.setIngredient('S', Material.CHISELED_STONE_BRICKS);
        recipe.setIngredient('E', Material.ENDER_PEARL);

        QuickWaystones.getInstance().getServer().addRecipe(recipe);
    }
}
