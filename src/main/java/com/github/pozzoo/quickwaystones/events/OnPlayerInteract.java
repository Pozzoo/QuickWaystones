package com.github.pozzoo.quickwaystones.events;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import com.github.pozzoo.quickwaystones.data.WaystoneData;
import com.github.pozzoo.quickwaystones.gui.WaystoneGUI;
import com.github.pozzoo.quickwaystones.utils.StringUtils;
import net.kyori.adventure.text.Component;
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
import org.bukkit.metadata.FixedMetadataValue;

public class OnPlayerInteract implements Listener {
    public OnPlayerInteract(QuickWaystones plugin) { Bukkit.getPluginManager().registerEvents(this, plugin); }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.LODESTONE) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getItem() == null) {
            if (!QuickWaystones.getWaystonesMap().containsKey(block.getLocation())) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.sendMessage(StringUtils.formatString("<gold> Waystone Activated"));
                QuickWaystones.getWaystonesMap().put(block.getLocation(), new WaystoneData(block.getLocation()));
                return;
            }

            WaystoneGUI.runGUI(player);
            return;
        }

        if (event.getItem().getType() == Material.NAME_TAG) {
            TextComponent textComponent = (TextComponent) event.getItem().getItemMeta().displayName();

            if (textComponent == null) return;

            QuickWaystones.getWaystonesMap().get(block.getLocation()).setName(textComponent.content());
            player.getInventory().getItemInMainHand().subtract();
        }


    }
}
