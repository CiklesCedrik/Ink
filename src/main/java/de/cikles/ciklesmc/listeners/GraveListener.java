package de.cikles.ciklesmc.listeners;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.cikles.ciklesmc.core.CiklesMC;
import de.cikles.ciklesmc.utility.DataUtil;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static de.cikles.ciklesmc.utility.Config.Translations.*;
import static de.cikles.ciklesmc.utility.DataUtil.*;

public class GraveListener implements Listener {


    /*
    WORLD
        GRAVES
            [ PlayerUUID, Location, Data, Exp]
            [ PlayerUUID, Location, Data, Exp]
     */
    private static final NamespacedKey GRAVE_KEY = NamespacedKey.fromString("graves", CiklesMC.getInstance());
    private static final NamespacedKey INVENTORY_CONTENTS_KEY = NamespacedKey.fromString("inventory", CiklesMC.getInstance());
    private static final NamespacedKey OWNER_KEY = NamespacedKey.fromString("owner", CiklesMC.getInstance());
    private static final NamespacedKey POSITION_KEY = NamespacedKey.fromString("pos", CiklesMC.getInstance());
    private static final NamespacedKey EXP_KEY = NamespacedKey.fromString("xp", CiklesMC.getInstance());

    private static boolean isInBounds(int x, int y, int z, int radius) {
        return x * x + y * y + z * z <= radius * radius;
    }

