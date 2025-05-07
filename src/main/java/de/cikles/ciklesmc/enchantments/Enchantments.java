package de.cikles.ciklesmc.enchantments;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public enum Enchantments {
    TELEKINESIS(EnchantmentKeys.create(Key.key("ciklesmc:telekinesis")), new Telekinesis()),
    VEIN(EnchantmentKeys.create(Key.key("ciklesmc:vein")), new Vein()),
    TREE_CHOPPER(EnchantmentKeys.create(Key.key("ciklesmc:tree_chopper")), new TreeChopper()),
    ORACLE(EnchantmentKeys.create(Key.key("ciklesmc:oracle")), new Oracle());

    public final @NotNull CiklesEnchant ciklesEnchant;
    public final @NotNull TypedKey<Enchantment> enchantmentTypedKey;

    <T extends CiklesEnchant> Enchantments(@NotNull TypedKey<Enchantment> enchantmentTypedKey, @NotNull T ciklesEnchant) {
        this.ciklesEnchant = ciklesEnchant;
        this.enchantmentTypedKey = enchantmentTypedKey;
    }

    public static final TagKey<ItemType> ENCHANTABLE_TOOL = ItemTypeTagKeys.create(Key.key("ciklesmc:enchantable_tool"));
    static final int[][] DIRECTIONS = {
            {-1, -1, -1}, {-1, -1, 0}, {-1, -1, 1},
            {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1},
            {-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1},
            {0, -1, -1}, {0, -1, 0}, {0, -1, 1},
            {0, 0, -1}, {0, 0, 1},
            {0, 1, -1}, {0, 1, 0}, {0, 1, 1},
            {1, -1, -1}, {1, -1, 0}, {1, -1, 1},
            {1, 0, -1}, {1, 0, 0}, {1, 0, 1},
            {1, 1, -1}, {1, 1, 0}, {1, 1, 1}
    };

    public Enchantment getEnchantment() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(this.enchantmentTypedKey);
    }

    public abstract static class CiklesEnchant implements Listener {

        public abstract void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c);

    }
}
