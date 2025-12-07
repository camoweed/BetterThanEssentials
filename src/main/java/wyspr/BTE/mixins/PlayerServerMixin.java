package wyspr.BTE.mixins;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.player.PlayerListBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

@Mixin(value = PlayerServer.class, remap = false) public abstract class PlayerServerMixin extends Player {
	public PlayerServerMixin(World world) {
		super(world);
		PlayerData.set(this);
	}

	@Override
	public void onDeath(Entity entityKilledBy) {
		if (Essentials.BackOnDeath) {
			PlayerData
				.get(this)
				.updateBackPos();
		}
		super.onDeath(entityKilledBy);
	}

	@Override
	public double getRidingHeight() {
		if (Essentials.HeadSit) {
			return super.getRidingHeight() + 0.5;
		} else {
			return super.getRidingHeight();
		}
	}

	@Inject(
		method = "setGamemode", at = @At("HEAD")
	)
	public void updateListOnGamemode(Gamemode newGamemode, CallbackInfo ci) {
		PlayerData playerData = PlayerData.get(this);
		if (playerData.vanished) {
			if (
				this.gamemode == Gamemode.spectator
			 && newGamemode   != Gamemode.spectator
			) {
				playerData.removeVanish();
				PlayerListBox.updateList();
			}
		}
	}
}
