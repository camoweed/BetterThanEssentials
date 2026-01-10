package wyspr.BTE.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.arguments.ArgumentTypeGameMode;
import net.minecraft.core.net.command.commands.CommandGameMode;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.core.net.command.util.CommandHelper;
import net.minecraft.core.player.gamemode.Gamemode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import wyspr.BTE.Essentials;

import java.util.List;

@SuppressWarnings("ALL")
@Mixin(value = CommandGameMode.class, remap = false)
public class CommandGameModeMixin {
	@Overwrite
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		String[] literals = {"gm", "gamemode"};
		for (String literal : literals) {
			dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin() || Essentials.GamemodeCommand)
				.then(ArgumentBuilderRequired
					.argument("gamemode", ArgumentTypeGameMode.gameMode())
					.executes(context -> {
						CommandSource source   = (CommandSource) context.getSource();
						Gamemode      gameMode = (Gamemode) context.getArgument("gamemode", Gamemode.class);
						if (source.getSender() == null) {
							throw CommandExceptions
								.notInWorld()
								.create();
						} else {
							source
								.getSender()
								.setGamemode(gameMode);
							source.sendTranslatableMessage(
								"command.commands.gamemode.success_self",
								new Object[]{I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name")}
							);
							return 1;
						}
					}))
				.then(ArgumentBuilderRequired
					.argument("targets", ArgumentTypeEntity.usernames())
					.requires(source -> ((CommandSource) source).hasAdmin())
					.executes((c) -> {
						CommandSource source   = (CommandSource) c.getSource();
						Gamemode      gameMode = (Gamemode) c.getArgument("gamemode", Gamemode.class);
						EntitySelector entitySelector = (EntitySelector) c.getArgument(
							"targets",
							EntitySelector.class
						);
						List<? extends Entity> entities = entitySelector.get((CommandSource) c.getSource());

						for (Entity entity : entities) {
							((Player) entity).setGamemode(gameMode);
							if (entity != source.getSender()) {
								source.sendTranslatableMessage(
									(Player) entity,
									"command.commands.gamemode.success_receiver",
									new Object[0]
								);
							}
						}

						if (entities.size() == 1) {
							if (entities.get(0) == source.getSender()) {
								source.sendTranslatableMessage(
									"command.commands.gamemode.success_self",
									new Object[]{I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name")}
								);
							} else {
								source.sendTranslatableMessage(
									"command.commands.gamemode.success_other", new Object[]{
										CommandHelper.getEntityName((Entity) entities.get(0)),
										I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name")
									}
								);
							}
						}

						return 1;
					}))

			);
		}
	}
}
