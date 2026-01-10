package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityPrimedTNT;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public class TntCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		String[] literals = {"tnt", "grenade"};
		for (String literal : literals) {
			dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.executes(this::noArg)
				.then(ArgumentBuilderRequired
					.argument("velocity", ArgumentTypeInteger.integer(0))
					.executes(this::velArg)));
		}
	}

	private @NotNull int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();

		DirectionalTNT tnt = new DirectionalTNT(
			player.world,
			player.x,
			player.y + 1,
			player.z,
			player.xRot,
			player.yRot,
			2
		);

		player.world.entityJoinedWorld(tnt);
		player.world.playSoundAtEntity((Entity) null, tnt, "tile.tnt.fuse", 1.0F, 1.0F);

		return 1;
	}

	private @NotNull int velArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source   = (CommandSource) context.getSource();
		Player        player   = source.getSender();
		int           velocity = context.getArgument("velocity", Integer.class);

		DirectionalTNT tnt = new DirectionalTNT(
			player.world,
			player.x,
			player.y + 1,
			player.z,
			player.xRot,
			player.yRot,
			velocity
		);

		player.world.entityJoinedWorld(tnt);
		player.world.playSoundAtEntity((Entity) null, tnt, "tile.tnt.fuse", 1.0F, 1.0F);

		return 1;
	}

	private class DirectionalTNT extends EntityPrimedTNT {
		public DirectionalTNT(
			World world,
			double x,
			double y,
			double z,
			float pitch,
			float yaw,
			float force
		)
		{
			super(world);
			this.setPos(x, y, z);

			float pitchRad = (float) Math.toRadians(pitch);
			float yawRad   = (float) Math.toRadians(yaw);

			this.xd = -Math.sin(yawRad) * Math.cos(pitchRad) * force;
			this.zd = Math.cos(yawRad) * Math.cos(pitchRad) * force;
			this.yd = -Math.sin(pitchRad) * force + 0.3;

			this.fuse = 80;

			this.xo = x;
			this.yo = y;
			this.zo = z;
		}
	}
}
