package wyspr.BTE.utils.UI;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketContainerClose;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.Nullable;
import wyspr.BTE.utils.PlayerData;
import wyspr.BTE.utils.PlayerData.MailManager.Mail;

import java.util.List;

public class Mailbox implements Container {
	public static final  String      ENVELOPE               = "✉";
	private static final int         MAX_TOOLTIP_TEXT_WIDTH = 50;
	private final        Player      player;
	private final        PlayerData  playerData;
	private final        MailboxType type;

	private Mailbox(MailboxType type, Player player) {
		this.type       = type;
		this.player     = player;
		this.playerData = PlayerData.get(player);
	}

	public static Mailbox inbox(Player player) {
		return new Mailbox(MailboxType.Inbox, player);
	}

	public static Mailbox read(Player player) {
		return new Mailbox(MailboxType.Read, player);
	}

	public static Mailbox drafts(Player player) {
		return new Mailbox(MailboxType.Drafts, player);
	}

	@Override
	public int getContainerSize() {
		return 36;
	}

	@Override
	public @Nullable ItemStack getItem(int i) {
		switch (this.type) {
			case Inbox:
				return getMailItem(i, this.playerData.mail.inbox);
			case Read:
				return getMailItem(i, this.playerData.mail.readMail);
			case Drafts:
				return getDraftItem(this.playerData, i);
		}
		return null;
	}

	@Override
	public @Nullable ItemStack removeItem(int i, int j) {
		switch (this.type) {
			case Inbox:
				player.displayTrommelScreen(MailSelector.inbox(player, i, false, this.getItem(i)));
				break;
			case Read:
				player.displayTrommelScreen(MailSelector.read(player, i, false, this.getItem(i)));
				break;
			case Drafts:
				player.displayTrommelScreen(MailSelector.drafts(player, i, false, this.getItem(i)));
				break;
		}
		return null;
	}

	public static ItemStack getMailItem(int index, List<Mail> mailList) {
		try {
			Mail mail = mailList.get(index);
			String message = mail.message != null
				? mail.message
				: TextFormatting.LIGHT_BLUE + "No message set";

			ItemStack item = new ItemStack(Items.PAPER, 0);

			item.setCustomName(
				TextFormatting.RESET + "" +
					TextFormatting.LIGHT_GRAY + "From: " +
					TextFormatting.RESET + mail.senderDisplayname +
					TextFormatting.LIGHT_GRAY + " (" +
					TextFormatting.WHITE + mail.senderUsername +
					TextFormatting.LIGHT_GRAY + ")\n" +
					TextFormatting.LIGHT_GRAY + "Sent at: " + TextFormatting.LIGHT_BLUE + mail.getSentDate() + "\n" +
					TextFormatting.LIGHT_GRAY + "Subject: " + TextFormatting.YELLOW + mail.subject + "\n" +
					TextFormatting.GRAY + TextFormatting.STRIKETHROUGH + "                                            \n" +
					TextFormatting.LIGHT_GRAY + "Message: " + TextFormatting.RESET + wrapMessage(
					message,
					MAX_TOOLTIP_TEXT_WIDTH
				) +
					TextFormatting.GRAY + TextFormatting.STRIKETHROUGH + "                                            "
			);

			return item;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public static ItemStack getDraftItem(PlayerData playerData, int index) {
		try {
			Mail mail = playerData.mail.drafts.get(index);
			String message = mail.message != null
				? mail.message
				: TextFormatting.LIGHT_BLUE + "No message set";

			ItemStack item = new ItemStack(
				playerData.mail.selectedDraft == index
					? Items.MAP
					: Items.PAPER,
				0
			);

			if (playerData.mail.selectedDraft == index) {
				item
					.getData()
					.putBoolean("initialized", true);
			}

			String msg = wrapMessage(message, MAX_TOOLTIP_TEXT_WIDTH);

			item.setCustomName(
				TextFormatting.RESET + "" +
				TextFormatting.LIGHT_GRAY + "Subject: " + TextFormatting.RESET + mail.subject + "\n" +
				TextFormatting.GRAY + TextFormatting.STRIKETHROUGH + "                                            \n" +
				TextFormatting.LIGHT_GRAY + "Message: " + TextFormatting.RESET + msg + '\n' +
				TextFormatting.GRAY + TextFormatting.STRIKETHROUGH + "                                            "
			);

			return item;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public static String wrapMessage(String message, int lineLength) {
		int messageLen = message.length();
		int lines      = (int) Math.ceil((double) messageLen / (double) lineLength);

		if (messageLen < lineLength) {
			return message;
		}

		String output       = "";
		String activeFormat = "";

		for (int i = 0; i < lines; i++) {
			int index = i * lineLength;

			output += message
				.substring(index, Math.min(index + lineLength, messageLen - 1))
				.trim();
			int lastFormat = output.lastIndexOf('§');
			if (lastFormat != -1) {
				char code = message.charAt(i + 1);

				if (code == 'r') {
					activeFormat = "";
				}
				// Color codes wipe previous colors but keep styles
				if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
					activeFormat = "§" + code;
				}

				// Formatting codes stack
				//noinspection SpellCheckingInspection
				if ("klmno".indexOf(code) >= 0) {
					if (!activeFormat.contains("§" + code)) {
						activeFormat = activeFormat + "§" + code;
					}
				}
				i++;
				continue;
			}
			if (i != lines - 1) {
				output += '\n' + activeFormat;
			}
		}

		return output;
	}

	@Override
	public void setItem(int i, @Nullable ItemStack itemStack) {
		if (itemStack != null) {
			// Close the GUI
			((PlayerServer) player).playerNetServerHandler.sendPacket(new PacketContainerClose());
			// Return the removed item to the player
			player.inventory.insertItem(itemStack, true);
		}
	}

	@Override
	public String getNameTranslationKey() {
		return this.type.toString();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public void setChanged() {}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void sortContainer() {}

	public enum MailboxType {
		Inbox, Read, Drafts;

		@Override
		public String toString() {
			switch (this) {
				case Inbox:
					return ENVELOPE + " Inbox";
				case Read:
					return ENVELOPE + " Read mail";
				case Drafts:
					return ENVELOPE + " Drafts";
			}
			return "Mailbox";
		}
	}
}
