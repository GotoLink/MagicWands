package magicwands;

import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class BuildWand extends WandItem {
	protected static final int BUILD_BOX = 100, BUILD_ROOM = 10, BUILD_FRAME = 1, BUILD_WATER = 110, BUILD_TORCHES = 101, BUILD_CAVES = 11, BUILD_LAVA = 111;

	public BuildWand(boolean reinforced) {
		super(reinforced);
		setMaxDamage(reinforced ? 200 : 30);// 20/200 uses
	}

	@Override
	public boolean canAlter(int keys, Block block) {
        if(block == Blocks.air || block == Blocks.leaves || block == Blocks.snow || block == Blocks.fire || block == Blocks.vine || block instanceof BlockCrops || block instanceof BlockFlower){
            return true;
        }
		switch (keys) {
		case BUILD_BOX:
		case BUILD_ROOM:
		case BUILD_FRAME:
        case BUILD_CAVES:
			return (block instanceof BlockLiquid);
		case BUILD_WATER:
		case BUILD_LAVA:
			return (block == Blocks.torch || block instanceof BlockLiquid);
		}
		return false;
	}

	@Override
	public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, Block idOrig, Block id, int meta) {
		if (id != info.id && (keys == BUILD_BOX || keys == BUILD_ROOM || keys == BUILD_FRAME || keys == BUILD_TORCHES)) {
			error(entityplayer, clicked_current, "notsamecorner");
			return false;
		}
		if (meta != info.meta && (keys == BUILD_BOX || keys == BUILD_ROOM || keys == BUILD_FRAME || keys == BUILD_TORCHES) && !(id == Blocks.cactus || id == Blocks.reeds)) {
			if ((id != Blocks.leaves) || ((meta & 3) != (info.meta & 3))) {
				error(entityplayer, clicked_current, "notsamecorner");
				return false;
			}
		}
		if (MagicWands.disableNotify)
			world.scheduledUpdatesAreImmediate = true;
		boolean flag = do_Building(world, start, end, clicked_current, keys, entityplayer, idOrig);
		if (flag && keys != BUILD_WATER)
			world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.pop", (world.rand.nextFloat() + 0.7F) / 2.0F,
					1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
		if (flag && keys == BUILD_WATER)
			world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "liquid.splash", (world.rand.nextFloat() + 0.7F) / 2.0F,
					1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
		if (MagicWands.disableNotify)
			world.scheduledUpdatesAreImmediate = false;
		return flag;
	}

	@Override
	public double[] getParticleColor() {
		return new double[] { 1.0D, 0.8D, 0.0D };
	}

	@Override
	public boolean isTooFar(int keys, int range, int max, int range2d) {
		switch (keys) {
		case BUILD_BOX:
		case BUILD_FRAME:
		case BUILD_ROOM:
		case BUILD_WATER:
		case BUILD_LAVA:
		case BUILD_TORCHES:
			return reinforced ? range - 400 > max : range - 50 > max;
		case BUILD_CAVES:
			return reinforced ? range2d - 1600 > max : range2d - 200 > max;
		}
		return true;
	}

	@Override
	protected boolean isIncompatibleBlock(Block block) {
		return block == Blocks.air || block == Blocks.piston_extension || block == Blocks.piston_head || block instanceof BlockBed || block instanceof BlockDoor || block instanceof BlockSign;
	}

	// === BUILDING ===
	private boolean do_Building(World world, WandCoord3D start, WandCoord3D end, WandCoord3D clicked, int keys, EntityPlayer entityplayer, Block idOrig) {
		// if((id==Block.cactus.blockID || id==Block.reed.blockID) && (keys ==
		// BUILD_BOX || keys == BUILD_ROOM || keys == BUILD_FRAME) && !FREE){
		// error(entityplayer, clicked,
		// (id==Block.cactus.blockID?"Cactus":"Reed")+" can be built only in Creative or FREE BUILD MODE."
		// );
		// return false;
		// }
		int X, Y = 0, Z;
        Block blockAt;
		Block id = clicked.id;
		int meta = clicked.meta;
		ItemStack neededStack = getNeededItem(id, meta);
		int multiplier = getNeededCount(id, meta);
		int neededItems;
		Item bucket;
		int affected = 0;
		boolean FREE = MagicWands.free || entityplayer.capabilities.isCreativeMode;
		switch (keys) {
		case BUILD_BOX:
			neededItems = 0;
			// count needed blocks
			for (X = start.x; X <= end.x; X++) {
				for (Z = start.z; Z <= end.z; Z++) {
					for (Y = start.y; Y <= end.y; Y++) {
						if (canPlace(world, X, Y, Z, id, keys)) {
							neededItems += multiplier;
						}
					}
				}
			}
			if (neededItems == 0) {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
				return false;
			}
			// consumeItems includes error message
			if (consumeItems(neededStack, entityplayer, neededItems, clicked)) {
				// do the building
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							if (canPlace(world, X, Y, Z, id, keys)) {
								world.setBlock(X, Y, Z, id, meta, 3);
								if (itemRand.nextInt(neededItems / 50 + 1) == 0)
									particles(world, X, Y, Z, 0);
								affected++;
							}
						}
					}
				}
				if (idOrig == Blocks.grass && affected > 0) {
					for (int run = 0; run <= 1; run++) {
						if (run == 0)
							Y = start.y;
						if (run == 1)
							Y = end.y;
						for (X = start.x; X <= end.x; X++) {
							for (Z = start.z; Z <= end.z; Z++) {
								if (world.getBlock(X, Y, Z) == Blocks.dirt
										&& (world.getBlock(X, Y + 1, Z) == Blocks.air || !world.getBlock(X, Y + 1, Z).renderAsNormalBlock())) {
									world.setBlock(X, Y, Z, Blocks.grass);
								}
							}
						}
					}
				}
				return affected > 0;
			} else {
				return false; // not enough items, aborting
			}
		case BUILD_ROOM:
			neededItems = 0;
			// count needed blocks
			for (X = start.x; X <= end.x; X++) {
				for (Z = start.z; Z <= end.z; Z++) {
					for (Y = start.y; Y <= end.y; Y++) {
						if (X == start.x || Y == start.y || Z == start.z || X == end.x || Y == end.y || Z == end.z) {
							if (canPlace(world, X, Y, Z, id, keys)) {
								neededItems += multiplier;
							}
						}
					}
				}
			}
			if (neededItems == 0) {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
				return false;
			}
			// consumeItems includes error message
			if (consumeItems(neededStack, entityplayer, neededItems, clicked)) {
				// do the building
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							if (X == start.x || Y == start.y || Z == start.z || X == end.x || Y == end.y || Z == end.z) {
								if (canPlace(world, X, Y, Z, id, keys)) {
									world.setBlock(X, Y, Z, id, meta, 3);
									if (itemRand.nextInt(neededItems / 50 + 1) == 0)
										particles(world, X, Y, Z, 0);
									affected++;
								}
							}
						}
					}
				}
				if (idOrig == Blocks.grass && affected > 0) {
					for (int run = 0; run <= 1; run++) {
						if (run == 0)
							Y = start.y;
						if (run == 1)
							Y = end.y;
						for (X = start.x; X <= end.x; X++) {
							for (Z = start.z; Z <= end.z; Z++) {
								if (world.getBlock(X, Y, Z) == Blocks.dirt
										&& (world.getBlock(X, Y + 1, Z) == Blocks.air || !world.getBlock(X, Y + 1, Z).renderAsNormalBlock())) {
									world.setBlock(X, Y, Z, Blocks.grass);
								}
							}
						}
					}
				}
				return affected > 0;
			} else {
				return false; // not enough items, aborting
			}
		case BUILD_FRAME:
			neededItems = 0;
			// count needed blocks
			for (X = start.x; X <= end.x; X++) {
				for (Z = start.z; Z <= end.z; Z++) {
					for (Y = start.y; Y <= end.y; Y++) {
						if ((X == start.x && Y == start.y) || (Y == start.y && Z == start.z) || (Z == start.z && X == start.x) || (X == start.x && Y == end.y) || (X == end.x && Y == start.y)
								|| (Y == start.y && Z == end.z) || (Y == end.y && Z == start.z) || (Z == start.z && X == end.x) || (Z == end.z && X == start.x) || (X == end.x && Y == end.y)
								|| (Y == end.y && Z == end.z) || (Z == end.z && X == end.x)) {
							if (canPlace(world, X, Y, Z, id, keys)) {
								neededItems += multiplier;
							}
						}
					}
				}
			}
			if (neededItems == 0) {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
				return false;
			}
			// consumeItems includes error message
			if (consumeItems(neededStack, entityplayer, neededItems, clicked)) {
				// do the building
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							if ((X == start.x && Y == start.y) || (Y == start.y && Z == start.z) || (Z == start.z && X == start.x) || (X == start.x && Y == end.y) || (X == end.x && Y == start.y)
									|| (Y == start.y && Z == end.z) || (Y == end.y && Z == start.z) || (Z == start.z && X == end.x) || (Z == end.z && X == start.x) || (X == end.x && Y == end.y)
									|| (Y == end.y && Z == end.z) || (Z == end.z && X == end.x)) {
								if (canPlace(world, X, Y, Z, id, keys)) {
									world.setBlock(X, Y, Z, id, meta, 3);
									if (itemRand.nextInt(neededItems / 50 + 1) == 0)
										particles(world, X, Y, Z, 0);
									affected++;
								}
							}
						}
					}
				}
				if (idOrig == Blocks.grass && affected > 0) {
					for (int run = 0; run <= 1; run++) {
						if (run == 0)
							Y = start.y;
						if (run == 1)
							Y = end.y;
						for (X = start.x; X <= end.x; X++) {
							for (Z = start.z; Z <= end.z; Z++) {
								if ((X == start.x && Y == start.y) || (Y == start.y && Z == start.z) || (Z == start.z && X == start.x) || (X == start.x && Y == end.y) || (X == end.x && Y == start.y)
										|| (Y == start.y && Z == end.z) || (Y == end.y && Z == start.z) || (Z == start.z && X == end.x) || (Z == end.z && X == start.x) || (X == end.x && Y == end.y)
										|| (Y == end.y && Z == end.z) || (Z == end.z && X == end.x)) {
									if (world.getBlock(X, Y, Z) == Blocks.dirt
											&& (world.getBlock(X, Y + 1, Z) == Blocks.air || !world.getBlock(X, Y + 1, Z).renderAsNormalBlock())) {
										world.setBlock(X, Y, Z, Blocks.grass);
									}
								}
							}
						}
					}
				}
				return affected > 0;
			} else {
				return false; // not enough items, aborting
			}
		case BUILD_TORCHES:
			// count items in inventory
			neededItems = 0;
			// count needed blocks
			for (X = start.x; X <= end.x; X += 5) {
				for (Z = start.z; Z <= end.z; Z += 5) {
					for (Y = start.y; Y <= end.y; Y++) {
						if (canPlace(world, X, Y, Z, id, keys)) {
							neededItems += multiplier;
						}
					}
				}
			}
			if (neededItems == 0) {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
				return false;
			}
			// consumeItems includes error message
			if (consumeItems(neededStack, entityplayer, neededItems, clicked)) {
				// do the building
				for (X = start.x; X <= end.x; X += 5) {
					for (Z = start.z; Z <= end.z; Z += 5) {
						for (Y = start.y; Y <= end.y; Y++) {
							if (canPlace(world, X, Y, Z, id, keys)) {
								world.setBlock(X, Y, Z, id, meta, 3);
								particles(world, X, Y, Z, 0);
								affected++;
							}
						}
					}
				}
				return affected > 0;
			} else {
				return false; // not enough items, aborting
			}
		case BUILD_WATER:
			if (!(reinforced || FREE)) {
				error(entityplayer, clicked, "cantfillwater");
				return false;
			}
			bucket = Items.water_bucket;
			if (!FREE) {
				// count items in inventory
				neededItems = 0;
				// count needed blocks
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (canAlter(keys, blockAt)) {
								neededItems++;
							}
						}
					}
				}
				if (neededItems == 0) {
					if (!world.isRemote)
						entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
					return false;
				}
			}
			if (emptyBuckets(bucket, entityplayer, 2)) {
				// do the building
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (canAlter(keys, blockAt)) {
								world.setBlock(X, Y, Z, Blocks.flowing_water);
								affected++;
							}
						}
					}
				}
				if (affected == 0)
					return false;
				// notification
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (blockAt == Blocks.flowing_water) {
								world.notifyBlockChange(X, Y, Z, Blocks.flowing_water);
								if (world.isAirBlock(X, Y + 1, Z))
									particles(world, X, Y, Z, 2);
							}
						}
					}
				}
				return true;
			} else {
				error(entityplayer, clicked, "toofewwater");
			}
			return false; // not enough items, aborting
		case BUILD_LAVA:
			if (!(reinforced || FREE)) {
				error(entityplayer, clicked, "cantfilllava");
				return false;
			}
			bucket = Items.lava_bucket;
			// count Items in inventory
			neededItems = 0;
			if (!FREE) {
				// count needed blocks
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (canAlter(keys, blockAt))
								neededItems++;
						}
					}
				}
				if (neededItems == 0) {
					if (!world.isRemote)
						entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
					return false;
				}
			}
			if (emptyBuckets(bucket, entityplayer, neededItems)) {
				// do the building
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (canAlter(keys, blockAt)) {
								world.setBlock(X, Y, Z, Blocks.flowing_lava);
								affected++;
							}
						}
					}
				}
				if (affected == 0)
					return false;
				// notification
				for (X = start.x; X <= end.x; X++) {
					for (Z = start.z; Z <= end.z; Z++) {
						for (Y = start.y; Y <= end.y; Y++) {
							blockAt = world.getBlock(X, Y, Z);
							if (blockAt == Blocks.flowing_lava) {
								world.notifyBlockChange(X, Y, Z, Blocks.flowing_lava);
							}
						}
					}
				}
				return true;
			} else {
				error(entityplayer, clicked, "toofewlava");
			}
			return false;
		case BUILD_CAVES:
			if (!(reinforced || FREE)) {
				error(entityplayer, clicked, "cantfillcave");
				return false;
			}
			boolean underground;
			long cnt = 0;
			for (X = start.x; X <= end.x; X++) {
				for (Z = start.z; Z <= end.z; Z++) {
					underground = false;
					for (Y = 127; Y > 1; Y--) {
						blockAt = world.getBlock(X, Y, Z);
						boolean surfaceBlock = isSurface(blockAt);
						if (!underground && surfaceBlock) {
							underground = true;
							continue;
						}
						if (!underground) {
							continue;
						}
						if (canAlter(keys, blockAt)) {
							world.setBlock(X, Y, Z, Blocks.stone);
							cnt++;
						}
					}
				}
			}
			if (cnt > 0) {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentText(cnt + StatCollector.translateToLocal("result.wand.fill")));
				return true;
			} else {
				if (!world.isRemote)
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nocave"));
				return false;
			}
		} // end of the long switch
		return false;
	}
}
