package wyspr.BTE.mixins;

import net.minecraft.core.entity.player.Player;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wyspr.BTE.utils.PlayerData;

@Mixin(value = PlayerServer.class, remap = false)
public class PlayerDataInterfaceMixin implements PlayerData.Interface {
	@Unique
	PlayerData playerData;

	@Override
	public PlayerData betterThanEssentials$getPlayerData(Player player) {
		if (this.playerData == null) this.playerData = new PlayerData(player);

		return this.playerData;
	}

	@Override
	public void betterThanEssentials$setPlayerData(Player player) {
		this.playerData = new PlayerData(player);
	}
}
