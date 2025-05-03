package de.cikles.ciklesmc.enchantments;

import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public enum Enchantments {
    TELEKINESIS(EnchantmentKeys.create(Key.key("ciklesmc:telekinesis")), new Telekinesis());

    public final @NotNull CiklesEnchant ciklesEnchant;
    public final @NotNull TypedKey<Enchantment> enchantmentTypedKey;

    <T extends CiklesEnchant> Enchantments(@NotNull TypedKey<Enchantment> enchantmentTypedKey, @NotNull T ciklesEnchant) {
        this.ciklesEnchant = ciklesEnchant;
        this.enchantmentTypedKey = enchantmentTypedKey;
    }

    public abstract static class CiklesEnchant implements Listener {

        public abstract void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c);

        public void registerTag(PostFlattenTagRegistrar<ItemType> registrar) {
        }
    }
}
