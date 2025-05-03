package de.cikles.ciklesmc.listeners;

import de.cikles.ciklesmc.utility.Config;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class MobGriefingListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if ((!Config.creeperExplosions() && type.equals(EntityType.CREEPER))
                || (!Config.ghastExplosions() && type.equals(EntityType.FIREBALL) && ((Fireball) event.getEntity()).getShooter() instanceof Ghast)) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!Config.enderman() && event.getEntity() instanceof Enderman) event.setCancelled(true);
    }
}
