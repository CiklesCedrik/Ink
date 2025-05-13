package de.cikles.ciklesmc.listeners;

import de.cikles.discord.DiscordBot;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class DiscordListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Component message = e.joinMessage();
        if (message == null) return;
        DiscordBot.sendServerMessage(LegacyComponentSerializer.legacySection().serialize(message));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Component message = e.quitMessage();
        if (message == null) return;
        DiscordBot.sendServerMessage(LegacyComponentSerializer.legacySection().serialize(message));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event) {
        DiscordBot.sendPlayerMessage(event.getPlayer().getName(), LegacyComponentSerializer.legacySection().serialize(event.message()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Component message = event.deathMessage();
        if (message == null) return;
        DiscordBot.sendServerMessage(LegacyComponentSerializer.legacySection().serialize(message));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        if (Objects.isNull(e.getAdvancement().getDisplay())) return;
        if (!e.getAdvancement().getDisplay().doesAnnounceToChat()) return;
        Component message = e.message();
        if (message == null) return;
        DiscordBot.sendServerMessage(LegacyComponentSerializer.legacySection().serialize(message));
    }
}
