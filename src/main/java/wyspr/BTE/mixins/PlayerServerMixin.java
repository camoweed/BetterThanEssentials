package wyspr.BTE.mixins;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.player.PlayerListBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wyspr.BTE.Essentials;
import wyspr.BTE.utils.PlayerData;

@Mixin(value = PlayerServer.class, remap = false)
public abstract class PlayerServerMixin extends Player {
	public PlayerServerMixin(World world) {
		super(world);
		PlayerData.set(this);
	}

	@Override
	public boolean isInLava() {
		if (PlayerData.get(this).godMode) return false;
		return super.isInLava();
	}

	@Override
	public boolean isOnFire() {
		if (PlayerData.get(this).godMode) return false;
		return super.isOnFire();
	}

	@Override
	public void onDeath(Entity entityKilledBy) {
		if (Essentials.BackOnDeath) {
			PlayerData.get(this).tpManager.updateBackPos();
		}
		if (PlayerData.get(this).godMode) return;
		super.onDeath(entityKilledBy);
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		if (PlayerData.get(this).godMode) return false;
		return super.hurt(attacker, damage, type);
	}

	@Override
	public void lavaHurt() {
		if (PlayerData.get(this).godMode) return;
		super.lavaHurt();
	}

	@Override
	public void fireHurt() {
		if (PlayerData.get(this).godMode) return;
		super.fireHurt();
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
			if (this.gamemode == Gamemode.spectator && newGamemode != Gamemode.spectator) {
				playerData.removeVanish();
				PlayerListBox.updateList();
			}
		}
	}
}
