package de.cikles.ciklesmc.utility;

import de.cikles.ciklesmc.core.CiklesMC;
import de.cikles.ciklesmc.enchantments.Enchantments;
import de.cikles.discord.DiscordBot;
import io.papermc.paper.chat.ChatRenderer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static de.cikles.ciklesmc.listeners.MessageListeners.transformComponent;

@SuppressWarnings({"unused"})
public class Config {
    private static boolean discord;
    private static @Nullable String token;
    private static long channel;
    private static @Nullable String webhookUrl;
    private static @Nullable String serverImage;
    private static @Nullable Component playerJoin;
    private static @Nullable Component playerQuit;
    private static @Nullable Component chat;
    private static boolean enchantments;
    private static boolean professions;
    private static boolean creeperExplosions;
    private static boolean ghastExplosions;
    private static boolean enderman;
    private static boolean graves;
    private static ChatRenderer renderer;

    static {
        load();
    }

    private Config() {
    }


    public static boolean discord() {
        return discord;
    }

    public static @Nullable String discordToken() {
        return token;
    }

    public static long discordChannelId() {
        return channel;
    }

    public static @Nullable String discordWebhookUrl() {
        return webhookUrl;
    }

    public static @Nullable String discordServerImage() {
        return serverImage;
    }

    public static @Nullable Component joinMessage(Player player) {
        return getComponent(playerJoin, player.name());
    }

    public static @Nullable Component quitMessage(Player player) {
        return getComponent(playerQuit, player.name());
    }

    private static @Nullable Component getComponent(Component message, Component playerName) {
        if (message == null) return null;
        return message.replaceText(b -> {
            b.match(Pattern.compile("%player%"));
            b.replacement((matchResult, builder) -> playerName);
            b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
        });
    }

    public static boolean enchantments() {
        return enchantments;
    }

    public static boolean professions() {
        return professions;
    }

    public static boolean creeperExplosions() {
        return creeperExplosions;
    }

    public static boolean ghastExplosions() {
        return ghastExplosions;
    }

    public static boolean enderman() {
        return enderman;
    }

    public static boolean graves() {
        return graves;
    }


