package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.player.Player;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wyspr.BTAEssentials.utils.IPlayerData;
import wyspr.BTAEssentials.utils.PlayerData;

@Environment(EnvType.SERVER)
@Mixin(value = PlayerServer.class, remap = false)
public class PlayerServerDataMixin implements IPlayerData {
	@Unique
	PlayerData playerData;

	@Override
	public PlayerData BTAEssentials$getPlayerData(Player player) {
		if (this.playerData == null)
			this.playerData = new PlayerData(player);

		return this.playerData;
	}

	@Override
	public void BTAEssentials$setPlayerData(Player player) {
		this.playerData = new PlayerData(player);
	}
}
