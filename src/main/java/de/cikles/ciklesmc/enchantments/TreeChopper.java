package de.cikles.ciklesmc.enchantments;

import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
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
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.cikles.ciklesmc.enchantments.Enchantments.DIRECTIONS;

public class TreeChopper extends Enchantments.CiklesEnchant {


    private static final Registry<BlockType> BLOCK_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);
    private static final ArrayList<Block> TO_BREAK = new ArrayList<>();

    public static boolean isLog(Block block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.LOGS).values().stream().map(TypedKey::key).toList().contains(block.getType().asBlockType().key());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockDestroy(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (TO_BREAK.contains(event.getBlock())) {
            TO_BREAK.remove(event.getBlock());
            return;
        }
        Player target = event.getPlayer();
        if (target.getGameMode().equals(GameMode.CREATIVE)) return;
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (!isLog(event.getBlock())) return;
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), t -> {
            if (mainHand.getItemMeta() != null && mainHand.getItemMeta().hasEnchant(Enchantments.TREE_CHOPPER.getEnchantment())) {
                AtomicInteger delay = new AtomicInteger(1);
                List<Block> logs = getNearbyLogs(event.getBlock().getLocation());
                TO_BREAK.addAll(logs);
                for (Block b : logs) {
                    float speed = b.getBreakSpeed(target);
                    Bukkit.getRegionScheduler().runDelayed(CiklesMC.getInstance(), b.getLocation(), task ->
                            target.breakBlock(b), Math.clamp(Math.round(1F / speed), 1L, 10L) * delay.getAndIncrement());
                }
            }
        });
    }

    public List<Block> getNearbyLogs(Location origin) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> toVisit = new LinkedList<>();
        List<Block> logs = new ArrayList<>();
        Block originBlock = origin.getBlock();
        for (int[] dir : DIRECTIONS) {
            Block neighbor = originBlock.getRelative(dir[0], dir[1], dir[2]);
            if (isLog(neighbor)) {
                toVisit.add(neighbor);
                visited.add(neighbor);
            }
        }
        while (!toVisit.isEmpty()) {
            Block current = toVisit.poll();
            logs.add(current);
            if (logs.size() >= 32) return logs;

            Location currentLoc = current.getLocation();
            if (Math.abs(currentLoc.getX() - origin.getX()) > 40 ||
                    Math.abs(currentLoc.getZ() - origin.getZ()) > 40 ||
                    Math.abs(currentLoc.getY() - origin.getY()) > 100) {
                continue;
            }

            for (int[] dir : DIRECTIONS) {
                Block neighbor = current.getRelative(dir[0], dir[1], dir[2]);
                if (!visited.contains(neighbor) && isLog(neighbor)) {
                    toVisit.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        return logs;
    }

    @Override
    public void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c) {
        c.description(Component.text("Tree Chopper")).supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES)).anvilCost(5).maxLevel(1).weight(2).minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 1)).maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 1)).activeSlots(EquipmentSlotGroup.HAND);
    }
}