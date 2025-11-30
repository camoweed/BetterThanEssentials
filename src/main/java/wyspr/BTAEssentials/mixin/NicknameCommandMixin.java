package wyspr.BTAEssentials.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.command.commands.CommandNickname;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import wyspr.BTAEssentials.BTAEssentials;

import java.util.List;

@Environment(EnvType.SERVER)
@SuppressWarnings("ALL") @Mixin(
	value = CommandNickname.class, remap = false
) public class NicknameCommandMixin implements CommandManager.CommandRegistry {

	@Shadow
	private static SimpleCommandExceptionType NICKNAME_TOO_LARGE;
	@Shadow
	private static SimpleCommandExceptionType NICKNAME_TOO_SMALL;

	/**
	 * @author wyspr
	 * @reason Override max nick length
	 */
	@Overwrite
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		CommandNode<Object> command
			= dispatcher.register((ArgumentBuilderLiteral) ((ArgumentBuilderLiteral) ((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("nickname")
			.then(((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal("set")
				.then(((ArgumentBuilderRequired) ArgumentBuilderRequired
					.argument("target", ArgumentTypeEntity.username())
					.requires(source -> ((CommandSource) source).hasAdmin()))
						.then(ArgumentBuilderRequired
						.argument("nickname", ArgumentTypeString.string())
						.executes((c) -> {
							CommandSource source = (CommandSource) c.getSource();
							EntitySelector entitySelector = (EntitySelector) c.getArgument(
								"target",
								EntitySelector.class
							);
							String nickname = (String) c.getArgument("nickname", String.class);
							if (nickname.length() > BTAEssentials.NickLength) {
								throw NICKNAME_TOO_LARGE.create();
							} else if (nickname.isEmpty()) {
								throw NICKNAME_TOO_SMALL.create();
							} else {
								List<? extends Entity> entities = entitySelector.get(source);
								PlayerServer player = (PlayerServer) entities.get(0);
								player.nickname = nickname;
								player.hadNicknameSet = true;
								player.mcServer.playerList.updatePlayerProfile(
									player.username,
									player.nickname,
									player.uuid,
									player.score,
									player.chatColor,
									true,
									player.isOperator()
								);
								if (source.getSender() == player) {
									source.sendTranslatableMessage(
										"command.commands.nickname.set.success",
										new Object[]{nickname}
									);
								} else {
									source.sendTranslatableMessage(
										"command.commands.nickname.set.success_other",
										new Object[]{player.username, nickname}
									);
									source.sendTranslatableMessage(
										player,
										"command.commands.nickname.set.success_receiver",
										new Object[]{nickname}
									);
								}

								return 1;
							}
						}))))
				.then(ArgumentBuilderRequired
				.argument("nickname", ArgumentTypeString.string())
				.executes((c) -> {
					CommandSource source = (CommandSource) c.getSource();
					String nickname = (String) c.getArgument("nickname", String.class);
					if (nickname.length() > BTAEssentials.NickLength) {
						throw NICKNAME_TOO_LARGE.create();
					} else if (nickname.isEmpty()) {
						throw NICKNAME_TOO_SMALL.create();
					} else {
						PlayerServer player = (PlayerServer) source.getSender();
						if (player == null) {
							throw CommandExceptions.notInWorld().create();
						} else {
							player.nickname = nickname;
							player.hadNicknameSet = true;
							player.mcServer.playerList.updatePlayerProfile(
								player.username,
								player.nickname,
								player.uuid,
								player.score,
								player.chatColor,
								true,
								player.isOperator()
							);
							source.sendTranslatableMessage(
								"command.commands.nickname.set.success",
								new Object[]{nickname}
							);
							return 1;
						}
					}
				})))).then(ArgumentBuilderLiteral
			.literal("get")
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeEntity.username()).executes((c) -> {
				CommandSource source = (CommandSource) c.getSource();
				EntitySelector entitySelector = (EntitySelector) c.getArgument(
					"target",
					EntitySelector.class
				);
				List<? extends Entity> entities = entitySelector.get(source);
				PlayerServer player = (PlayerServer) entities.get(0);
				source.sendTranslatableMessage(
					"command.commands.nickname.get.success",
					new Object[]{player.username, player.nickname}
				);
				return 1;
			})))).then(((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("reset")
			.then(((ArgumentBuilderRequired) ArgumentBuilderRequired
				.argument("target", ArgumentTypeEntity.username())
				.requires(source -> ((CommandSource) source).hasAdmin())).executes((c) -> {
				CommandSource source = (CommandSource) c.getSource();
				EntitySelector entitySelector = (EntitySelector) c.getArgument(
					"target",
					EntitySelector.class
				);
				List<? extends Entity> entities = entitySelector.get(source);
				PlayerServer player = (PlayerServer) entities.get(0);
				player.nickname = "";
				player.hadNicknameSet = false;
				player.mcServer.playerList.updatePlayerProfile(
					player.username,
					player.nickname,
					player.uuid,
					player.score,
					player.chatColor,
					true,
					player.isOperator()
				);
				if (source.getSender() == player) {
					source.sendTranslatableMessage("command.commands.nickname.reset.success", new Object[0]);
				} else {
					source.sendTranslatableMessage(
						"command.commands.nickname.reset.success_other",
						new Object[]{player.username}
					);
					source.sendTranslatableMessage(
						player,
						"command.commands.nickname.reset.success_receiver",
						new Object[0]
					);
				}

				return 1;
			}))).executes((c) -> {
			CommandSource source = (CommandSource) c.getSource();
			PlayerServer player = (PlayerServer) source.getSender();
			if (player == null) {
				throw CommandExceptions.notInWorld().create();
			} else {
				player.nickname = "";
				player.hadNicknameSet = false;
				player.mcServer.playerList.updatePlayerProfile(
					player.username,
					player.nickname,
					player.uuid,
					player.score,
					player.chatColor,
					true,
					player.isOperator()
				);
				source.sendTranslatableMessage(
					"command.commands.nickname.reset.success",
					new Object[]{player.username}
				);
				return 1;
			}
		})));
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("nick")
			.redirect(command));
	}
}