    public static Location getGraveLocation(Location original, int radius) {
        Set<String> visited = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        Location initial = createInitial(original, radius);

        queue.add(new int[]{0, 0, 0});
        visited.add("0,0,0");

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            int z = current[2];

            int worldX = initial.blockX() + x;
            int worldY = initial.blockY() + y;
            int worldZ = initial.blockZ() + z;

            if (isInBounds(x, y, z, radius)) {
                Location target = new Location(initial.getWorld(), worldX, worldY, worldZ);
                if (target.getBlock().isEmpty()) return target;

                int[][] directions = {{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

                for (int[] dir : directions) {
                    int nx = x + dir[0];
                    int ny = y + dir[1];
                    int nz = z + dir[2];
                    String key = nx + "," + ny + "," + nz;

                    if (!visited.contains(key)) {
                        visited.add(key);
                        queue.add(new int[]{nx, ny, nz});
                    }
                }
            }
        }
        return null;
    }

    // Sets the middle of the search sphere, away from the minimum and maximum build height
    // also returns first empty block on Y-axis if it's not more than 3 Block away.
    private static Location createInitial(Location location, int radius) {
        World world = location.getWorld();
        // Checking Y-Axis for free Block in a 3 Block distance
        int highestBlock = location.getWorld().getHighestBlockAt(location.blockX(), location.blockZ()).getY() + 1;
        if (highestBlock - location.blockY() <= 3 && highestBlock < world.getMaxHeight())
            return new Location(location.getWorld(), location.blockX(), highestBlock, location.blockZ());
        // Checking for maximum centre height
        if (location.blockY() + radius / 2 > location.getWorld().getMaxHeight())
            return new Location(world, location.getX(), world.getMaxHeight() - radius / 2D - 2, location.getZ());
        // Checking for minimum centre height
        if (location.blockY() - radius / 2 > location.getWorld().getMinHeight())
            return new Location(world, location.getX(), world.getMinHeight() + radius / 2D + 2, location.getZ());
        return location;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location loc = getGraveLocation(player.getLocation(), 20);

        if (loc == null) {
            player.sendMessage(Component.translatable(GRAVE_FAILED, NamedTextColor.RED));
            return;
        }

        // Set Skull on Location of death
        loc.getBlock().setType(Material.PLAYER_HEAD, false);
        Skull skull = (Skull) loc.getBlock().getState();
        skull.setOwningPlayer(player);
        skull.setPlayerProfile(player.getPlayerProfile());
        skull.update();

        Inventory inventory = player.getInventory();
        int xp = player.getTotalExperience();
        PersistentDataContainer container = loc.getWorld().getPersistentDataContainer();

        // Clear Drops and Exp
        event.getDrops().clear();
        event.setShouldDropExperience(true);
        event.setDroppedExp(Math.round(xp * 0.5f));
        // Loading other graves of Player
        ArrayList<PersistentDataContainer> graves = new ArrayList<>(DataUtil.getSubArrayContainer(container, GRAVE_KEY));

        // Save Inventory and Exp into a new container.
        PersistentDataContainer deathPoint = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        DataUtil.set(deathPoint, POSITION_KEY, new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()}, PersistentDataType.INTEGER_ARRAY);
        DataUtil.set(deathPoint, EXP_KEY, Math.round(xp * 0.2f), PersistentDataType.INTEGER);
        DataUtil.set(deathPoint, INVENTORY_CONTENTS_KEY, INVENTORY_CONTENTS.toPrimitive(Arrays.stream(inventory.getContents()).map(it -> it == null ? ItemStack.empty() : it).toList(), player.getPersistentDataContainer().getAdapterContext()), PersistentDataType.LIST.dataContainers());
        DataUtil.set(deathPoint, OWNER_KEY, UUID.toPrimitive(player.getUniqueId(), deathPoint.getAdapterContext()), PersistentDataType.LONG_ARRAY);

        // Add to the list and save into the current world.
        graves.add(deathPoint);
        DataUtil.set(container, GRAVE_KEY, graves, PersistentDataType.LIST.dataContainers());

        player.sendMessage(Component.translatable(GRAVE_SPAWNED, NamedTextColor.YELLOW, Component.text("X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ())));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Skull)) return;
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        Inventory inventory = player.getInventory();
        PersistentDataContainer container = loc.getWorld().getPersistentDataContainer();

        // Check if the world has graves
        if (!container.has(GRAVE_KEY, PersistentDataType.LIST.dataContainers())) return;
        event.setDropItems(false);

        // Search for target grave.
        List<PersistentDataContainer> graves = new ArrayList<>(DataUtil.getSubArrayContainer(container, GRAVE_KEY));
        PersistentDataContainer deathPoint = getGrave(graves, loc);
        if (deathPoint == null) {
            CiklesMC.getInstance().getSLF4JLogger().warn("No grave found!");
            return;
        }

        // Load Items from Grave

        ItemStack[] contents = inventory.getContents();
        List<ItemStack> dropLater = new ArrayList<>();
        AtomicInteger slot = new AtomicInteger();
        DataUtil.get(deathPoint, INVENTORY_CONTENTS_KEY, PersistentDataType.LIST.dataContainers()).forEach(container0 -> {
            ItemStack itemStack = ITEM_STACK.fromPrimitive(DataUtil.get(container0, DATA_KEY, PersistentDataType.STRING), player.getPersistentDataContainer().getAdapterContext());
            if (contents[slot.get()] == null || contents[slot.get()].isEmpty()) contents[slot.get()] = itemStack;
            else dropLater.add(itemStack);
            slot.getAndIncrement();
        });
        Entity e = Bukkit.getEntity(UUID.fromPrimitive(DataUtil.get(deathPoint, OWNER_KEY, PersistentDataType.LONG_ARRAY), deathPoint.getAdapterContext()));
        if (e != null) {
            if (e.getUniqueId().equals(player.getUniqueId())) {
                // Give items to player
                player.giveExp(DataUtil.get(deathPoint, EXP_KEY, PersistentDataType.INTEGER), false);
                inventory.setContents(contents);
                inventory.addItem(dropLater.stream().filter(Objects::nonNull).filter(it -> !it.isEmpty()).toArray(ItemStack[]::new)).values().forEach(drop -> player.getWorld().dropItemNaturally(loc, drop));
            } else {
                // drop Items on grave
                ((ExperienceOrb) loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB)).setExperience(DataUtil.get(deathPoint, EXP_KEY, PersistentDataType.INTEGER));
                dropLater.addAll(List.of(contents));
                dropLater.forEach(itemStack -> player.getWorld().dropItemNaturally(loc, itemStack));

                e.sendMessage(Component.translatable(GRAVE_REMOVED_BY, NamedTextColor.RED, player.playerListName().color(NamedTextColor.GRAY)).hoverEvent(HoverEvent.showText(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.YELLOW))));
            }
        }

        // Remove from the list and save into the current world.
        graves.remove(deathPoint);
        DataUtil.set(container, GRAVE_KEY, graves, PersistentDataType.LIST.dataContainers());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakBlockEvent(BlockBreakBlockEvent event) {
        if (!(event.getBlock().getState() instanceof Skull)) return;
        Location loc = event.getBlock().getLocation();
        PersistentDataContainer container = loc.getWorld().getPersistentDataContainer();

        // Check if the world has graves
        if (!container.has(GRAVE_KEY, PersistentDataType.LIST.dataContainers())) return;

        // Search for target grave.
        List<PersistentDataContainer> graves = new ArrayList<>(DataUtil.getSubArrayContainer(container, GRAVE_KEY));
        PersistentDataContainer deathPoint = getGrave(graves, loc);
        if (deathPoint == null)
            return;
        event.getDrops().clear();
        Bukkit.getAsyncScheduler().runDelayed(CiklesMC.getInstance(), t -> {

            Location target = getGraveLocation(loc, 15);
            UUID uuid = UUID.fromPrimitive(DataUtil.get(deathPoint, OWNER_KEY, PersistentDataType.LONG_ARRAY), deathPoint.getAdapterContext());
            Entity e = Bukkit.getEntity(uuid);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (e != null && target == null) {
                e.sendMessage(Component.translatable(GRAVE_FAILED, NamedTextColor.RED));
                return;
            }

            // Set Skull on Location of death
            assert target != null;
            Bukkit.getRegionScheduler().run(CiklesMC.getInstance(), target, update -> {
                target.getBlock().setType(Material.PLAYER_HEAD, false);
                Skull skull = (Skull) target.getBlock().getState();
                skull.setOwningPlayer(offlinePlayer);
                PlayerProfile profile = offlinePlayer.getPlayerProfile();
                profile.update();
                skull.setPlayerProfile(profile);
                skull.update();
            });
            graves.remove(deathPoint);
            assert POSITION_KEY != null;
            deathPoint.remove(POSITION_KEY);
            DataUtil.set(deathPoint, POSITION_KEY, new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()}, PersistentDataType.INTEGER_ARRAY);
            graves.add(deathPoint);
            DataUtil.set(container, GRAVE_KEY, graves, PersistentDataType.LIST.dataContainers());
        }, 5, TimeUnit.SECONDS);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        cancel(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(BlockExplodeEvent event) {
        cancel(event.blockList());
    }

    private void cancel(List<Block> blocks) {
        for (Block b : blocks) {
            if (!(b.getState() instanceof Skull)) return;
            Location loc = b.getLocation();
            PersistentDataContainer container = loc.getWorld().getPersistentDataContainer();

            // Check if the world has graves
            if (!container.has(GRAVE_KEY, PersistentDataType.LIST.dataContainers())) return;

            // Search for target grave.
            List<PersistentDataContainer> graves = new ArrayList<>(DataUtil.getSubArrayContainer(container, GRAVE_KEY));
            PersistentDataContainer deathPoint = getGrave(graves, loc);
            if (deathPoint != null) blocks.remove(b);
        }
    }

    private @Nullable PersistentDataContainer getGrave(List<PersistentDataContainer> graves, Location location) {
        Optional<PersistentDataContainer> graveOptional = graves.stream().filter(grave -> isRightGrave(grave, location)).findAny();
        return graveOptional.orElse(null);
    }

    private boolean isRightGrave(PersistentDataContainer grave, Location targetCompare) {
        int[] graveLocation = DataUtil.get(grave, POSITION_KEY, PersistentDataType.INTEGER_ARRAY);
        if (targetCompare.getBlockZ() != graveLocation[2]) return false;
        if (targetCompare.getBlockX() != graveLocation[0]) return false;
        return targetCompare.getBlockY() == graveLocation[1];
    }

}