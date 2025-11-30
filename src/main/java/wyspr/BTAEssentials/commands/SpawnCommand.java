package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

@SuppressWarnings("ALL") public class SpawnCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		ArgumentBuilderLiteral<CommandSource> builder = ArgumentBuilderLiteral.literal("spawn");

		spawn(builder);
		spawnSet(builder);
		spawnReset(builder);

		dispatcher.register(builder);
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> spawn(
		ArgumentBuilderLiteral<CommandSource> builder
	)
	{
		builder
			.then(ArgumentBuilderLiteral.literal(""))
			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.SpawnCommand)
			.executes(context -> {
				CommandSource    source           = (CommandSource) context.getSource();
				Player           sender           = source.getSender();
				World            world            = source.getWorld(0);
				ChunkCoordinates spawnCoordinates = world.getSpawnPoint();
				if (sender == null) {
					throw CommandExceptions.notInWorld().create();
				} else {
					if (sender.dimension != 0) {
						source.movePlayerToDimension(sender, 0);
					}

					PlayerData.get(sender).updateBackPos();

					source.teleportPlayerToPos(
						sender,
						(double) spawnCoordinates.x + (double) 0.5F,
						(double) ((float) world.findTopSolidBlock(
							spawnCoordinates.x,
							spawnCoordinates.z
						) + sender.heightOffset),
						(double) spawnCoordinates.z + (double) 0.5F
					);
					source.sendTranslatableMessage("command.commands.spawn.success", new Object[0]);
					return 1;
				}
			});
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> spawnSet(
		ArgumentBuilderLiteral<CommandSource> builder
	)
	{
		builder.then(ArgumentBuilderLiteral
			.<CommandSource>literal("set")
			.requires(CommandSource::hasAdmin)
			.executes(context -> {
				CommandSource    source           = (CommandSource) context.getSource();
				Player           sender           = source.getSender();
				World            world            = source.getWorld(0);
				ChunkCoordinates spawnCoordinates = world.getSpawnPoint();

				world.setSpawnPoint(new ChunkCoordinates(
					sender.chunkCoordX,
					sender.chunkCoordY,
					sender.chunkCoordZ
				));

				return 1;
			}));
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> spawnReset(
		ArgumentBuilderLiteral<CommandSource> builder
	)
	{
		builder.then(ArgumentBuilderLiteral
			.<CommandSource>literal("reset")
			.requires(CommandSource::hasAdmin)
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        sender = source.getSender();
				World         world  = source.getWorld(0);
				if (sender == null) {
					return 0;
				}

				world.setSpawnPoint(new ChunkCoordinates(0, 128, 0));

				return 1;
			}));
		return builder;
	}


//		dispatcher.register((ArgumentBuilderLiteral) ((ArgumentBuilderLiteral) ((ArgumentBuilderLiteral) ((ArgumentBuilderLiteral) ArgumentBuilderLiteral
//			.literal("spawn")
//			.requires(source -> ((CommandSource) source).hasAdmin() || BTAEssentials.SpawnCommand)).executes((c) -> {
//			CommandSource source = (CommandSource) c.getSource();
//			Player sender = source.getSender();
//			World world = source.getWorld(0);
//			ChunkCoordinates spawnCoordinates = world.getSpawnPoint();
//			if (sender == null) {
//				throw CommandExceptions.notInWorld().create();
//			} else {
//				if (sender.dimension != 0) {
//					source.movePlayerToDimension(sender, 0);
//				}
//
//				PlayerData.get(sender).updateBackPos();
//
//				source.teleportPlayerToPos(
//					sender,
//					(double) spawnCoordinates.x + (double) 0.5F,
//					(double) ((float) world.findTopSolidBlock(
//						spawnCoordinates.x,
//						spawnCoordinates.z
//					) + sender.heightOffset),
//					(double) spawnCoordinates.z + (double) 0.5F
//				);
//				source.sendTranslatableMessage("command.commands.spawn.success", new Object[0]);
//				return 1;
//			}
//		})).then(ArgumentBuilderRequired
//			.argument("players", ArgumentTypeEntity.usernames())
//			.requires(source -> ((CommandSource) source).hasAdmin())
//			.executes((c) -> {
//				CommandSource source = (CommandSource) c.getSource();
//				Player sender = source.getSender();
//				World world = source.getWorld(0);
//				ChunkCoordinates spawnCoordinates = world.getSpawnPoint();
//				EntitySelector entitySelector = (EntitySelector) c.getArgument(
//					"players",
//					EntitySelector.class
//				);
//
//				for (Entity entity : entitySelector.get(source)) {
//					if (((Player) entity).dimension != 0) {
//						source.movePlayerToDimension((Player) entity, 0);
//					}
//
//					PlayerData.get((Player) entity).updateBackPos();
//
//					source.teleportPlayerToPos(
//						(Player) entity,
//						(double) spawnCoordinates.x + (double) 0.5F,
//						(double) ((float) world.findTopSolidBlock(
//							spawnCoordinates.x,
//							spawnCoordinates.z
//						) + entity.heightOffset),
//						(double) spawnCoordinates.z + (double) 0.5F
//					);
//					if (entity == sender) {
//						source.sendTranslatableMessage("command.commands.spawn.success", new Object[0]);
//					} else {
//						source.sendTranslatableMessage(
//							"command.commands.spawn.success_receiver",
//							new Object[0]
//						);
//						source.sendTranslatableMessage(
//							"command.commands.spawn.success_other", new Object[]{
//								CommandHelper.getEntityName(entity)
//							}
//						);
//					}
//				}
//
//				return 1;
//			}))).then(ArgumentBuilderLiteral
//			.literal("get")
//			.requires(source -> ((CommandSource) source).hasAdmin())
//			.executes((c) -> {
//				CommandSource source = (CommandSource) c.getSource();
//				ChunkCoordinates spawnCoordinates = source.getWorld(0).getSpawnPoint();
//				source.sendMessage(I18n.getInstance().translateKeyAndFormat(
//					"command.commands.spawn.get",
//					new Object[]{spawnCoordinates.x, spawnCoordinates.y, spawnCoordinates.z}
//				));
//				return 1;
//			})));
//	}
}