    public static void load() {
        FileConfiguration config = CiklesMC.getInstance().getConfig();

        discord = config.getBoolean("discord.enabled");
        token = getString("discord.token");
        if (token == null || token.isBlank()) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Discord-Sync was disabled because no token was defined!");
            discord = false;
        } else if (token.matches("^[A-Za-z0-9]{24}\\.[A-Za-z0-9_-]{6}\\.[A-Za-z0-9_-]{27}$")) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Discord-Sync was disabled because given token isn't valid!");
            discord = false;
        }
        channel = config.getLong("discord.channel");
        webhookUrl = getString("discord.webhook-url");
        serverImage = getString("discord.server-image");

        playerJoin = getComponent("messages.player-join");
        playerQuit = getComponent("messages.player-quit");
        chat = getComponent("messages.chat");
        if (chat == null) renderer = null;

        enchantments = config.getBoolean("enchantments.enabled");

        professions = config.getBoolean("professions.enabled");

        creeperExplosions = config.getBoolean("mob-griefing.creeper-explosions");

        ghastExplosions = config.getBoolean("mob-griefing.ghast-explosions");

        enderman = config.getBoolean("mob-griefing.enderman");

        graves = config.getBoolean("graves");

    }

    private static @Nullable String getString(String path) {
        String string = CiklesMC.getInstance().getConfig().getString(path);
        if (string == null || string.equalsIgnoreCase("null")) return null;
        return string;
    }

    private static @Nullable Component getComponent(String path) {
        String message = getString(path);
        if (message == null || message.isBlank()) return null;
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static @Nullable ChatRenderer chatRenderer() {
        if (chat == null) return null;
        if (renderer == null) renderer = new ChatRenderer() {
            @Override
            public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
                return chat.replaceText(b -> {
                    b.match(Pattern.compile("%source%"));
                    b.replacement((matchResult, builder) -> source.name());
                    b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
                }).replaceText(b -> {
                    b.match(Pattern.compile("%sourceDisplayName%"));
                    b.replacement((matchResult, builder) -> sourceDisplayName);
                    b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
                }).replaceText(b -> {
                    b.match(Pattern.compile("%message%"));
                    b.replacement((matchResult, builder) -> transformComponent(message));
                    b.condition((index, replaced) -> PatternReplacementResult.REPLACE);
                });
            }
        };
        return renderer;
    }

    public static List<Enchantments> enabledEnchantments() {
        if (!enchantments()) return List.of();
        ArrayList<Enchantments> list = new ArrayList<>(1);
        if (CiklesMC.getInstance().getConfig().getBoolean("enchantments.telekinesis"))
            list.add(Enchantments.TELEKINESIS);
        return list;
    }

    public static @Nullable GuildMessageChannel discordChannel() {
        if (DiscordBot.getJda() == null) throw new NullPointerException("Discord Bot not initialized!");
        return DiscordBot.getJda().getChannelById(GuildMessageChannel.class, channel);
    }


    public static @NotNull Guild getGuild() {
        GuildMessageChannel channel = discordChannel();
        if (channel == null) throw new NullPointerException("Discord-Channel not Specified!");
        return channel.getGuild();
    }

    public static void registerLocale(TranslationRegistry registry, Locale locale) {
        File externalFile = new File(new File(CiklesMC.getInstance().getDataFolder(), "lang"), locale.toLanguageTag() + ".properties");
        String internalPath = "/lang/Lang_" + locale.toLanguageTag().replace('-', '_') + ".properties";
        try {
            Properties merged = mergeWithInternalDefaults(externalFile, internalPath);
            ResourceBundle bundle = new PropertyResourceBundle(new StringReader(toPropertyString(merged)));
            registry.registerAll(locale, bundle, true);
        } catch (IOException e) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Failed to register locale", e);
        }
    }

    public static Properties mergeWithInternalDefaults(File externalFile, String internalResourcePath) throws IOException {
        Properties internal = new Properties();
        Properties external = new Properties();
        boolean externalPresent = externalFile.exists();
        // Load external file if present
        if (externalPresent)
            try (InputStream in = new FileInputStream(externalFile)) {
                external.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }

        // Load internal properties
        try (InputStream in = Config.class.getResourceAsStream(internalResourcePath)) {
            if (in != null)
                internal.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            else if (!externalPresent) throw new IOException("No internal or external file Present!");
            else return external;
        }
        // Add all missing keys from internal bundle
        boolean updated = false;
        for (String key : internal.stringPropertyNames()) {
            if (!external.containsKey(key)) {
                external.setProperty(key, internal.getProperty(key));
                updated = true;
            }
        }
        // Remove Unused keys
        for (String key : external.stringPropertyNames()) {
            if (!internal.containsKey(key)) {
                external.remove(key);
                updated = true;
            }
        }

        // If file doesn't exist or was updated -> save
        if (updated || !externalFile.exists()) {
            try (OutputStream out = new FileOutputStream(externalFile)) {
                external.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "Generated or Updated by CiklesMC");
            }
        }

        return external;
    }

    public static String toPropertyString(Properties props) throws IOException {
        StringWriter writer = new StringWriter();
        props.store(writer, null);
        return writer.toString();
    }

    public static class Translations {
        public static final String GRAVE_FAILED = "core.grave_failed";
        public static final String GRAVE_SPAWNED = "core.grave_spawned";
        public static final String GRAVE_REMOVED_BY = "core.grave_removed_by";
        public static final String FAILED_TO_TELEPORT = "core.failed_to_teleport";
        public static final String WAIT_BEFORE_TELEPORTING = "core.wait_before_teleporting";
        public static final String PLAYER_NOT_CONNECTED = "commands.player_not_connected";
        public static final String NOT_ENOUGH_MONEY = "commands.economy.not_enough_money";
        public static final String HOME_NOT_FOUND = "commands.home.not_found";
        public static final String HOME_CREATED = "commands.home.created";
        public static final String HOME_REMOVED = "commands.home.removed";
        public static final String SHOP_TITLE_VILLAGER_TRADES = "commands.shop.title.villager_trades";
        public static final String SHOP_NEXT_PAGE = "commands.shop.redirect.next_page";
        public static final String SHOP_PREVIOUS_PAGE = "commands.shop.redirect.previous_page";
        public static final String SHOP_MAIN_PAGE = "commands.shop.redirect.main";
        public static final String NOT_AVAILABLE_YET = "commands.shop.not_available";
        public static final String SHOP_ENCHANTMENT = "commands.shop.title.enchantments";

        Translations() {
        }

    }

}
