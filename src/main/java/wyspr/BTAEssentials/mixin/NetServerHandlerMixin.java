//package wyspr.mixin;
//
//import com.llamalad7.mixinextras.sugar.Local;
//import wyspr.BTAEssentials;
//import wyspr.utils.PlayerData;
//import net.minecraft.core.block.Block;
//import net.minecraft.core.block.entity.TileEntity;
//import net.minecraft.core.block.entity.TileEntitySign;
//import net.minecraft.core.entity.Entity;
//import net.minecraft.core.entity.player.EntityPlayer;
//import net.minecraft.core.enums.EnumSignPicture;
//import net.minecraft.core.item.Item;
//import net.minecraft.core.item.ItemStack;
//import net.minecraft.core.net.ICommandListener;
//import net.minecraft.core.net.command.TextFormatting;
//import net.minecraft.core.net.handler.NetHandler;
//import net.minecraft.core.net.packet.*;
//import net.minecraft.core.player.inventory.slot.Slot;
//import net.minecraft.core.util.helper.ChatAllowedCharacters;
//import net.minecraft.core.util.helper.Direction;
//import net.minecraft.core.util.helper.MathHelper;
//import net.minecraft.core.world.chunk.ChunkCoordinates;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.entity.player.EntityPlayerMP;
//import net.minecraft.server.net.PlayerList;
//import net.minecraft.server.net.handler.NetServerHandler;
//import net.minecraft.server.world.WorldServer;
//import org.apache.log4j.Logger;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.Redirect;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.*;
//
//@Mixin(value = NetServerHandler.class, remap = false)
//public class NetServerHandlerMixin extends NetHandler implements ICommandListener {
//    @Shadow
//	public static Logger logger = Logger.getLogger("Minecraft");
//	@Shadow
//	private MinecraftServer mcServer;
//	@Shadow
//	private EntityPlayerMP playerEntity;
//	@Shadow
//	private double lastPosX;
//	@Shadow
//	private double lastPosY;
//	@Shadow
//	private double lastPosZ;
//	@Shadow
//	private boolean hasMoved = true;
//	@Shadow
//	public void log(String string) {}
//	@Shadow
//	public void sendPacket(Packet packet) {}
//	@Shadow
//	private void handleSlashCommand(String s) {}
//	@Shadow
//	public String getUsername() {
//		return null;
//	}
//	@Shadow
//	public boolean isServerHandler() {
//		return true;
//	}
//
//	@Inject(
//		method = "handleUseEntity",
//		at = @At(value = "INVOKE",
//			target = "Lnet/minecraft/server/entity/player/EntityPlayerMP;useCurrentItemOnEntity(Lnet/minecraft/core/entity/Entity;)V",
//			shift = At.Shift.BEFORE
//		)
//	)
//	public void ridePlayer(Packet7UseEntity packet, CallbackInfo ci, @Local(ordinal = 0) Entity targetEntity) {
//		if (targetEntity instanceof EntityPlayer && this.playerEntity.getHeldItem() == null && targetEntity.vehicle != this.playerEntity && BTAEssentials.headSit) {
//			this.playerEntity.startRiding(targetEntity);
//			this.playerEntity.collision = false;
//			this.playerEntity.noPhysics = true;
//		}
//	}
//
//	@Inject(
//		method = "handleEntityAction",
//		at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/server/entity/player/EntityPlayerMP;setSneaking(Z)V",
//			ordinal = 0,
//			shift = At.Shift.BEFORE
//		)
//	)
//	public void handlePlayerRideEject(Packet19EntityAction packet, CallbackInfo ci) {
//		if (this.playerEntity.vehicle instanceof EntityPlayer) {
//			this.playerEntity.noPhysics = false;
//			this.playerEntity.collision = true;
//		}
//	}
//
//	@Redirect(
//		method = "handleFlying",
//		at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/server/net/handler/NetServerHandler; kickPlayer(Ljava/lang/String;) V"
//		)
//	)
//	private void redirectFlyKick(NetServerHandler self, String msg) {
//		if ("You moved too quickly :( (Hacking?)".equals(msg)) {
//			self.teleportAndRotate(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.yRot, this.playerEntity.xRot);
//		} else {
//			self.kickPlayer(msg);
//		}
//	}
//
//	@Redirect(
//		method = "handleChat",
//		at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/server/net/handler/NetServerHandler; handleSlashCommand(Ljava/lang/String;) V"
//		)
//	)
//	public void allowColoredCommands(NetServerHandler self, String s) {
//		if (BTAEssentials.colourChat) {
//			s = s.replace("$$", "§");
//		}
//		if (!this.playerEntity.isOperator() && s.contains("§k")) {
//			s = s.replace("§k", "$$k");
//			this.mcServer.playerList.sendChatMessageToPlayer(this.playerEntity.username, "§e§lHey!§r You may not use obfuscated text!");
//		}
//		this.handleSlashCommand(s);
//	}
//
//
//	@Overwrite
//	public void handleUpdateSign(Packet130UpdateSign packet) {
//		WorldServer worldserver = this.mcServer.getDimensionWorld(this.playerEntity.dimension);
//		if (worldserver.isBlockLoaded(packet.xPosition, packet.yPosition, packet.zPosition)) {
//			TileEntity tileentity = worldserver.getBlockTileEntity(packet.xPosition, packet.yPosition, packet.zPosition);
//
//			int l;
//			int i;
//			for(i = 0; i < 4; ++i) {
//				boolean isLineValid = true;
//				if (packet.signLines[i].length() > 15) {
//					isLineValid = false;
//				} else {
//					packet.signLines[i] = packet.signLines[i].replaceAll("§", "$");
//
//					for(l = 0; l < packet.signLines[i].length(); ++l) {
//						if (ChatAllowedCharacters.ALLOWED_CHARACTERS.indexOf(packet.signLines[i].charAt(l)) < 0) {
//							isLineValid = false;
//							break;
//						}
//					}
//				}
//
//				if (!isLineValid) {
//					packet.signLines[i] = "!?";
//				}
//			}
//
//			if (tileentity instanceof TileEntitySign) {
//				i = packet.xPosition;
//				int y = packet.yPosition;
//				l = packet.zPosition;
//				TileEntitySign tileEntity = (TileEntitySign)tileentity;
//				System.out.println("Sign placed by "+ this.playerEntity.username+" at "+ i + ", " + y + ", " + l);
//				System.out.println("Contains text: " + Arrays.toString(packet.signLines));
//				for(int j1 = 0; j1 < 4; ++j1) {
//					tileEntity.signText[j1] = packet.signLines[j1];
//				}
//
//				tileEntity.setColor(TextFormatting.FORMATTINGS[packet.color]);
//				tileEntity.setPicture(EnumSignPicture.values()[packet.picture]);
//				tileEntity.onInventoryChanged();
//				worldserver.markBlockNeedsUpdate(i, y, l);
//			}
//		}
//
//	}
//	@Overwrite
//	public void handlePlace(Packet15Place packet) {
//		WorldServer worldserver = this.mcServer.getDimensionWorld(this.playerEntity.dimension);
//		ItemStack itemstack = this.playerEntity.inventory.getCurrentItem();
//
//
//		if (this.playerEntity.getHealth() <= 0) {
//			System.out.println(this.playerEntity.username + " tried to place a block while dead");
//			this.mcServer.playerList.sendChatMessageToPlayer(this.playerEntity.username,"§e§lHey!§r You cannot do that. §5Respawn§r.");
//			return;
//		}
//
//
//		boolean flag = worldserver.field_819_z = worldserver.dimension.id != 0 || this.mcServer.playerList.isOp(this.playerEntity.username);
//		if (packet.direction == Direction.NONE) {
//			if (itemstack == null) {
//				return;
//			}
//
//			this.playerEntity.playerController.func_6154_a(this.playerEntity, worldserver, itemstack);
//		} else {
//			int x = packet.xPosition;
//			int y = packet.yPosition;
//			int z = packet.zPosition;
//			Direction direction = packet.direction;
//			double xPlaced = packet.xPlaced;
//			double yPlaced = packet.yPlaced;
//			ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();
//			int i1 = (int)MathHelper.abs((float)(x - chunkcoordinates.x));
//			int j1 = (int)MathHelper.abs((float)(z - chunkcoordinates.z));
//			if (i1 > j1) {
//				j1 = i1;
//			}
//
//			if (this.hasMoved && this.playerEntity.distanceToSqr((double)x + 0.5, (double)y + 0.5, (double)z + 0.5) < 64.0 && (j1 > this.mcServer.spawnProtectionRange || flag)) {
//
//
//				this.playerEntity.playerController.activateBlockOrUseItem(this.playerEntity, worldserver, itemstack, x, y, z, direction.getSide(), xPlaced, yPlaced);
//			}
//			if (worldserver.isBlockLoaded(packet.xPosition, packet.yPosition, packet.zPosition) && itemstack == Item.sign.getDefaultStack()) {
//				TileEntity tileentity = worldserver.getBlockTileEntity(packet.xPosition, packet.yPosition, packet.zPosition);
//				if (tileentity instanceof TileEntitySign) {
//					TileEntitySign tileentitysign = (TileEntitySign) tileentity;
//					worldserver.markBlockNeedsUpdate(packet.xPosition, packet.yPosition, packet.zPosition);
//					return;
//				}
//			}
//			this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, worldserver));
//			x += direction.getOffsetX();
//			y += direction.getOffsetY();
//			z += direction.getOffsetZ();
//			this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, worldserver));
//		}
//
//		itemstack = this.playerEntity.inventory.getCurrentItem();
//		if (itemstack != null && itemstack.stackSize <= 0) {
//			this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
//		}
//
//		this.playerEntity.isChangingQuantityOnly = true;
//		this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack.copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
//		Slot slot = this.playerEntity.craftingInventory.func_20127_a(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
//		this.playerEntity.craftingInventory.updateInventory();
//		this.playerEntity.isChangingQuantityOnly = false;
//		if (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), packet.itemStack)) {
//			this.sendPacket(new Packet103SetSlot(this.playerEntity.craftingInventory.windowId, slot.id, this.playerEntity.inventory.getCurrentItem()));
//		}
//
//		worldserver.field_819_z = false;
//	}
//	@Overwrite
//	public void handleRespawn(Packet9Respawn packet) {
//		if (this.playerEntity.getHealth() <= 0) {
//			int score = this.playerEntity.score;
//			this.playerEntity = this.mcServer.playerList.recreatePlayerEntity(this.playerEntity, 0);
//            this.playerEntity.score = (int) ((double) score * BTAEssentials.deathCost);
//			PlayerData.update(this.playerEntity);
//		}
//	}
//
//	@Overwrite
//	public void handleBlockDig(Packet14BlockDig packet) {
//        int x = packet.xPosition;
//        int y = packet.yPosition;
//        int z = packet.zPosition;
//        if (this.playerEntity.getHealth() <= 0) {
//            System.out.println(this.playerEntity.username + " tried to break a block while dead");
//            this.mcServer.playerList.sendChatMessageToPlayer(this.playerEntity.username, "§e§lHey!§r You cannot do that. §5Respawn§r.");
//            return;
//        }
//
//		// Copied
//        WorldServer world = this.mcServer.getDimensionWorld(this.playerEntity.dimension);
//        if (packet.status == 4) {
//            this.playerEntity.dropCurrentItem(false);
//        } else if (packet.status == 5) {
//            this.playerEntity.dropCurrentItem(true);
//        } else if (packet.status == 6) {
//            this.playerEntity.pickBlock(x, y, z);
//        } else {
//            double playerDistX = this.playerEntity.x - ((double) x + 0.5);
//            double playerDistY = this.playerEntity.y - ((double) y + 0.5);
//            double playerDistZ = this.playerEntity.z - ((double) z + 0.5);
//            double playerDist = playerDistX * playerDistX + playerDistY * playerDistY + playerDistZ * playerDistZ;
//            if (!(playerDist > 44.0)) {
//                boolean ignoreSpawnProtection = world.field_819_z = this.mcServer.spawnProtectionRange <= 0 || world.dimension.id != 0 || this.mcServer.playerList.isOp(this.playerEntity.username);
//                ChunkCoordinates spawnPos = world.getSpawnPoint();
//                int spawnDistX = (int) MathHelper.abs((float) (x - spawnPos.x));
//                int spawnDistZ = (int) MathHelper.abs((float) (z - spawnPos.z));
//                int distanceFromSpawn = Math.max(spawnDistX, spawnDistZ);
//                if (distanceFromSpawn <= this.mcServer.spawnProtectionRange && !ignoreSpawnProtection) {
//                    this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, world));
//                    world.field_819_z = false;
//                } else {
//                    if (packet.status == 0) {
//                        if (!((world.getBlock(x, y, z) == Block.signPostPlanksOak || world.getBlock(x, y, z) == Block.signWallPlanksOak) && this.playerEntity.isSneaking())) {
//                            this.playerEntity.playerController.startMining(x, y, z, packet.side);
//                        }
//                    } else if (packet.status == 1) {
//                        if (!((world.getBlock(x, y, z) == Block.signPostPlanksOak || world.getBlock(x, y, z) == Block.signWallPlanksOak) && this.playerEntity.isSneaking())) {
//                            this.playerEntity.playerController.hitBlock(x, y, z, packet.side);
//                        }
//                    } else if (packet.status == 2 && !this.playerEntity.playerController.destroyBlock(x, y, z))
//						if (world.getBlock(x, y, z) == Block.chestLegacy || world.getBlock(x, y, z) == Block.chestPlanksOakPainted || world.getBlock(x, y, z) == Block.chestPlanksOak || world.getBlock(x, y, z) == Block.chestLegacyPainted) {
//							System.out.println(this.playerEntity.username + " broke chest at " + x + ", " + y + ", " + z);
//							logger.info(this.playerEntity.username + " broke chest at " + x + ", " + y + ", " + z);
//						}
//                        if (!((world.getBlock(x, y, z) == Block.signPostPlanksOak || world.getBlock(x, y, z) == Block.signWallPlanksOak) && this.playerEntity.isSneaking())) {
//                            this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, world));
//                        } else {
//                            this.mcServer.playerList.sendChatMessageToPlayer(this.playerEntity.username, "Replace the sign to rewrite some lines, or don't.");
//                        }
//                    }
//
//                    world.field_819_z = false;
//                }
//            }
//        }
//}
