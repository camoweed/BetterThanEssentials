package wyspr.BTE.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketUpdatePlayerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

import java.util.Objects;

import static wyspr.BTE.utils.Utils.playNotificationAtPlayer;

@Mixin(value = PacketHandlerServer.class, remap = false)
public class PacketHandlerServerMixin {
	@Shadow
	private PlayerServer playerEntity;

	@WrapOperation(
		method = "handleChat", at = @At(
		value = "INVOKE", target = "Ljava/lang/String;trim()Ljava/lang/String;"

	)
	)
	public String greentextAndColorChat(String message, Operation<String> original) {
		if (Essentials.GreenText && message.startsWith("> ")) {
			message = TextFormatting.GREEN + message;
		} else if (Essentials.ColorChat) {
			message = message.replace("$$", "§");
		}

		return original.call(message);
	}

	/// Sends muted players messages to server ops
	@WrapOperation(
		method = "handleChat", at = @At(
		value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"
	)
	)
	public void redirectMutePlayer(Logger logger, String message, Operation<Void> original) {
		if (PlayerData.get(this.playerEntity).muted) {
			MinecraftServer.getInstance().playerList.sendChatMessageToAllOps(TextFormatting.RED + (TextFormatting.BOLD + "[MUTED] ") + TextFormatting.RESET + message);
		}
		logger.info(message);
	}

	/// Prevents muted players from chatting & plays a sound at them
	@WrapOperation(
		method = "handleChat", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/server/net/PlayerList;sendEncryptedChatToAllPlayers(Ljava/lang/String;)V"
	)
	)
	public void mutePlayer(PlayerList instance, String s, Operation<Void> original) {
		if (PlayerData.get(this.playerEntity).muted) {
			playNotificationAtPlayer(this.playerEntity, Essentials.MutedSound);
			this.playerEntity.sendMessage(TextFormatting.RED + "You have been muted.");
			return;
		}
		original.call(instance, s);
	}

	/// Ride player feature
	@WrapOperation(
		method = "handleUseEntity", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/server/entity/player/PlayerServer;useCurrentItemOnEntity(Lnet/minecraft/core/entity/Entity;)Z"
	)
	)
	public boolean ridePlayer(PlayerServer player, Entity targetEntity, Operation<Boolean> original) {
		if (Essentials.HeadSit) {
			if (targetEntity instanceof Player) {
				if (player.getCurrentEquippedItem() != null) {
					return player.useCurrentItemOnEntity(targetEntity);
				}

				Player targetPlayer = (Player) targetEntity;
				if (Essentials.HeadSitSaddle) {
					ItemStack headSlot = targetPlayer.inventory.armorInventory[3];
					if (Objects.nonNull(headSlot)) {
						if (Objects.equals(headSlot.getItem(), Items.SADDLE)) {
							return mountPlayer(player, targetEntity);
						}
					}
				} else {
					return mountPlayer(player, targetEntity);
				}

			}
		}

		return original.call(player, targetEntity);
	}

	/// Ride player feature
	@Unique
	private static boolean mountPlayer(PlayerServer player, Entity targetEntity) {
		player.startRiding(targetEntity);

		player.collision = false;
		player.noPhysics = true;
		return true;
	}

	/// Ride player feature
	@Inject(
		method = "handlePlayerState", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/core/world/IVehicle;ejectRider()Lnet/minecraft/core/entity/Entity;",
		shift = At.Shift.AFTER
	)
	)
	public void playerRideEject(PacketUpdatePlayerState updatePlayerStatePacket, CallbackInfo ci) {
		if (this.playerEntity.vehicle instanceof Player) {
			this.playerEntity.collision = true;
			this.playerEntity.noPhysics = false;
		}
	}
}
