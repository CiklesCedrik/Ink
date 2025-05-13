package de.cikles.ciklesmc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cikles.ciklesmc.utility.DataUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.cikles.ciklesmc.utility.Config.Translations.*;

public class Home {
    public static final long TIMEOUT = 36000;
    public static final LiteralArgumentBuilder<CommandSourceStack> HOMES = new Home.Homes();
    public static final LiteralArgumentBuilder<CommandSourceStack> SET_HOME = new Home.SetHome();
    public static final LiteralArgumentBuilder<CommandSourceStack> REMOVE_HOME = new Home.RemoveHome();
    private static final NamespacedKey HOME_KEY = NamespacedKey.fromString("homes");
    private static final NamespacedKey NAME_KEY = NamespacedKey.fromString("name");
    private static final NamespacedKey POSITION_KEY = NamespacedKey.fromString("position");
    private static final NamespacedKey WORLD_KEY = NamespacedKey.fromString("world");
    private static final NamespacedKey LAST_TP = NamespacedKey.fromString("last_tp");

    Home() {
    }

    private static CompletableFuture<Suggestions> getSuggestionsCompletableFuture(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Entity entity = context.getSource().getExecutor();
        DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().forEach(home -> {
            int[] position = DataUtil.get(home, POSITION_KEY, PersistentDataType.INTEGER_ARRAY);
            String name = DataUtil.get(home, NAME_KEY, PersistentDataType.STRING);
            if (builder.getRemainingLowerCase().isBlank() || name.startsWith(builder.getRemainingLowerCase()))
                builder.suggest(name, MessageComponentSerializer.message().serialize(Component.text("X:" + position[0] + " Y:" + position[1] + " Z:" + position[2], NamedTextColor.GRAY)));
        });
        return builder.buildFuture();
    }

    private static class SetHome extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

        protected SetHome() {
            super("sethome");
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument("name", StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument("name", String.class).trim().toLowerCase();
            List<PersistentDataContainer> homes = DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().filter(t -> !DataUtil.get(t, NAME_KEY, PersistentDataType.STRING).equals(targetHome)).collect(Collectors.toList());

            PersistentDataContainer home = entity.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            DataUtil.set(home, NAME_KEY, targetHome, PersistentDataType.STRING);
            DataUtil.set(home, WORLD_KEY, DataUtil.UUID.toPrimitive(entity.getWorld().getUID(), entity.getPersistentDataContainer().getAdapterContext()), PersistentDataType.LONG_ARRAY);
            DataUtil.set(home, POSITION_KEY, new int[]{entity.getLocation().blockX(), entity.getLocation().blockY(), entity.getLocation().blockZ()}, PersistentDataType.INTEGER_ARRAY);
            entity.sendMessage(Component.translatable(HOME_CREATED, NamedTextColor.YELLOW, Component.text(targetHome, NamedTextColor.GRAY)));
            homes.add(home);
            DataUtil.set(entity, HOME_KEY, homes, PersistentDataType.LIST.dataContainers());
            return Command.SINGLE_SUCCESS;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return builder.buildFuture();
        }
    }

    private static class Homes extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

        protected Homes() {
            super("home");
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument("name", StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument("name", String.class).trim().toLowerCase();
            Optional<PersistentDataContainer> homeOptional = DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().filter(c -> DataUtil.get(c, NAME_KEY, PersistentDataType.STRING).equals(targetHome)).findAny();
            if (homeOptional.isEmpty()) {
                context.getSource().getSender().sendMessage(Component.translatable(HOME_NOT_FOUND, NamedTextColor.RED, Component.text(targetHome, NamedTextColor.GRAY)));
                return Command.SINGLE_SUCCESS;
            }
            PersistentDataContainer home = homeOptional.get();
            int[] loc = DataUtil.get(home, POSITION_KEY, PersistentDataType.INTEGER_ARRAY);
            World world = Bukkit.getWorld(DataUtil.get(home, WORLD_KEY, DataUtil.UUID));
            long lastTP = DataUtil.getOrDefault(entity, LAST_TP, world.getGameTime() - TIMEOUT, PersistentDataType.LONG);
            if (world.getGameTime() - lastTP < TIMEOUT) {
                entity.sendMessage(Component.translatable(WAIT_BEFORE_TELEPORTING, NamedTextColor.RED, Component.text(15 - Math.round((world.getGameTime() - lastTP) / 1200D), NamedTextColor.YELLOW)));
                return SINGLE_SUCCESS;
            }
            entity.teleportAsync(new Location(world, loc[0], loc[1], loc[2]), PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(success -> {
                if (Boolean.FALSE.equals(success))
                    entity.sendMessage(Component.translatable(FAILED_TO_TELEPORT, NamedTextColor.RED));
                else {
                    DataUtil.set(entity, LAST_TP, world.getGameTime(), PersistentDataType.LONG);
                }
            });
            return Command.SINGLE_SUCCESS;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return getSuggestionsCompletableFuture(context, builder);
        }
    }

    private static class RemoveHome extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

        protected RemoveHome() {
            super("removehome");
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument("name", StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument("name", String.class).trim().toLowerCase();
            Optional<PersistentDataContainer> homeOptional = DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().filter(c -> DataUtil.get(c, NAME_KEY, PersistentDataType.STRING).equals(targetHome)).findAny();
            if (homeOptional.isEmpty()) {
                context.getSource().getSender().sendMessage(Component.translatable(HOME_NOT_FOUND, NamedTextColor.RED, Component.text(targetHome, NamedTextColor.GRAY)));
            } else {
                List<PersistentDataContainer> homes = new ArrayList<>(DataUtil.getSubArrayContainer(entity, HOME_KEY));
                homes.remove(homeOptional.get());
                DataUtil.set(entity, HOME_KEY, homes, PersistentDataType.LIST.dataContainers());
                entity.sendMessage(Component.translatable(HOME_REMOVED, NamedTextColor.RED, Component.text(targetHome, NamedTextColor.GRAY)));
            }
            return Command.SINGLE_SUCCESS;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return getSuggestionsCompletableFuture(context, builder);
        }

    }
}
