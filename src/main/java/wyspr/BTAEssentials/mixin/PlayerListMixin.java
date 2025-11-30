package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.entity.player.PlayerServer;
import wyspr.BTAEssentials.utils.PlayerData;
import net.minecraft.server.net.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(value = PlayerList.class, remap = false)
public class PlayerListMixin {
	@Inject(at = @At("TAIL"), method = "playerLoggedIn")
	public void onLogin(PlayerServer player, CallbackInfo ci) {
		PlayerData.set(player);
	}

	@Inject(at = @At("HEAD"), method = "playerLoggedOut")
	public void onLogout(PlayerServer entityplayermp, CallbackInfo ci) {
		PlayerData.set(entityplayermp);
	}
}
