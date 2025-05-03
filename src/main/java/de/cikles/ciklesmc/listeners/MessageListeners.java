package de.cikles.ciklesmc.listeners;

import de.cikles.ciklesmc.utility.Config;
import de.cikles.discord.DiscordBot;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Pattern;

public class MessageListeners implements Listener {

    public static Component transformComponent(Component component) {
        if (Config.discord()) {
            assert DiscordBot.getJda() != null;
            component = component.replaceText(b -> {
                b.match(Pattern.compile("<#[a-fA-F0-9]{18,19}>"));
                b.replacement((matchResult, builder) -> {
                    TextChannel channel = DiscordBot.getJda().getTextChannelById(Long.parseLong(matchResult.group().substring(2, matchResult.group().length() - 1)));
                    if (channel == null)
                        return Component.text("<UNKNOWN CHANNEL>");
                    return Component.text("#" + channel.getName()).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, channel.getJumpUrl())).hoverEvent(HoverEvent.showText(Component.text("Guild: " + channel.getGuild().getName())));
                });
                b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
            }).replaceText(b -> {
                b.match(Pattern.compile("<@[a-fA-F0-9]{18,19}>"));
                b.replacement((matchResult, builder) -> {
                    User user = DiscordBot.getJda().getUserById(Long.parseLong(matchResult.group().substring(2, matchResult.group().length() - 1)));
                    if (user == null || user.getGlobalName() == null)
                        return Component.text("<UNKNOWN USER>");
                    return Component.text("@" + user.getGlobalName(), Style.style(TextColor.color(DiscordBot.getColorOfDiscordMember(user)), TextDecoration.UNDERLINED)).hoverEvent(HoverEvent.showText(Component.text(user.getName() + " (" + user.getId() + ")")));
                });
                b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
            }).replaceText(b -> {
                b.match(Pattern.compile("<@&[a-fA-F0-9]{18,19}>"));
                b.replacement((matchResult, builder) -> {
                    Role role = DiscordBot.getJda().getRoleById(Long.parseLong(matchResult.group().substring(3, matchResult.group().length() - 1)));
                    if (role == null)
                        return Component.text("<UNKNOWN ROLE>", Style.style(TextDecoration.UNDERLINED));
                    return Component.text("@" + role.getName(), Style.style(TextColor.color(role.getColorRaw()), TextDecoration.UNDERLINED)).hoverEvent(HoverEvent.showText(Component.text(role.getName() + " (" + role.getId() + ")")));
                });
                b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
            });
        }
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
