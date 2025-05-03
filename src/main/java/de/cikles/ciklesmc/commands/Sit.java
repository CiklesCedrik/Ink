package de.cikles.ciklesmc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Sit extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack> {

    public Sit() {
        super("sit");
        this.requires(ctx -> ctx.getExecutor() instanceof LivingEntity).executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getExecutor() instanceof LivingEntity entity) {
            Arrow arrow = context.getSource().getLocation().getWorld().spawnArrow(entity.getLocation().add(0, -0.4, 0), new Vector(), 0, 0);
            arrow.setGravity(false);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            if (!arrow.addPassenger(entity)) {
                arrow.remove();
            } else
                Bukkit.getRegionScheduler().runAtFixedRate(CiklesMC.getInstance(), arrow.getWorld(), arrow.getChunk().getX(), arrow.getChunk().getZ(), scheduledTask -> {
                    if (arrow.getPassengers().isEmpty()) {
                        arrow.remove();
                        scheduledTask.cancel();
                    }
                }, 60, 60);
        }
        return Command.SINGLE_SUCCESS;
    }
}
