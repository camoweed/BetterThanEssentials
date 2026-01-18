package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.projectile.ProjectileFireball;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class FireballCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("fireball")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(this::exec));
	}

	private @NotNull int exec(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);

		World world = player.world;

		double x = player.x;
		double y = player.y + 1;
		double z = player.z;

		double pitchRad = Math.toRadians(player.xRot);
		double yawRad   = Math.toRadians(player.yRot);

		double vX = -Math.sin(yawRad) * Math.cos(pitchRad);
		double vZ = Math.cos(yawRad) * Math.cos(pitchRad);
		double vY = -Math.sin(pitchRad);

		world.playSoundAtEntity(null, player, "mob.ghast.fireball", 1, 1);
		ProjectileFireball fireball = new ProjectileFireball(world, x, y, z, vX, vY, vZ);
		world.entityJoinedWorld(fireball);

		return 1;
	}
}
