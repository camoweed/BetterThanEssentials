package wyspr.BTAEssentials.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import net.minecraft.server.net.handler.PacketHandlerLogin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

@Mixin(value = PacketHandlerLogin.class, remap = false) public class PacketHandlerLoginMixin {
	@Shadow
	@Final
	private MinecraftServer mcServer;

	@Redirect(
		method = "doLogin", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/server/net/PlayerList;load(Lnet/minecraft/server/entity/player/PlayerServer;)V"
	)
	)
	public void loadPlayerData(PlayerList instance, PlayerServer player) {
		this.mcServer.playerList.load(player);
		BTAEssentials.LOGGER.info("Loading player data for: {}", player.username);
		PlayerData.set(player);
	}
}
