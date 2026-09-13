package dev.pozzoo.quickwaystones.events;

import dev.pozzoo.quickwaystones.QuickWaystones;
import dev.pozzoo.quickwaystones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnPlayerJoin implements Listener {

    public OnPlayerJoin(QuickWaystones plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    //TODO: CHANGE URL TO MAIN BRANCH BEFORE MERGING
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            event.getPlayer().addResourcePack(UUID.randomUUID(), "https://github.com/Pozzoo/QuickWaystones/raw/refs/heads/v2.3.0/QuickWaystones%20Pack.zip", Utils.hexToBytes("8e6142679a507e25ea67cf0165ea44afbaa05ba7"), QuickWaystones.getInstance().getConfig().getString("Messages.ResourcePackPrompt", "Download QuickWaystones Resource Pack?"), QuickWaystones.getInstance().getConfig().getBoolean("Settings.ForceResourcePack"));
        } catch (NoSuchMethodError ignored) {
            event.getPlayer().setResourcePack("https://github.com/Pozzoo/QuickWaystones/raw/refs/heads/v2.3.0/QuickWaystones%20Pack.zip", Utils.hexToBytes("8e6142679a507e25ea67cf0165ea44afbaa05ba7"));
        }
    }
}
