package de.cikles.ciklesmc.commands.claim;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;

// Only for testing for later (Adding Party's/Team's and Claims)

public class Claim extends LiteralArgumentBuilder<CommandSourceStack> implements Command<CommandSourceStack> {

    public Claim() {
        super("claim");
        this.requires(ctx -> ctx.getExecutor() instanceof LivingEntity).executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getExecutor() instanceof LivingEntity entity) {
            Chunk chunk = entity.getChunk();
            Bukkit.getGlobalRegionScheduler().execute(CiklesMC.getInstance(), () -> chunk.setForceLoaded(!chunk.isForceLoaded()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
