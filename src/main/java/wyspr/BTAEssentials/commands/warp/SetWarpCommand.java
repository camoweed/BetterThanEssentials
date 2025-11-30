package wyspr.BTAEssentials.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import wyspr.BTAEssentials.utils.Warps;
import wyspr.BTAEssentials.utils.WorldPosition;

@SuppressWarnings("ALL") public class SetWarpCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("addwarp")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeString.string()))
			.executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        player = source.getSender();
				String        target = context.getArgument("target", String.class);

				if (Warps.addWarp(
					target,
					new WorldPosition(player.x, player.y, player.z, player.dimension)
				)) {
					player.sendMessage("§1Created warp: §4" + target);
				} else {
					player.sendMessage("§4" + target + "§1 already exists");
				}

				return 1;
			}));

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("setwarp")
			.redirect(command));
	}
}
