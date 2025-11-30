package wyspr.BTAEssentials.mixin;

import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PacketHandlerServer.class, remap = false) public abstract class PacketHandlerServerMixin {
//	@Shadow
//	private PlayerServer playerEntity;
//
//	@Inject(
//		method = "handleRespawn", at = @At(
//		shift = At.Shift.BEFORE, value = "INVOKE", target = "Lnet/minecraft/server/net/PlayerList;recreatePlayerEntity(Lnet/minecraft/server/entity/player/PlayerServer;I)Lnet/minecraft/server/entity/player/PlayerServer;"
//	)
//	)
//	public void respawnHook(PacketRespawn packet, CallbackInfo ci) {
//		BTAEssentials.LOGGER.info("RESPAWN");
//		PlayerData.set(this.playerEntity);
//	}

	@Redirect(
		method = "handleChat",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/String;trim()Ljava/lang/String;"
		)
	)
	public String redirectChat(String message) {
		if (message.startsWith("> ")) {
			message = "§d" + message;
		} else {
			message = message.replace("$$", "§");
		}

		return message;
	}
}
