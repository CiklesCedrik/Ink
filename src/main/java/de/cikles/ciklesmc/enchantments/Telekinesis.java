package de.cikles.ciklesmc.enchantments;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Telekinesis extends Enchantments.CiklesEnchant {
    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        ItemStack mainHand = event.getPlayer().getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() != null && mainHand.getItemMeta().getEnchants().keySet().stream().anyMatch(t -> t.key().equals(Enchantments.TELEKINESIS.enchantmentTypedKey))) {
            @NotNull List<Item> drops =
                    event.getItems();
            event.getPlayer().getInventory().addItem(drops.stream().map(Item::getItemStack).toArray(ItemStack[]::new)).values().forEach(it -> event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), it));
            event.getItems().clear();
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player target)) return;
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() != null && mainHand.getItemMeta().hasEnchant(Enchantments.TELEKINESIS.getEnchantment())) {
            target.getInventory().addItem(event.getDrops().toArray(ItemStack[]::new)).values().forEach(it -> target.getWorld().dropItemNaturally(target.getLocation(), it));
            event.getDrops().clear();
        }
    }

    @Override
    public void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c) {
        c.description(Component.text("Telekinesis")).supportedItems(event.getOrCreateTag(Enchantments.ENCHANTABLE_TOOL))
                .anvilCost(3)
                .maxLevel(1).weight(3)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(6, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 1))
                .activeSlots(EquipmentSlotGroup.HAND);

    }

}
