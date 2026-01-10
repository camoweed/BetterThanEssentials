package wyspr.BTE.commands.warp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import wyspr.BTE.utils.WarpsManager;
import wyspr.BTE.utils.WorldPosition;

@SuppressWarnings("ALL")
public class SetWarpCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		String[] literals = {"setwarp", "addwarp"};
		for (String literal : literals) {
			commandDispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.requires(source -> ((CommandSource) source).hasAdmin())
				.then(ArgumentBuilderRequired
					.argument("target", ArgumentTypeString.string())
					.executes(this::exec)));
		}
	}

	private int exec(CommandContext<Object> context) {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();
		String        target = context.getArgument("target", String.class);

		if (WarpsManager.addWarp(
			target,
			new WorldPosition(player.x, player.y, player.z, player.dimension)
		)) {
			player.sendMessage(TextFormatting.ORANGE + "Created warp: " + TextFormatting.YELLOW + target);
		} else {
			player.sendMessage(TextFormatting.YELLOW + "" + target + TextFormatting.ORANGE + " already exists");
		}

		return 1;
	}
}
