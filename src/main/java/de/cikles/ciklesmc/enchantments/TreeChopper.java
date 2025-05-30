package de.cikles.ciklesmc.enchantments;

import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.cikles.ciklesmc.enchantments.Enchantments.DIRECTIONS;

public class TreeChopper extends Enchantments.CiklesEnchant {

    private static final int MAX_LOGS = 128;
    private static final int MAX_SEARCH_DISTANCE_HORIZONTAL = 40;
    private static final int MAX_SEARCH_DISTANCE_VERTICAL = 100;
    private static final int MAX_PLAYER_DISTANCE = 50;
    private static final long MAX_BREAK_DELAY = 8L;

    private static final Registry<BlockType> BLOCK_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);

    // Thread-sichere Datenstrukturen für Folia
    private static final Set<Location> BREAKING_BLOCKS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Set<Location>> PLAYER_BREAKING_BLOCKS = new ConcurrentHashMap<>();

    private static final Set<Key> LOG_KEYS = BLOCK_REGISTRY.getTag(BlockTypeTagKeys.LOGS)
            .values().stream()
            .map(TypedKey::key)
            .collect(Collectors.toSet());

    public static boolean isLog(Block block) {
        return LOG_KEYS.contains(block.getType().asBlockType().key());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockDestroy(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Location blockLoc = block.getLocation().toBlockLocation();

        // Überprüfe, ob dieser Block bereits von TreeChopper bearbeitet wird
        if (BREAKING_BLOCKS.contains(blockLoc)) {
            BREAKING_BLOCKS.remove(blockLoc);
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (lacksEnchantment(player.getInventory().getItemInMainHand().getItemMeta())) return;
        if (!isLog(block)) return;

        // Starte Tree-Breaking-Prozess
        scheduleTreeBreaking(block.getLocation(), player);
    }

    private void scheduleTreeBreaking(@NotNull Location originLocation, @NotNull Player player) {
        UUID playerId = player.getUniqueId();

        // Verwende Folia's RegionScheduler für die initiale Berechnung
        Bukkit.getRegionScheduler().run(CiklesMC.getInstance(), originLocation, task -> {
            // Überprüfe erneut, ob der Spieler noch das Enchantment hat
            if (!player.isOnline() || lacksEnchantment(player.getInventory().getItemInMainHand().getItemMeta())) {
                return;
            }

            // Finde alle verbundenen Logs
            List<Block> logs = findConnectedLogs(originLocation);
            if (logs.isEmpty()) return;

            // Registriere die Blöcke, die gebrochen werden sollen
            Set<Location> playerBreakingSet = new HashSet<>();
            for (Block log : logs) {
                Location loc = log.getLocation().toBlockLocation();
                BREAKING_BLOCKS.add(loc);
                playerBreakingSet.add(loc);
            }
            PLAYER_BREAKING_BLOCKS.put(playerId, playerBreakingSet);

            // Plane das Brechen der Blöcke
            scheduleBlockBreaking(logs, player);
        });
    }

    private void scheduleBlockBreaking(@NotNull List<Block> logs, @NotNull Player player) {
        AtomicInteger delay = new AtomicInteger(1);

        for (Block block : logs) {
            Location blockLocation = block.getLocation();
            float breakSpeed = block.getBreakSpeed(player);
            long breakDelay = Math.clamp(Math.round(1F / breakSpeed), 1L, MAX_BREAK_DELAY) * delay.getAndIncrement();

            // Verwende RegionScheduler für jeden Block in seiner eigenen Region
            Bukkit.getRegionScheduler().runDelayed(
                    CiklesMC.getInstance(),
                    blockLocation,
                    task -> breakBlockSafely(block, player),
                    breakDelay
            );
        }

        // Cleanup nach einer bestimmten Zeit
        long maxDelay = delay.get() * MAX_BREAK_DELAY + 20L; // +20 ticks Puffer
        Bukkit.getRegionScheduler().runDelayed(
                CiklesMC.getInstance(),
                Objects.requireNonNull(player.getLocation()),
                task -> cleanupPlayerBreaking(player.getUniqueId()),
                maxDelay
        );
    }

    private void breakBlockSafely(@NotNull Block block, @NotNull Player player) {
        Location blockLoc = block.getLocation().toBlockLocation();
        UUID playerId = player.getUniqueId();

        try {
            // Überprüfe, ob der Block noch gebrochen werden soll
            if (!BREAKING_BLOCKS.contains(blockLoc)) {
                return;
            }

            // Überprüfe Spieler-Status
            if (!player.isOnline()) {
                cleanupBlock(blockLoc, playerId);
                return;
            }
            // Überprüfe Distanz
            if (Objects.requireNonNull(player.getLocation()).distance(block.getLocation()) > MAX_PLAYER_DISTANCE) {
                cleanupBlock(blockLoc, playerId);
                return;
            }

            // Überprüfe Enchantment
            if (lacksEnchantment(player.getInventory().getItemInMainHand().getItemMeta())) {
                cleanupBlock(blockLoc, playerId);
                return;
            }

            // Überprüfe, ob der Block noch ein Log ist
            if (!isLog(block)) {
                cleanupBlock(blockLoc, playerId);
                return;
            }

            // Breche den Block
            player.breakBlock(block);

        } finally {
            // Cleanup nach dem Brechen
            cleanupBlock(blockLoc, playerId);
        }
    }

    private void cleanupBlock(Location blockLoc, UUID playerId) {
        BREAKING_BLOCKS.remove(blockLoc);
        Set<Location> playerBlocks = PLAYER_BREAKING_BLOCKS.get(playerId);
        if (playerBlocks != null) {
            playerBlocks.remove(blockLoc);
        }
    }

    private void cleanupPlayerBreaking(UUID playerId) {
        Set<Location> playerBlocks = PLAYER_BREAKING_BLOCKS.remove(playerId);
        if (playerBlocks != null) {
            BREAKING_BLOCKS.removeAll(playerBlocks);
        }
    }

    private boolean lacksEnchantment(ItemMeta meta) {
        return meta == null || !meta.hasEnchant(Enchantments.TREE_CHOPPER.getEnchantment());
    }

    private List<Block> findConnectedLogs(Location origin) {
        Set<Location> visited = new HashSet<>();
        Queue<Block> toVisit = new LinkedList<>();
        List<Block> logs = new ArrayList<>();
        Block originBlock = origin.getBlock();

        // Füge initiale Nachbarn hinzu
        addNeighborsToQueue(originBlock, toVisit, visited, origin);

        // Breadth-First-Search für verbundene Logs
        while (!toVisit.isEmpty() && logs.size() < MAX_LOGS) {
            Block current = toVisit.poll();
            logs.add(current);

            // Füge Nachbarn des aktuellen Blocks hinzu
            addNeighborsToQueue(current, toVisit, visited, origin);
        }

        return logs;
    }

    private void addNeighborsToQueue(Block centerBlock, Queue<Block> toVisit, Set<Location> visited, Location origin) {
        Location centerLoc = centerBlock.getLocation();

        // Überprüfe Distanz-Limits
        if (Math.abs(centerLoc.getX() - origin.getX()) > MAX_SEARCH_DISTANCE_HORIZONTAL ||
                Math.abs(centerLoc.getZ() - origin.getZ()) > MAX_SEARCH_DISTANCE_HORIZONTAL ||
                Math.abs(centerLoc.getY() - origin.getY()) > MAX_SEARCH_DISTANCE_VERTICAL) {
            return;
        }

        // Überprüfe alle Nachbarn
        for (int[] dir : DIRECTIONS) {
            Block neighbor = centerBlock.getRelative(dir[0], dir[1], dir[2]);
            Location neighborLoc = neighbor.getLocation().toBlockLocation();

            if (!visited.contains(neighborLoc) && isLog(neighbor)) {
                toVisit.add(neighbor);
                visited.add(neighborLoc);
            }
        }
    }

    @Override
    public void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c) {
        c.description(Component.text("Tree Chopper"))
                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                .anvilCost(5)
                .maxLevel(1)
                .weight(2)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 1))
                .activeSlots(EquipmentSlotGroup.HAND);
    }
}