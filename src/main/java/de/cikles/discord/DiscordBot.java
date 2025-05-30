package de.cikles.discord;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.cikles.ciklesmc.core.CiklesMC;
import de.cikles.ciklesmc.utility.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

public class DiscordBot {

    private static JDA jda;

    private DiscordBot() {
    }

    public static @Nullable JDA getJda() {
        return jda;
    }

    public static void start() {
        if (jda != null) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Bot is already Running! To fix this restart Server");
            Bukkit.getPluginManager().disablePlugin(CiklesMC.getInstance());
            return;
        }
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), r -> {
            JDABuilder builder = JDABuilder.create(Config.discordToken(), GUILD_MESSAGES, MESSAGE_CONTENT);
            builder.setMemberCachePolicy(MemberCachePolicy.NONE);
            builder.setChunkingFilter(ChunkingFilter.NONE);
            builder.disableCache(ACTIVITY, CLIENT_STATUS, ONLINE_STATUS, EMOJI, STICKER, VOICE_STATE, SCHEDULED_EVENTS, FORUM_TAGS, MEMBER_OVERRIDES, ROLE_TAGS);
            builder.setStatus(OnlineStatus.ONLINE);
            builder.addEventListeners(new ListenerAdapter() {

                @Override
                public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                    if (!event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage())
                        return;
                    if (event.getChannel().getIdLong() != Config.discordChannelId()) return;
                    Role role = getHighestRoleOfDiscordMember(event.getAuthor());
                    Bukkit.getServer().sendMessage(Component.text("[DISCORD] ", NamedTextColor.AQUA).append(Component.text(event.getAuthor().getEffectiveName(), TextColor.color(role.getColorRaw())).hoverEvent(HoverEvent.showText(Component.text(role.getName()).color(TextColor.color(role.getColorRaw()))))).append(Component.text(": " + event.getMessage().getContentRaw(), NamedTextColor.GRAY)));
                }

            });
            jda = builder.build();
        });
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdownNow();
        }
    }

    public static void sendPlayerMessage(String name, String msg) {
        send(name, msg, "https://mc-heads.net/avatar/" + name);
    }

    public static void sendServerMessage(String msg) {
        send("Server", msg, Config.discordServerImage());
    }

    public static void send(String name, String message, String avatarUrl) {
        final String msg = message.replaceAll("&[0-9a-fr]|§[0-9a-fr]|#[a-fA-F0-9]{6}", "");
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), r -> {
            if (Config.discordWebhookUrl() == null) {
                sendOverBot(name, msg, avatarUrl);
                return;
            }

            Request.Builder bu = new Request.Builder();
            JsonObject json = new JsonObject();
            json.add("content", new JsonPrimitive(msg));
            if (avatarUrl != null) json.add("avatar_url", new JsonPrimitive(avatarUrl));
            json.add("username", new JsonPrimitive(name));
            bu.url(Config.discordWebhookUrl());
            bu.post(RequestBody.create(json.toString(), MediaType.get("application/json")));
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(bu.build());
            try (Response res = call.execute()) {
                ResponseBody body = res.body();
                if (res.code() >= 300) {
                    if (body != null)
                        throw new IOException("Response Code " + res.code() + " " + body.string());
                    throw new IOException("Response Code " + res.code());
                }
            } catch (IOException e) {
                CiklesMC.getInstance().getSLF4JLogger().warn("Failed to post webhook on discord: {}", e.getLocalizedMessage(), e);
                sendOverBot(name, msg, avatarUrl);
            }
        });
    }

    public static void sendOverBot(String name, String msg, String avatarUrl) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(name);
        builder.setThumbnail(avatarUrl);
        builder.setDescription(msg.substring(0, 4096));
        Config.discordChannel();
    }

    public static @NotNull Role getHighestRoleOfDiscordMember(@Nullable User user) {
        Guild guild = Config.getGuild();
        if (user == null) return guild.getPublicRole();
        return getHighestRoleOfDiscordMember(guild.getMember(user));
    }

    public static @NotNull Role getHighestRoleOfDiscordMember(@Nullable Member member) {
        if (member == null) return Config.getGuild().getPublicRole();
        return member.getRoles().stream().filter(role -> !role.isPublicRole())
                // Filter Coloring -> No default colors
                .filter(role -> role.getColorRaw() != 0x000000 && role.getColorRaw() != 0x99AAB5).min((x, y) -> Integer.compare(y.getPosition(), x.getPosition())).orElse(member.getGuild().getPublicRole());
    }

    public static int getColorOfDiscordMember(@Nullable User user) {
        if (user == null) return 0xA0A0A0;
        Guild guild = Config.getGuild();
        return getColorOfDiscordMember(guild.getMember(user));
    }

    public static int getColorOfDiscordMember(@Nullable Member member) {
        if (member == null) return 0xA0A0A0;
        return getHighestRoleOfDiscordMember(member).getColorRaw();
    }
}
