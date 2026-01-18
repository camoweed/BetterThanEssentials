package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.commands.arguments.ArgumentTypeOnlineUser;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL")
public class TrollCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("troll")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(this::noArg)
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeOnlineUser.online())
				.executes(this::userArg)));
	}

	private @NotNull int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);

		TrollTNT tnt = new TrollTNT(
			player.world,
			player.x,
			player.y + 1,
			player.z,
			player.xRot,
			player.yRot,
			1.5F
		);

		player.world.entityJoinedWorld(tnt);
		player.world.playSoundAtEntity((Entity) null, tnt, "tile.tnt.fuse", 1.0F, 1.0F);

		return 1;
	}

	private @NotNull int userArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = Utils.requirePlayer(source);
		PlayerServer  target = context.getArgument("player", PlayerServer.class);

		TrollTNT tnt = new TrollTNT(
			target.world,
			target.x,
			target.y + 1,
			target.z,
			target.xRot,
			target.yRot,
			1.5F
		);

		target.world.entityJoinedWorld(tnt);
		target.world.playSoundAtEntity((Entity) null, tnt, "tile.tnt.fuse", 1.0F, 1.0F);

		return 1;
	}

	private class TrollTNT extends EntityPrimedTNT {
		public TrollTNT(
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
			this.yd = 0.2 + -Math.sin(pitchRad) * force;

			this.fuse = 100;

			this.xo = x;
			this.yo = y;
			this.zo = z;
		}


		@Override
		public void tick() {
			this.checkOnWater(true);
			this.pushTime *= 0.98F;
			if (this.pushTime < 0.05F || (double) this.pushTime < (double) 0.25F && this.onGround) {
				this.pushTime = 0.0F;
			}

			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.yd -= 0.04;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.98;
			this.yd *= 0.98;
			this.zd *= 0.98;
			if (this.onGround) {
				this.xd *= 0.7;
				this.zd *= 0.7;
				this.yd *= -0.5;
			}

			if (this.fuse <= 40) {
				this.world.spawnParticle(
					"smoke",
					this.x,
					this.y + 0.5,
					this.z,
					0.0,
					0.0,
					0.0,
					0
				);
			}

			switch (this.fuse) {
				case 45:
					this.world.playSoundAtEntity(null, this, "mob.ghast.scream", 2, 1);
					break;
				case 40:
					this.world.playSoundAtEntity(null, this, "random.explode", 0.5F, 0);
					break;
				case 35:
					this.world.playSoundAtEntity(null, this, "mob.cowhurt", 2, 1.5F);
					break;
				case 30:
					this.world.playSoundAtEntity(null, this, "random.explode", 1, 0.25F);
					break;
				case 25:
					this.world.playSoundAtEntity(null, this, "mob.wolf.hurt", 2, 1.5F);
					break;
				case 20:
					this.world.playSoundAtEntity(null, this, "random.explode", 1.5F, 0.75F);
					break;
				case 15:
					this.world.playSoundAtEntity(null, this, "mob.pigdeath", 2, 0.75F);
					break;
				case 10:
					this.world.playSoundAtEntity(null, this, "random.explode", 2, 1);
					break;
				case 05:
					this.world.playSoundAtEntity(null, this, "mob.chickenhurt", 2, 0.75F);
					break;
				case 00:
					this.world.playSoundAtEntity(null, this, "random.glass", 2, 0.75F);
					this.world.playSoundAtEntity(null, this, "mob.ghast.scream", 2, 0.75F);
					this.remove();
					break;
			}

			--this.fuse;
		}
	}
}
