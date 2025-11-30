package wyspr.BTAEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTAEssentials.utils.ContainerInvsee;

import java.util.List;

@SuppressWarnings("ALL") public class InvseeCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.then(ArgumentBuilderRequired
				.argument("target", ArgumentTypeEntity.username())
				.executes(context -> {
					CommandSource source = (CommandSource) context.getSource();
					EntitySelector entitySelector = (EntitySelector) context.getArgument(
						"target",
						EntitySelector.class
					);
					List<? extends Entity> entities = entitySelector.get(source);
					PlayerServer           target   = (PlayerServer) entities.get(0);
					Player                 player   = source.getSender();
					player.displayContainerScreen(new ContainerInvsee(target));

					return 1;
				})));

		String[] literals = {"invsee", "openinv"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.redirect(command));
		}
	}
}
