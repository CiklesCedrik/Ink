package de.cikles.ciklesmc.core;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.cikles.ciklesmc.commands.Home;
import de.cikles.ciklesmc.commands.Sit;
import de.cikles.ciklesmc.commands.shop.Shop;
import de.cikles.ciklesmc.enchantments.Enchantments;
import de.cikles.ciklesmc.utility.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemType;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.cikles.ciklesmc.core.CiklesMCBootstrap.CiklesCommands.*;
import static de.cikles.ciklesmc.enchantments.Enchantments.ENCHANTABLE_TOOL;
import static de.cikles.ciklesmc.enchantments.Vein.ORES;
import static net.kyori.adventure.key.Key.key;

public class CiklesMCBootstrap implements PluginBootstrap, PluginLoader {
    @Override
    public void bootstrap(BootstrapContext context) {
        final LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM), event -> {
            PostFlattenTagRegistrar<ItemType> registrar = event.registrar();
            List<TypedKey<ItemType>> enchantableTools = new ArrayList<>(registrar.getTag(ItemTypeTagKeys.ENCHANTABLE_MINING));
            enchantableTools.addAll(registrar.getTag(ItemTypeTagKeys.ENCHANTABLE_SHARP_WEAPON));
            registrar.setTag(ENCHANTABLE_TOOL, enchantableTools);
        });
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.BLOCK), event -> {
            PostFlattenTagRegistrar<BlockType> registrar = event.registrar();
            List<TypedKey<BlockType>> ores = new ArrayList<>(registrar.getTag(BlockTypeTagKeys.COAL_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.COPPER_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.IRON_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.GOLD_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.DIAMOND_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.EMERALD_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.REDSTONE_ORES));
            ores.addAll(registrar.getTag(BlockTypeTagKeys.LAPIS_ORES));
            ores.add(BlockTypeKeys.NETHER_QUARTZ_ORE);
            ores.add(BlockTypeKeys.ANCIENT_DEBRIS);
            registrar.setTag(ORES, ores);
        });
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> List.of(Enchantments.values()).forEach(enchantments -> event.registry().register(enchantments.enchantmentTypedKey, c -> enchantments.ciklesEnchant.register(event, c)))));
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> event.registrar().addToTag(EnchantmentTagKeys.create(key("ciklesmc:enchantment")), Stream.of(Enchantments.values()).map(e -> e.enchantmentTypedKey).toList()));

    }

    static void registerCommands(ReloadableRegistrarEvent<Commands> registrar) {
        Commands commands = registrar.registrar();
        commands.register(SIT);
        if (Config.isHomeEnabled()) {
            commands.register(HOME);
            commands.register(SET_HOME);
            commands.register(REMOVE_HOME);
        }
        if (Config.isShopEnabled())
            commands.register(SHOP);
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("net.dv8tion:JDA:5.3.2"), null));
        classpathBuilder.addLibrary(resolver);
    }

    public class CiklesCommands {

        public static final LiteralCommandNode<CommandSourceStack> SIT = new Sit().build();
        public static final LiteralCommandNode<CommandSourceStack> HOME = Home.HOMES.build();
        public static final LiteralCommandNode<CommandSourceStack> SET_HOME = Home.SET_HOME.build();
        public static final LiteralCommandNode<CommandSourceStack> REMOVE_HOME = Home.REMOVE_HOME.build();
        public static final LiteralCommandNode<CommandSourceStack> SHOP = new Shop().build();

        CiklesCommands() {
        }
    }
}