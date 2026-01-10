package wyspr.BTE.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.TextFormatting;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.utils.PlayerData;

import static wyspr.BTE.utils.Utils.requirePlayer;

@SuppressWarnings("ALL")
public class DraftCommand implements CommandManager.CommandRegistry {
	private static final String ENVELOPE        = "✉";
	private static final int    MAX_LINES       = 19;
	private static final int    SUBJECT_MAX_LEN = 30;

	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		String[] literals = {"draft", "drafts"};
		for (String literal : literals) {
			dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
				.literal(literal)
				.executes(MailCommand::viewDraftMailbox)
				.then(ArgumentBuilderRequired
					.argument("message", ArgumentTypeString.greedyString())
					.executes(this::attachMessageToDraft)));
		}
	}

	private @NotNull int attachMessageToDraft(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = requirePlayer(source);
		PlayerData    data   = PlayerData.get(player);

		String message = context.getArgument("message", String.class);

		if (data.mail.selectedDraft == -1) {
			player.sendMessage(TextFormatting.ORANGE + "No draft selected.");
			return 1;
		}

		if (message.length() == 0) {
			player.sendMessage(TextFormatting.ORANGE + "Message can not be blank.");
			return 1;
		}

		String subject = data.mail.attachMessageToDraft(message);
		if (subject == null) {
			player.sendMessage(TextFormatting.ORANGE + "Message not found with subject \"" + TextFormatting.LIGHT_BLUE + subject + TextFormatting.ORANGE + "\"");
		} else {
			player.sendMessage(TextFormatting.YELLOW + "Message attached to draft \"" + TextFormatting.LIGHT_BLUE + subject + TextFormatting.YELLOW + "\"");
		}

		return 1;
	}
}
