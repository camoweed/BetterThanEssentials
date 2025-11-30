package wyspr.BTAEssentials.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityFishingBobber;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.util.phys.Vec3;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import wyspr.BTAEssentials.BTAEssentials;

import java.util.List;

@Environment(EnvType.SERVER)
@Mixin(value = EntityFishingBobber.class, remap = false) public abstract class EntityFishingBobberMixin extends Entity {
	@Shadow
	public  Player owner;
	@Shadow
	public  Entity hookedEntity;
	@Shadow
	private int    xTile;
	@Shadow
	private int    yTile;
	@Shadow
	private int    zTile;
	@Shadow
	private int    ticksInAir;
	@Shadow
	private int    ticksCatchable;
	@Shadow
	private int    lerpSteps;
	@Shadow
	private double lerpX;
	@Shadow
	private double lerpY;
	@Shadow
	private double lerpZ;
	@Shadow
	private double lerpYRot;
	@Shadow
	private double lerpXRot;

	public EntityFishingBobberMixin(@Nullable World world) {
		super(world);
	}

	/**
	 * @author wyspr
	 * @reason Allow modification of catch timer
	 */
	@Overwrite
	public void tick() {
		super.tick();
		if (this.lerpSteps > 0) {
			double d = this.x + (this.lerpX - this.x) / (double) this.lerpSteps;
			double d1 = this.y + (this.lerpY - this.y) / (double) this.lerpSteps;
			double d2 = this.z + (this.lerpZ - this.z) / (double) this.lerpSteps;

			double d4;
			for (d4 = this.lerpYRot - (double) this.yRot; d4 < (double) -180.0F; d4 += 360.0F) {
			}

			while (d4 >= (double) 180.0F) {
				d4 -= 360.0F;
			}

			this.yRot = (float) ((double) this.yRot + d4 / (double) this.lerpSteps);
			this.xRot
				= (float) ((double) this.xRot + (this.lerpXRot - (double) this.xRot) / (double) this.lerpSteps);
			--this.lerpSteps;
			this.setPos(d, d1, d2);
			this.setRot(this.yRot, this.xRot);
		} else {
			if (!this.world.isClientSide) {
				ItemStack heldPlayerItem = this.owner.getCurrentEquippedItem();
				if (this.owner.removed || !this.owner.isAlive() || heldPlayerItem == null || heldPlayerItem.getItem() != Items.TOOL_FISHINGROD || this.distanceToSqr(
					this.owner) > (double) 1024.0F) {
					this.remove();
					this.owner.bobberEntity = null;
					return;
				}

				if (this.hookedEntity != null) {
					if (!this.hookedEntity.removed) {
						this.x = this.hookedEntity.x;
						this.y = this.hookedEntity.bb.minY + (double) this.hookedEntity.bbHeight * 0.8;
						this.z = this.hookedEntity.z;
						if (this.hookedEntity instanceof MobPathfinder) {
							((MobPathfinder) this.hookedEntity).setTarget(this.owner);
						}

						double dx = this.owner.x - this.x;
						double dy = this.owner.y - this.y;
						double dz = this.owner.z - this.z;
						double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
						if (distance > (double) 10.0F) {
							double scale = 0.01;
							Entity var10000 = this.hookedEntity;
							var10000.xd += dx * scale;
							var10000 = this.hookedEntity;
							var10000.yd += dy * scale;
							var10000 = this.hookedEntity;
							var10000.zd += dz * scale;
						}

						return;
					}

					this.hookedEntity = null;
				}
			}

			if (this.isInGround()) {
				if (this.world.getBlockId(this.xTile, this.yTile, this.zTile) == Blocks.ROPE.id()) {
					this.x = (double) this.xTile + (double) 0.5F;
					this.y = (double) this.yTile + (double) 0.5F;
					this.z = (double) this.zTile + (double) 0.5F;
					return;
				}

				this.setInGround(false);
				this.xd *= this.random.nextFloat() * 0.2F;
				this.yd *= this.random.nextFloat() * 0.2F;
				this.zd *= this.random.nextFloat() * 0.2F;
				this.ticksInAir = 0;
				this.ticksCatchable = 0;
			}

			++this.ticksInAir;
			Vec3 currentPos = Vec3.getTempVec3(this.x, this.y, this.z);
			Vec3 nextPos = Vec3.getTempVec3(this.x + this.xd, this.y + this.yd, this.z + this.zd);
			HitResult clip = this.world.checkBlockCollisionBetweenPoints(currentPos, nextPos);
			currentPos = Vec3.getTempVec3(this.x, this.y, this.z);
			nextPos = Vec3.getTempVec3(this.x + this.xd, this.y + this.yd, this.z + this.zd);
			if (clip != null) {
				nextPos = Vec3.getTempVec3(clip.location.x, clip.location.y, clip.location.z);
				if (clip.hitType == HitResult.HitType.TILE && this.world.getBlockId(
					clip.x,
					clip.y,
					clip.z
				) == Blocks.ROPE.id()) {
					this.setInGround(true);
					this.xTile = clip.x;
					this.yTile = clip.y;
					this.zTile = clip.z;
				}
			}

			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(
				this,
				this.bb
					.expand(
						this.xd,
						this.yd,
						this.zd
					)
					.grow(1.0F, 1.0F, 1.0F)
			);
			double d3 = 0.0F;

			for (Entity e : list) {
				if (e.isPickable() && (e != this.owner || this.ticksInAir >= 5)) {
					float f2 = 0.3F;
					AABB aabb = e.bb.grow(f2, f2, f2);
					HitResult newHitResult = aabb.clip(currentPos, nextPos);
					if (newHitResult != null) {
						double d6 = currentPos.distanceTo(newHitResult.location);
						if (d6 < d3 || d3 == (double) 0.0F) {
							entity = e;
							d3 = d6;
						}
					}
				}
			}

			if (entity != null) {
				clip = new HitResult(entity);
			}

			if (clip != null && clip.entity != null && clip.entity.hurt(this.owner, 0, DamageType.COMBAT)) {
				this.hookedEntity = clip.entity;
			}

			this.move(this.xd, this.yd, this.zd);
			float f = MathHelper.sqrt(this.xd * this.xd + this.zd * this.zd);
			this.yRot = (float) (Math.atan2(this.xd, this.zd) * (double) 180.0F / Math.PI);

			for (this.xRot = (float) (Math.atan2(
				this.yd,
				f
			) * (double) 180.0F / Math.PI); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
			}

			while (this.xRot - this.xRotO >= 180.0F) {
				this.xRotO += 360.0F;
			}

			while (this.yRot - this.yRotO < -180.0F) {
				this.yRotO -= 360.0F;
			}

			while (this.yRot - this.yRotO >= 180.0F) {
				this.yRotO += 360.0F;
			}

			this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
			this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;
			float movementScale = 0.92F;
			if (this.onGround || this.horizontalCollision) {
				movementScale = 0.5F;
			}

			int k = 5;
			double d5 = 0.0F;

			for (int l = 0; l < k; ++l) {
				double d8
					= this.bb.minY + (this.bb.maxY - this.bb.minY) * (double) l / (double) k - (double) 0.125F + (double) 0.125F;
				double d9
					= this.bb.minY + (this.bb.maxY - this.bb.minY) * (double) (l + 1) / (double) k - (double) 0.125F + (double) 0.125F;
				AABB axisalignedbb1 = AABB.getTemporaryBB(
					this.bb.minX,
					d8,
					this.bb.minZ,
					this.bb.maxX,
					d9,
					this.bb.maxZ
				);
				if (this.world.isAABBInMaterial(axisalignedbb1, Material.water)) {
					d5 += (double) 1.0F / (double) k;
				}
			}

			if (d5 > (double) 0.0F) {
				if (this.ticksCatchable > 0) {
					--this.ticksCatchable;
				} else {
					int catchRate = 500;
					int rainRate = 0;
					int algaeRate = 0;
					if (this.world.canBlockBeRainedOn(
						MathHelper.floor(this.x),
						MathHelper.floor(this.y) + 1,
						MathHelper.floor(this.z)
					)) {
						rainRate = 200;
					}

					if (this.world.getBlockId(
						MathHelper.floor(this.x),
						MathHelper.floor(this.y) + 1,
						MathHelper.floor(this.z)
					) == Blocks.ALGAE.id()) {
						algaeRate = 100;
					}

					catchRate = catchRate - rainRate - algaeRate;
					if (this.random.nextInt(catchRate) == 0) {
						// CODE MODIFICATION
						// from:
						// this.ticksCatchable = this.random.nextInt(30) + 10;
						// to:
						if (BTAEssentials.AddedTicksCatchable <=-40) {
							this.remove();
						} else {
							this.ticksCatchable = this.random.nextInt(30) + 10 + BTAEssentials.AddedTicksCatchable;
						}
						// END
						this.yd -= 0.2;
						this.world.playSoundAtEntity(
							null,
							this,
							"random.splash",
							0.25F,
							1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F
						);
						float f3 = (float) MathHelper.floor(this.bb.minY);

						for (int i1 = 0; (float) i1 < 1.0F + this.bbWidth * 20.0F; ++i1) {
							double xOff = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
							double zOff = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
							this.world.spawnParticle(
								"bubble",
								this.x + xOff,
								f3 + 1.0F,
								this.z + zOff,
								this.xd,
								this.yd - (double) (this.random.nextFloat() * 0.2F),
								this.zd,
								0
							);
						}

						for (int j1 = 0; (float) j1 < 1.0F + this.bbWidth * 20.0F; ++j1) {
							double xOff = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
							double zOff = (this.random.nextFloat() * 2.0F - 1.0F) * this.bbWidth;
							this.world.spawnParticle(
								"splash",
								this.x + xOff,
								f3 + 1.0F,
								this.z + zOff,
								this.xd,
								this.yd,
								this.zd,
								0
							);
						}
					}
				}
			}

			if (this.ticksCatchable > 0) {
				this.yd
					-= (double) (this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat()) * 0.2;
			}

			double d7 = d5 * (double) 2.0F - (double) 1.0F;
			this.yd += 0.04 * d7;
			if (d5 > (double) 0.0F) {
				movementScale = (float) ((double) movementScale * 0.9);
				this.yd *= 0.8;
			}

			this.xd *= movementScale;
			this.yd *= movementScale;
			this.zd *= movementScale;
			this.setPos(this.x, this.y, this.z);
		}
	}

	@Shadow
	public boolean isInGround() {
		return true;
	}

	@Shadow
	public void setInGround(boolean b) {}
}
