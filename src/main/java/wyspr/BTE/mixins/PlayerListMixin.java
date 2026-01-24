package wyspr.BTE.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.AllUsersMap;
import wyspr.BTE.utils.PlayerData;

@Mixin(value = PlayerList.class, remap = false)
public class PlayerListMixin {
	@Inject(at = @At("TAIL"), method = "playerLoggedIn")
	public void onLogin(PlayerServer player, CallbackInfo ci) {
		PlayerData.set(player);
		int mailCount = PlayerData.get(player).mail.inbox.size();
		if (mailCount > 0) {
			player.sendMessage(
				TextFormatting.YELLOW + "You have " +
				TextFormatting.ORANGE + mailCount +
				TextFormatting.YELLOW + " unread mail."
			);
		}
		AllUsersMap.updatePlayerID(player);
		Essentials.LOGGER.info("Loading player data for: {}", player.username);
	}

	@Inject(at = @At("HEAD"), method = "playerLoggedOut")
	public void onLogout(PlayerServer player, CallbackInfo ci) {
		PlayerData.set(player);
	}


	@Inject(
		method = "recreatePlayerEntity", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/core/player/inventory/container/ContainerInventory;transferAllContents(Lnet/minecraft/core/player/inventory/container/ContainerInventory;)V"
	), remap = false
	)
	public void keepInfoMP(
		PlayerServer previousPlayer,
		int dimension,
		CallbackInfoReturnable<PlayerServer> cir,
		@Local(name = "newPlayer") final PlayerServer newPlayer
	)
	{
		if (previousPlayer.passenger instanceof Player) {
			previousPlayer.ejectRider();
		}
		newPlayer.score = (int) (previousPlayer.score * Essentials.DeathCost);
	}
}
