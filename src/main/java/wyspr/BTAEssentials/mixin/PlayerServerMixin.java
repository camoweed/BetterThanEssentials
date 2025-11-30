package wyspr.BTAEssentials.mixin;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import wyspr.BTAEssentials.BTAEssentials;
import wyspr.BTAEssentials.utils.PlayerData;

@Mixin(value = PlayerServer.class, remap = false) public abstract class PlayerServerMixin extends Player {
	public PlayerServerMixin(World world) {
		super(world);
		PlayerData.set(this);
	}

//	@Override
//	public void tick() {
//		PlayerData.get(this).tick();
//		super.tick();
//	}

	@Override
	public void onDeath(Entity entityKilledBy) {
		if (BTAEssentials.BackOnDeath) {
			PlayerData.get(this).updateBackPos();
		}
		super.onDeath(entityKilledBy);
	}
}
