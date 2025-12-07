package wyspr.BTE.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.commands.arguments.ArgumentTypeWarp;
import wyspr.BTE.utils.Warps;

@SuppressWarnings("ALL") public class DelWarpCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		CommandNode<Object> command
			= commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("delwarp")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.then(ArgumentBuilderRequired.argument("target", ArgumentTypeWarp.warp()).executes(context -> {
				CommandSource source = (CommandSource) context.getSource();
				Player        player = source.getSender();
				String        target = context.getArgument("target", String.class);

				if (Warps.removeWarp(target)) {
					player.sendMessage(TextFormatting.ORANGE + "Removed warp: " + TextFormatting.YELLOW + target);
				} else {
					player.sendMessage(TextFormatting.ORANGE + "There is no warp named: " + TextFormatting.YELLOW + target);
					player.sendMessage(TextFormatting.ORANGE + "Set a warp with: §3/setwarp [name]");
				}

				return 1;
			})));

		commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("rmwarp")
			.redirect(command));
	}
}
