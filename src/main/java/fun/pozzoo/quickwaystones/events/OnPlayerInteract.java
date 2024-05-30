package fun.pozzoo.quickwaystones.events;

import fun.pozzoo.quickwaystones.QuickWaystones;
import fun.pozzoo.quickwaystones.data.WaystoneData;
import fun.pozzoo.quickwaystones.gui.WaystoneGUI;
import fun.pozzoo.quickwaystones.utils.StringUtils;
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

public class OnPlayerInteract implements Listener {
    private final QuickWaystones plugin;

    public OnPlayerInteract(QuickWaystones plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

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
                player.sendMessage(StringUtils.formatString("<gold>" + this.plugin.getConfig().getString("Messages.WaystoneActivated")));
                QuickWaystones.getWaystonesMap().put(block.getLocation(), new WaystoneData(block.getLocation(), player.getName()));
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
