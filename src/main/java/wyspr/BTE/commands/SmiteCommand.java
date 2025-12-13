package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.EntityLightning;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.commands.arguments.ArgumentTypeUser;
import wyspr.BTE.utils.Utils;

@SuppressWarnings("ALL") public class SmiteCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("smite")
			.requires(source -> ((CommandSource) source).hasAdmin())
			.executes(this::noArg)
			.then(ArgumentBuilderRequired
				.argument("player", ArgumentTypeUser.user())
				.executes(this::userArg)));
	}

	private @NotNull int noArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source    = (CommandSource) context.getSource();
		Player        player    = source.getSender();
		HitResult     hitresult = Utils.rayCastFromPlayer((PlayerServer) player, 100);

		if (hitresult == null) {
			player.sendMessage(TextFormatting.ORANGE + "No block in sight");
		} else {
			player.world.addWeatherEffect(new EntityLightning(
				player.world,
				hitresult.x,
				hitresult.y,
				hitresult.z
			));
			player.sendMessage(TextFormatting.YELLOW + "Struck at: " +
				TextFormatting.LIGHT_BLUE + String.format(
				"%d, %d, %d",
				hitresult.x,
				hitresult.y,
				hitresult.z
			));
		}

		return 1;
	}

	private @NotNull int userArg(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = source.getSender();
		PlayerServer  target  = context.getArgument("player", PlayerServer.class);

		target.world.addWeatherEffect(new EntityLightning(
			target.world,
			target.x,
			target.y,
			target.z
		));
		player.sendMessage(TextFormatting.YELLOW + "Struck " +
			TextFormatting.RESET + target.getDisplayName() +
			TextFormatting.YELLOW + " at: " +
			TextFormatting.LIGHT_BLUE + String.format(
			"%d, %d, %d",
				(int)target.x,
				(int)target.y,
				(int)target.z
		) +
			TextFormatting.YELLOW + " in " +
			TextFormatting.LIGHT_BLUE + target.world.dimension.getTranslatedName()
		);

		return 1;
	}
}
