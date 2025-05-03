package de.cikles.ciklesmc.listeners;

import de.cikles.ciklesmc.core.CiklesMC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class XaeroImplementation implements Listener {
    public static final String XAEROWORLDMAP = "xaeroworldmap:main";
    public static final String XAEROMINIMAP = "xaerominimap:main";

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        var channel = event.getChannel();
        if (channel.equals(XAEROWORLDMAP) || channel.equals(XAEROMINIMAP))
            event.getPlayer().sendPluginMessage(CiklesMC.getInstance(), channel, new byte[]{0, 39, 12, -27, -107});
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        player.sendPluginMessage(CiklesMC.getInstance(), XAEROWORLDMAP, new byte[]{0, 39, 12, -27, -107});
        player.sendPluginMessage(CiklesMC.getInstance(), XAEROMINIMAP, new byte[]{0, 39, 12, -27, -107});
    }
}
