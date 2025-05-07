package de.cikles.ciklesmc.enchantments;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

public class Oracle extends Enchantments.CiklesEnchant {
    @Override
    public void register(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event, EnchantmentRegistryEntry.Builder c) {
        c.description(Component.text("Oracle"))
                .supportedItems(event.getOrCreateTag(Enchantments.ENCHANTABLE_TOOL))
                .anvilCost(5)
                .maxLevel(4).weight(2)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 1))
                .activeSlots(EquipmentSlotGroup.HAND);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player target)) return;
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() != null && mainHand.getItemMeta().hasEnchant(Enchantments.ORACLE.getEnchantment())) {
            int exp = event.getDroppedExp();
            int enchLvl = mainHand.getItemMeta().getEnchantLevel(Enchantments.ORACLE.getEnchantment());
            event.setDroppedExp(Math.round(exp * (1 + 0.5f / 5 * enchLvl)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player target = event.getPlayer();
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() != null && mainHand.getItemMeta().hasEnchant(Enchantments.ORACLE.getEnchantment())) {
            int exp = event.getExpToDrop();
            int enchLvl = mainHand.getItemMeta().getEnchantLevel(Enchantments.ORACLE.getEnchantment());
            event.setExpToDrop(Math.round(exp * (1 + 0.5f / 5 * enchLvl)));
        }
    }
}
