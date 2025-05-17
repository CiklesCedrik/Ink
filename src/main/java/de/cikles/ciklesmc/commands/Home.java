package de.cikles.ciklesmc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cikles.ciklesmc.core.CiklesMC;
import de.cikles.ciklesmc.utility.Config;
import de.cikles.ciklesmc.utility.DataUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.cikles.ciklesmc.utility.Config.Translations.*;

public class Home implements Listener {
    public static final LiteralArgumentBuilder<CommandSourceStack> HOMES = new Home.Homes();
    public static final LiteralArgumentBuilder<CommandSourceStack> SET_HOME = new Home.SetHome();
    public static final LiteralArgumentBuilder<CommandSourceStack> REMOVE_HOME = new Home.RemoveHome();
    private static final NamespacedKey HOME_KEY = NamespacedKey.fromString("homes");
    private static final String NAME_ARGUMENT = "name";
    private static final NamespacedKey NAME_KEY = NamespacedKey.fromString(NAME_ARGUMENT);
    private static final NamespacedKey POSITION_KEY = NamespacedKey.fromString("position");
    private static final NamespacedKey WORLD_KEY = NamespacedKey.fromString("world");
    private static final NamespacedKey LAST_TP = NamespacedKey.fromString("last_tp");

    private static final ArrayList<Entity> pendingTeleports = new ArrayList<>();

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())
            pendingTeleports.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) pendingTeleports.remove(player);
    }

    private static class SetHome extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

        protected SetHome() {
            super("sethome");
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument(NAME_ARGUMENT, StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument(NAME_ARGUMENT, String.class).trim().toLowerCase();
            List<PersistentDataContainer> homes = DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().filter(t -> !DataUtil.get(t, NAME_KEY, PersistentDataType.STRING).equals(targetHome)).collect(Collectors.toList());

            if (homes.size() < Config.getHomePerPlayer()) {
                PersistentDataContainer home = entity.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
                DataUtil.set(home, NAME_KEY, targetHome, PersistentDataType.STRING);
                DataUtil.set(home, WORLD_KEY, DataUtil.UUID.toPrimitive(entity.getWorld().getUID(), entity.getPersistentDataContainer().getAdapterContext()), PersistentDataType.LONG_ARRAY);
                DataUtil.set(home, POSITION_KEY, new int[]{entity.getLocation().blockX(), entity.getLocation().blockY(), entity.getLocation().blockZ()}, PersistentDataType.INTEGER_ARRAY);
                entity.sendMessage(Component.translatable(HOME_CREATED, NamedTextColor.YELLOW, Component.text(targetHome, NamedTextColor.GRAY)));
                homes.add(home);
                DataUtil.set(entity, HOME_KEY, homes, PersistentDataType.LIST.dataContainers());
            } else
                entity.sendMessage(Component.translatable(HOME_REACHED_MAXIMUM, NamedTextColor.RED));
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
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument(NAME_ARGUMENT, StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument(NAME_ARGUMENT, String.class).trim().toLowerCase();
            Optional<PersistentDataContainer> homeOptional = DataUtil.getSubArrayContainer(entity, HOME_KEY).stream().filter(c -> DataUtil.get(c, NAME_KEY, PersistentDataType.STRING).equals(targetHome)).findAny();
            if (homeOptional.isEmpty()) {
                context.getSource().getSender().sendMessage(Component.translatable(HOME_NOT_FOUND, NamedTextColor.RED, Component.text(targetHome, NamedTextColor.GRAY)));
                return Command.SINGLE_SUCCESS;
            }
            PersistentDataContainer home = homeOptional.get();
            int[] loc = DataUtil.get(home, POSITION_KEY, PersistentDataType.INTEGER_ARRAY);
            World world = Bukkit.getWorld(DataUtil.get(home, WORLD_KEY, DataUtil.UUID));
            long lastTP = DataUtil.getOrDefault(entity, LAST_TP, world.getGameTime() - Config.getHomeCooldown() * 1200L, PersistentDataType.LONG);
            if (world.getGameTime() - lastTP < Config.getHomeCooldown() * 1200L) {
                entity.sendMessage(Component.translatable(WAIT_BEFORE_TELEPORTING, NamedTextColor.RED, Component.text(Config.getHomeCooldown()), Component.text(Config.getHomeCooldown() - Math.round((world.getGameTime() - lastTP) / 1200D), NamedTextColor.YELLOW)));
                return SINGLE_SUCCESS;
            }
            pendingTeleports.add(entity);
            entity.sendMessage(Component.translatable(PENDING_TELEPORT, NamedTextColor.RED));
            AtomicInteger timeout = new AtomicInteger(Config.getHomeTimeout());
            Bukkit.getAsyncScheduler().runAtFixedRate(CiklesMC.getInstance(), t -> {
                if (!pendingTeleports.contains(entity)) {
                    entity.sendMessage(Component.translatable(TELEPORT_CANCELED, NamedTextColor.RED));
                    t.cancel();
                    return;
                }
                if (timeout.getAndDecrement() > 0) {
                    entity.showTitle(Title.title(Component.text(timeout.get(), NamedTextColor.YELLOW), Component.translatable(PENDING_TELEPORT, NamedTextColor.RED), Title.Times.times(Duration.of(200, ChronoUnit.MILLIS), Duration.of(1, ChronoUnit.SECONDS), Duration.of(200, ChronoUnit.MILLIS))));
                    return;
                }

                pendingTeleports.remove(entity);
                entity.teleportAsync(new Location(world, loc[0], loc[1], loc[2]), PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(success -> {
                    if (Boolean.FALSE.equals(success))
                        entity.sendMessage(Component.translatable(FAILED_TO_TELEPORT, NamedTextColor.RED));
                    else {
                        DataUtil.set(entity, LAST_TP, world.getGameTime(), PersistentDataType.LONG);
                    }
                });
                t.cancel();
            }, 0, 1, TimeUnit.SECONDS);
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
            this.requires(ctx -> ctx.getExecutor() instanceof Player).then(Commands.argument(NAME_ARGUMENT, StringArgumentType.word()).suggests(this).executes(this));
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            @NotNull Entity entity = Objects.requireNonNull(context.getSource().getExecutor());
            String targetHome = context.getArgument(NAME_ARGUMENT, String.class).trim().toLowerCase();
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
