package de.cikles.ciklesmc.enchantments;

import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.cikles.ciklesmc.enchantments.Enchantments.DIRECTIONS;

public class Vein extends Enchantments.CiklesEnchant {


    public static final TagKey<BlockType> ORES = BlockTypeTagKeys.create(Key.key("ciklesmc:ores"));
    private static final Registry<BlockType> BLOCK_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);
    private static final ArrayList<Block> TO_BREAK = new ArrayList<>();

    public static boolean isOreOfType(@NotNull Block block, @Nullable TagKey<BlockType> type, @NotNull BlockType broken) {
        if (type == null) return broken.equals(block.getType().asBlockType());
        return BLOCK_REGISTRY.getTag(type).values().stream().map(TypedKey::key).toList().contains(block.getType().asBlockType().key());
    }

    public static boolean isCoal(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.COAL_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isCopperOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.COPPER_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isIronOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.IRON_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isGoldOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.GOLD_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isDiamondOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.DIAMOND_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isEmeraldOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.EMERALD_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isRedstoneOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.REDSTONE_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isLapisOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(BlockTypeTagKeys.LAPIS_ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
    }

    public static boolean isQuartz(@NotNull BlockType block) {
        return block.equals(BlockTypeKeys.NETHER_QUARTZ_ORE);
    }

    public static boolean isOre(@NotNull BlockType block) {
        return BLOCK_REGISTRY.getTag(ORES).values().stream().map(TypedKey::key).toList().contains(block.key());
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
        BlockType type = event.getBlock().getType().asBlockType();
        if (!isOre(type)) return;
        TagKey<BlockType> tag = getTagKey(Objects.requireNonNull(type));
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), t -> {
            if (mainHand.getItemMeta() != null && mainHand.getItemMeta().hasEnchant(Enchantments.VEIN.getEnchantment())) {
                event.setDropItems(false);
                AtomicInteger delay = new AtomicInteger(1);
                List<Block> logs = getNearbyOres(event.getBlock().getLocation(), tag, type);
                TO_BREAK.addAll(logs);
                for (Block b : logs) {
                    float speed = b.getBreakSpeed(target);
                    Bukkit.getRegionScheduler().runDelayed(CiklesMC.getInstance(), b.getLocation(), task ->
                            target.breakBlock(b), Math.clamp(Math.round(1F / speed * delay.getAndIncrement()), 1, 10));
                }
            }
        });
    }

    public List<Block> getNearbyOres(@NotNull Location origin, @Nullable TagKey<BlockType> type, @NotNull BlockType broken) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> toVisit = new LinkedList<>();
        List<Block> ores = new ArrayList<>();
        Block originBlock = origin.getBlock();
        for (int[] dir : DIRECTIONS) {
            Block neighbor = originBlock.getRelative(dir[0], dir[1], dir[2]);
            if (isOreOfType(neighbor, type, broken)) {
                toVisit.add(neighbor);
                visited.add(neighbor);
            }
        }
        while (!toVisit.isEmpty()) {
            Block current = toVisit.poll();
            ores.add(current);
            if (ores.size() >= 32) return ores;

            Location currentLoc = current.getLocation();
            if (Math.abs(currentLoc.getX() - origin.getX()) > 40 ||
                    Math.abs(currentLoc.getZ() - origin.getZ()) > 40 ||
                    Math.abs(currentLoc.getY() - origin.getY()) > 100) {
                continue;
            }

            for (int[] dir : DIRECTIONS) {
                Block neighbor = current.getRelative(dir[0], dir[1], dir[2]);
                if (!visited.contains(neighbor) && isOreOfType(neighbor, type, broken)) {
                    toVisit.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        return ores;
    }

    public @Nullable TagKey<BlockType> getTagKey(@NotNull BlockType blockType) {
        if (isCoal(blockType)) return BlockTypeTagKeys.COAL_ORES;
        if (isCopperOre(blockType)) return BlockTypeTagKeys.COPPER_ORES;
        if (isIronOre(blockType)) return BlockTypeTagKeys.IRON_ORES;
        if (isGoldOre(blockType)) return BlockTypeTagKeys.GOLD_ORES;
        if (isDiamondOre(blockType)) return BlockTypeTagKeys.DIAMOND_ORES;
        if (isEmeraldOre(blockType)) return BlockTypeTagKeys.EMERALD_ORES;
        if (isRedstoneOre(blockType)) return BlockTypeTagKeys.REDSTONE_ORES;
        if (isLapisOre(blockType)) return BlockTypeTagKeys.LAPIS_ORES;
        return null;
    }

    @Override
    public void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c) {
        c.description(Component.text("Vein"))
                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.PICKAXES))
                .anvilCost(3)
                .maxLevel(1).weight(1)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 1))
                .activeSlots(EquipmentSlotGroup.HAND);

    }
}
