package com.github.pozzoo.quickwaystones.gui;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import com.github.pozzoo.quickwaystones.data.WaystoneData;
import com.github.pozzoo.quickwaystones.utils.StringUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public class WaystoneGUI {
    public static void runGUI(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(StringUtils.formatString("Waystones"))
                .rows(4)
                .pageSize(27)
                .create();

        Map<Location, WaystoneData> waystones = QuickWaystones.getWaystonesMap();

        for (WaystoneData waystone : waystones.values()) {
            GuiItem item = ItemBuilder.from(Material.ENDER_PEARL).name(StringUtils.formatItemName(waystone.getName())).asGuiItem(inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                player.teleport(waystone.getLocation().clone().add(0.5, 1, 0.5));
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 5);
                player.playSound(player, Sound.ENTITY_FOX_TELEPORT, 0.5f, 1f);
                player.closeInventory();
            });
            gui.addItem(item);
        }

        if (gui.getPagesNum() > 1) {
            gui.setItem(4, 3, ItemBuilder.from(Material.PAPER).name(StringUtils.formatString("Previous")).asGuiItem(event -> {
                event.setCancelled(true);
                gui.previous();
            }));

            gui.setItem(4, 7, ItemBuilder.from(Material.PAPER).name(StringUtils.formatString("Next")).asGuiItem(event -> {
                event.setCancelled(true);
                gui.next();
            }));
        }

        gui.open(player);
    }
}
