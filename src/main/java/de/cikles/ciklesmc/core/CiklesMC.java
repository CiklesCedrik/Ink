package de.cikles.ciklesmc.core;

import de.cikles.ciklesmc.commands.Home;
import de.cikles.ciklesmc.commands.shop.Shop;
import de.cikles.ciklesmc.listeners.*;
import de.cikles.ciklesmc.listeners.mod_implementation.ModImplementation;
import de.cikles.ciklesmc.utility.Config;
import de.cikles.discord.DiscordBot;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class CiklesMC extends JavaPlugin {

    public static final TranslationStore<MessageFormat> translationRegistry =
            TranslationStore.messageFormat(Key.key("lang", "ciklesmc"));
    public static CiklesMC getInstance() {
        return (CiklesMC) getProvidingPlugin(CiklesMC.class);
    }

    private final ModImplementation modImpl = new ModImplementation();
    @Override
    public void onEnable() {
        try {

            // register pluginChannels for mod implementations
            modImpl.registerIncomingChannels(getServer().getMessenger());
            modImpl.registerOutgoingChannels(getServer().getMessenger());

            registerTranslations();
            registerListeners();
            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, CiklesMCBootstrap::registerCommands);

            // Start DiscordBot
            if (Config.discord()) DiscordBot.start();

        } catch (Exception e) {
            getSLF4JLogger().error("Can't enable {}! {}", this.getName(), e.getLocalizedMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        DiscordBot.stop();
        saveConfig();
        getServer().getOnlinePlayers().forEach(p -> p.activeBossBars().forEach(p::hideBossBar));
    }

    public void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        modImpl.registerListeners(pluginManager);
        if (Config.discord()) pluginManager.registerEvents(new DiscordListener(), this);
        if (Config.graves()) pluginManager.registerEvents(new GraveListener(), this);
        if (Config.enchantments())
            Config.enabledEnchantments().forEach(enchantment -> pluginManager.registerEvents(enchantment.ciklesEnchant, this));
        pluginManager.registerEvents(new FarmListener(), this);
        pluginManager.registerEvents(new MobGriefingListener(), this);
        pluginManager.registerEvents(new MessageListeners(), this);
        if (Config.isHomeEnabled())
            pluginManager.registerEvents(new Home(), this);
        if (Config.isShopEnabled())
            pluginManager.registerEvents(new Shop(), this);
    }

    private void registerTranslations() {
        translationRegistry.defaultLocale(Locale.US);

        File langFolder = new File(getDataFolder(), "lang");
        if (langFolder.mkdirs()) getSLF4JLogger().info("Created Translation folder {}", langFolder.getPath());
        if (langFolder.listFiles() == null) return;
        List<Locale> bundleFiles = Arrays.stream(Objects.requireNonNull(langFolder.listFiles(s -> s.getName().endsWith(".properties")))).map(f -> Locale.forLanguageTag(f.getName().substring(0, f.getName().length() - 11))).toList();
        bundleFiles.forEach(locale -> Config.registerLocale(translationRegistry, locale));

        if (!bundleFiles.contains(Locale.US)) Config.registerLocale(translationRegistry, Locale.US);
        if (!bundleFiles.contains(Locale.GERMANY)) Config.registerLocale(translationRegistry, Locale.GERMANY);

        GlobalTranslator.translator().addSource(translationRegistry);
    }


    @Override
    public void onLoad() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getConfig().options().parseComments(true);
    }
}