package de.cikles.ciklesmc.listeners;

import de.cikles.ciklesmc.utility.Config;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Pattern;

public class MessageListeners implements Listener {

    public static Component transformComponent(Component component) {
        return component.replaceText(b -> {
            b.match(Pattern.compile("#[a-fA-F0-9]{6}.*"));
            b.replacement((matchResult, builder) -> {
                String target = Pattern.compile("#[a-fA-F0-9]{6}").split(matchResult.group().substring(7))[0];
                TextColor color = TextColor.fromHexString(matchResult.group().substring(0, 7));
                return Component.text(target, color).append(Component.text(matchResult.group().substring(7 + target.length())));
            });
            b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Component message = Config.joinMessage(e.getPlayer());
        if (message != null) e.joinMessage(message);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Component message = Config.quitMessage(e.getPlayer());
        if (message != null) e.quitMessage(message);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        ChatRenderer renderer = Config.chatRenderer();
        if (renderer == null) return;
        event.renderer(renderer);
    }


}
