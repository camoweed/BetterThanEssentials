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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import wyspr.BTE.Essentials;
import wyspr.BTE.commands.arguments.ArgumentTypeAllUsers;
import wyspr.BTE.utils.AllUsersMap;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.MailManager;
import wyspr.BTE.utils.PlayerData.MailManager.Mail;
import wyspr.BTE.utils.UI.Mailbox;

import java.util.List;

import static wyspr.BTE.utils.UI.Mailbox.ENVELOPE;
import static wyspr.BTE.utils.Utils.playNotificationAtPlayer;
import static wyspr.BTE.utils.Utils.requirePlayer;

@SuppressWarnings("ALL")
public class MailCommand implements CommandManager.CommandRegistry {
	private static final int MAX_LINES       = 19;
	private static final int SUBJECT_MAX_LEN = 40;

	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register((ArgumentBuilderLiteral) ArgumentBuilderLiteral
			.literal("mail")
			.executes(this::viewMailbox)
			.then(ArgumentBuilderLiteral
				.literal("view")
				.executes(this::viewMailbox)
				.then(ArgumentBuilderLiteral
					.literal("inbox")
					.executes(this::viewMailbox))
				.then(ArgumentBuilderLiteral
					.literal("read")
					.executes(this::viewReadMailbox))
				.then(ArgumentBuilderLiteral
					.literal("draft")
					.executes(MailCommand::viewDraftMailbox)))
			.then(ArgumentBuilderLiteral
				.literal("send")
				.then(ArgumentBuilderRequired
					.argument("recipient", ArgumentTypeAllUsers.all())
					.executes(this::sendMail)))
			.then(ArgumentBuilderLiteral
				.literal("draft")
				.executes(MailCommand::viewDraftMailbox)
				.then(ArgumentBuilderLiteral
					.literal("new")
					.then(ArgumentBuilderRequired
						.argument("subject", ArgumentTypeString.greedyString())
						.executes(MailCommand::createDraftMailOutline)))));
	}

	private @NotNull int viewMailbox(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		Player        player     = requirePlayer(source);
		PlayerData    playerData = PlayerData.get(player);

		List<Mail> unread = playerData.mail.inbox;

		if (unread.isEmpty()) {
			player.sendMessage(TextFormatting.YELLOW + "You have no mail!");
			return 1;
		}

		player.displayContainerScreen(Mailbox.inbox(player));

		return 1;
	}

	public @NotNull int viewReadMailbox(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		Player        player     = requirePlayer(source);
		PlayerData    playerData = PlayerData.get(player);

		List<Mail> read = playerData.mail.readMail;

		if (read.isEmpty()) {
			player.sendMessage(TextFormatting.YELLOW + "You have no read mail!");
			return 1;
		}

		player.displayContainerScreen(Mailbox.read(player));

		return 1;
	}

	public static @NotNull int viewDraftMailbox(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source     = (CommandSource) context.getSource();
		Player        player     = requirePlayer(source);
		PlayerData    playerData = PlayerData.get(player);

		List<Mail> drafts = playerData.mail.drafts;

		if (drafts.isEmpty()) {
			player.sendMessage(TextFormatting.YELLOW + "You have no drafts!");
			return 1;
		}

		player.displayContainerScreen(Mailbox.drafts(player));

		return 1;
	}

	private @NotNull int sendMail(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        sender = requirePlayer(source);

		String recipientUsername = context.getArgument("recipient", String.class);

		PlayerData  senderData  = PlayerData.get(sender);
		MailManager mailManager = new MailManager(AllUsersMap.getUUID(recipientUsername));

		if (mailManager.inbox.size() >= Essentials.MAX_MAILS) {
			sender.sendMessage(
				TextFormatting.YELLOW + recipientUsername +
				TextFormatting.ORANGE + " has a full inbox and cannot recieve mails."
			);
			return 1;
		}

		boolean draftSelected = senderData.mail.sendDraft(mailManager);

		if (!draftSelected) {
			sender.sendMessage(TextFormatting.RED + "No draft selected.");
			return 1;
		}

		sender.sendMessage(TextFormatting.GREEN + "Mail sent to " + TextFormatting.YELLOW + recipientUsername + "!");

		PlayerServer recipient = MinecraftServer.getInstance().playerList.getPlayerEntity(recipientUsername);
		if (recipient != null) {
			playNotificationAtPlayer(recipient, Essentials.MailNotificationSound);
			recipient.sendMessage(TextFormatting.LIME + ENVELOPE + " You received new mail from " + TextFormatting.RESET + sender.getDisplayName());
			PlayerData.get(recipient).mail.reload();
		}
		return 1;
	}

	public static @NotNull int createDraftMailOutline(CommandContext<Object> context) throws CommandSyntaxException {
		CommandSource source = (CommandSource) context.getSource();
		Player        player = requirePlayer(source);
		PlayerData    data   = PlayerData.get(player);

		if (data.mail.drafts.size() >= Essentials.MAX_MAILS) {
			player.sendMessage(
				TextFormatting.ORANGE + "You have a full draft mailbox and cannot create drafts."
			);
			return 1;
		}

		String subject = context
			.getArgument("subject", String.class)
			.trim();

		if (subject.isEmpty()) {
			player.sendMessage(TextFormatting.RED + "Subject cannot be empty.");
			return 1;
		}

		if (subject.length() > SUBJECT_MAX_LEN) {
			subject = subject.substring(0, SUBJECT_MAX_LEN - 1) + "…";
		}

		boolean draftAdded = data.mail.newDraftOutline(subject, player);

		if (draftAdded) {
			int index = data.mail.drafts.size() - 1;

			player.sendMessage(
				TextFormatting.YELLOW + "Draft created with subject: " +
					TextFormatting.LIGHT_GRAY + subject
			);
			player.sendMessage(TextFormatting.LIGHT_GRAY + "(Select message with /draft)");
		} else {
			player.sendMessage(TextFormatting.ORANGE + "A draft with that subject already exists!");
			player.sendMessage(TextFormatting.LIGHT_GRAY + "(View drafts with /draft)");

		}

		return 1;
	}
}
