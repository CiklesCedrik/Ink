package de.cikles.ciklesmc.listeners;

import de.cikles.ciklesmc.enchantments.Enchantments;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.*;

public class FarmListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        if (player.isSneaking()) return;
        if (!(b.getBlockData() instanceof Ageable crop)) return;
        if (crop.getAge() != crop.getMaximumAge()) return;
        switch (b.getType()) {
            case POTATOES, CARROTS:
                e.setCancelled(true);
                dropItem(b.getLocation(), new ItemStack(b.getType().equals(POTATOES) ? POTATO : CARROT, b.getDrops(player.getInventory().getItemInMainHand(), player).stream().mapToInt(ItemStack::getAmount).sum() - 1), player);
                break;
            case WHEAT, BEETROOTS:
                e.setCancelled(true);
                b.getDrops(player.getInventory().getItemInMainHand(), player).forEach(it -> {
                    if (it.getType().equals(Material.BEETROOT_SEEDS) || it.getType().equals(Material.WHEAT_SEEDS))
                        it.setAmount(it.getAmount() - 1);
                    dropItem(b.getLocation(), it, player);
                });
                break;
            case SWEET_BERRIES, GLOW_BERRIES, SWEET_BERRY_BUSH, GLOW_LICHEN, PUMPKIN_STEM, PUMPKIN_SEEDS, MELON_STEM, MELON_SEEDS, BAMBOO:
                return;
            default:
                dropItem(b.getLocation(), new ItemStack(b.getType(), b.getDrops(player.getInventory().getItemInMainHand(), player).stream().mapToInt(ItemStack::getAmount).sum() - 1), player);
                break;
        }
        crop.setAge(0);
        b.setBlockData(crop);

    }

    private void dropItem(Location loc, ItemStack itemStack, Player p) {
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() != null && mainHand.getItemMeta().getEnchants().keySet().stream().anyMatch(t -> t.key().equals(Enchantments.TELEKINESIS.enchantmentTypedKey))) {
            p.getInventory().addItem(itemStack).values().forEach(it -> p.getWorld().dropItemNaturally(p.getLocation(), it));
        } else {
            loc.getWorld().dropItemNaturally(loc, itemStack);
        }
    }
}
