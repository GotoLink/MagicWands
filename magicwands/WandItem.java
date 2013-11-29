package magicwands;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockHalfSlab;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public abstract class WandItem extends Item {
	static ArrayList<Integer> m_ores = new ArrayList<Integer>();
	static ArrayList<Integer> ores = new ArrayList<Integer>();
	public boolean reinforced;
	static Random rand = new Random();

	public WandItem(int i, boolean reinforced) {
		super(i);
		setCreativeTab(MagicWands.wands);
		setMaxStackSize(1);
		this.reinforced = reinforced;
		setMaxDamage(reinforced ? 200 : 30);// 20/200 uses
	}

	/**
	 * If block can be changed by the wand magic
	 * 
	 * @param keys
	 *            type of magic, as a key combination
	 * @param blockAt
	 *            the block id
	 * @return true if magic can be performed on the block id
	 */
	public abstract boolean canAlter(int keys, int blockAt);

	/**
	 * Perform the wand magic
	 * 
	 * @param entityplayer
	 *            player using the wand
	 * @param world
	 *            world inside which the player is
	 * @param start
	 *            calculated start block coordinate
	 * @param end
	 *            calculated end block coordinate
	 * @param info
	 * @param clicked_current
	 *            the current block
	 * @param keys
	 *            type of magic, as key combination
	 * @param idOrig
	 *            the original current block id
	 * @param id
	 *            the modified current block id
	 * @param meta
	 *            the current block metadata
	 * @return true if something changed, thus damaging the wand
	 */
	public abstract boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, int idOrig, int id, int meta);

	/**
	 * Used by {@link #particles(World, WandCoord3D, int)} when effect type is 0
	 * 
	 * @return array of R,G,B
	 */
	public abstract double[] getParticleColor();

	/**
	 * Check range of block against wand range of effect
	 * 
	 * @param keys
	 *            type of magic, as key combination
	 * @param range
	 *            volume distance to check against
	 * @param max
	 *            maximum variation to introduce in the result
	 * @param range2D
	 *            horizontal distance to check against
	 * @return
	 */
	public abstract boolean isTooFar(int keys, int range, int max, int range2D);

	// range test
	public boolean isTooFar(WandCoord3D a, WandCoord3D b, int keys, boolean free) {
		if (free) {
			return a.getDistance(b) > 1500;
		}
		return isTooFar(keys, (int) a.getDistance(b), 10, (int) a.getDistanceFlat(b));
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l, float par8, float par9, float par10) {
		int id = world.getBlockId(i, j, k);
		int meta = world.getBlockMetadata(i, j, k);
		int idOrig = id;
		// general changes
		if (id == Block.grass.blockID) {
			id = Block.dirt.blockID;
		} // Grass->Dirt
		if (id == Block.torchRedstoneIdle.blockID) {
			id = Block.torchRedstoneActive.blockID;
		} // RStorch off->on
		if (id == Block.redstoneRepeaterIdle.blockID) {
			id = Block.redstoneRepeaterActive.blockID;
		} // repeater off->on
		WandCoord3D clicked_current = new WandCoord3D(i, j, k, id, meta);
		// invalid blocks for building
		if (isIncompatibleBlock(id)) {
			error(entityplayer, clicked_current, "cantbuild");
			return true;
		}
		if (!itemstack.hasTagCompound()) {
			itemstack.stackTagCompound = new NBTTagCompound();
		}
		int keys = itemstack.stackTagCompound.getInteger("Keys");
		if (keys == 0) {
			// MARKING START BLOCK
			Block tmpblock = Block.blocksList[id];
			world.playSoundEffect(i + 0.5F, j + 0.5F, k + 0.5F, tmpblock.stepSound.getBreakSound(), (tmpblock.stepSound.getVolume() + 1.0F) / 2.0F, tmpblock.stepSound.getPitch() * 0.8F);
			// saving current block info...
			clicked_current.writeToNBT(itemstack.stackTagCompound, "Start");
			// coloured particles
			particles(world, clicked_current, 0);
			// Next use can be target-selection
			itemstack.stackTagCompound.setBoolean("Started", true);
			return true;
		} else {
			clicked_current.writeToNBT(itemstack.stackTagCompound, "End");
			// Some keys pressed - END BLOCK
			if (!itemstack.stackTagCompound.getBoolean("Started")) {
				error(entityplayer, clicked_current, "nostart");
				return true;
			}
			// find the smaller coords
			WandCoord3D Info = WandCoord3D.getFromNBT(itemstack.stackTagCompound, "Start");
			WandCoord3D Start = Info.copy();
			WandCoord3D End = clicked_current.copy();
			WandCoord3D.findEnds(Start, End);
			if (isTooFar(Start, End, keys, MagicWands.free || entityplayer.capabilities.isCreativeMode)) {
				error(entityplayer, clicked_current, "toofar");
				return true;
			}
			boolean damage = false;
			damage = doMagic(entityplayer, world, Start, End, Info, clicked_current, keys, idOrig, id, meta);
			if (damage) {
				itemstack.stackTagCompound.setBoolean("Started", false);
				if (!(MagicWands.free || entityplayer.capabilities.isCreativeMode)) {
					itemstack.damageItem(1, entityplayer);
					return true;
				}
			}
		}
		return true;
	}

	// CLICKED
	protected boolean canPlace(World world, int i, int j, int k, int id, int keys) {
		Block block = Block.blocksList[id];
		if (canAlter(keys, world.getBlockId(i, j, k))) {
			if (block.canPlaceBlockAt(world, i, j, k))
				return true;
			if ((id == Block.cactus.blockID || id == Block.reed.blockID) || (block instanceof BlockFlower)) {
				return block.canPlaceBlockAt(world, i, j, k);
			}
			if (id == Block.redstoneWire.blockID || id == Block.pressurePlateStone.blockID || id == Block.pressurePlatePlanks.blockID || id == Block.snow.blockID || block instanceof BlockTorch) {
				return false;
			}
			return true;
		}
		return false;
	}

	protected boolean consumeItems(ItemStack neededStack, EntityPlayer entityplayer, int neededItems, WandCoord3D clicked) {
		if (MagicWands.free || entityplayer.capabilities.isCreativeMode) {
			return true;
		}
		int invItems = 0;
		// count items in inv.
		for (int t = 0; t < entityplayer.inventory.getSizeInventory(); t++) {
			ItemStack currentItem = entityplayer.inventory.getStackInSlot(t);
			if (currentItem != null && currentItem.isItemEqual(neededStack)) {
				invItems += currentItem.stackSize;
				if (invItems == neededItems)
					break; // enough, no need to continue counting.
			}
		}
		if (neededItems > invItems) {
			error(entityplayer, clicked, "toofewitems (needed " + neededItems + ", you have " + invItems + ").");
			return false; // abort
		}
		// remove blocks from inventory, highest positions first (quickbar last)
		for (int t = entityplayer.inventory.getSizeInventory() - 1; t >= 0; t--) {
			ItemStack currentItem = entityplayer.inventory.getStackInSlot(t);
			if (currentItem != null && currentItem.isItemEqual(neededStack)) {
				int stackSize = currentItem.stackSize;
				if (stackSize < neededItems) {
					entityplayer.inventory.setInventorySlotContents(t, null);
					neededItems -= stackSize;
				} else if (stackSize >= neededItems) {
					entityplayer.inventory.decrStackSize(t, neededItems);
					neededItems = 0;
					break;
				}
			}
		}
		return true;
	}

	/**
	 * Perform error warning in various form: sound effect, error message in
	 * chat, and particles
	 * 
	 * @param entityplayer
	 *            player to warn
	 * @param pos
	 *            position of the error
	 * @param reason
	 *            message given to the player
	 */
	protected void error(EntityPlayer entityplayer, WandCoord3D pos, String reason) {
		entityplayer.worldObj.playSoundEffect(pos.x, pos.y, pos.z, "damage.fallsmall", (entityplayer.worldObj.rand.nextFloat() + 0.7F) / 2.0F, 0.5F + entityplayer.worldObj.rand.nextFloat() * 0.3F);
		if (!entityplayer.worldObj.isRemote)
			entityplayer.addChatMessage(StatCollector.translateToLocal("error.wand." + reason));
		particles(entityplayer.worldObj, pos.x, pos.y, pos.z, 3);
		return;
	}

	/**
	 * First check for block compatibility with the wand power. Overridden in
	 * {@link BuildWand}
	 * 
	 * @param id
	 *            the block id to check
	 * @return true if the wand can't interact with this block
	 */
	protected boolean isIncompatibleBlock(int id) {
		return false;
	}

	/**
	 * Spawn particles near coordinates, depending on effect type
	 * 
	 * @param effect
	 *            0 - wand, 1 - smoke, 2 - splash, other - reddust
	 */
	protected void particles(World world, int i, int j, int k, int effect) {
		double d = 0.0625D;
		if (effect == 1) {
			world.spawnParticle("smoke", i + 0.5D, j + 0.5D, k + 0.5D, 0.0D, 0.0D, 0.0D);
			return;
		} else if (effect == 2) {
			world.spawnParticle("splash", i + 0.5D, j + 1D, k + 0.5D, 0.0D, 0.0D, 0.0D);
			return;
		}
		double R = 0.0, G = 0.0, B = 0.0;
		/* select color */
		if (effect == 0) {
			double[] color = getParticleColor();
			R = color[0];
			G = color[1];
			B = color[2];
		} else {
			R = 0.8;
		}
		for (int l = 0; l < 6; l++) {
			double d1 = i + rand.nextFloat();
			double d2 = j + rand.nextFloat();
			double d3 = k + rand.nextFloat();
			if (l == 0 && !world.isBlockOpaqueCube(i, j + 1, k)) {
				d2 = j + 1 + d;
			}
			if (l == 1 && !world.isBlockOpaqueCube(i, j - 1, k)) {
				d2 = j + 0 - d;
			}
			if (l == 2 && !world.isBlockOpaqueCube(i, j, k + 1)) {
				d3 = k + 1 + d;
			}
			if (l == 3 && !world.isBlockOpaqueCube(i, j, k - 1)) {
				d3 = k + 0 - d;
			}
			if (l == 4 && !world.isBlockOpaqueCube(i + 1, j, k)) {
				d1 = i + 1 + d;
			}
			if (l == 5 && !world.isBlockOpaqueCube(i - 1, j, k)) {
				d1 = i + 0 - d;
			}
			if (d1 < i || d1 > i + 1 || d2 < 0.0D || d2 > j + 1 || d3 < k || d3 > k + 1) {
				world.spawnParticle("reddust", d1, d2, d3, R, G, B);
			}
		}
	}

	/**
	 * @param effect
	 *            0 -wand, 1 - smoke, 2 - splash, 3-error
	 */
	protected void particles(World world, WandCoord3D c, int effect) {
		particles(world, c.x, c.y, c.z, effect);
	}

	public static int getNeededCount(int id, int meta) {
		if (Block.blocksList[id] instanceof BlockHalfSlab) {
			return 2;
		} else {
			return 1;
		}
	}

	public static ItemStack getNeededItem(int id, int meta) {
		if (id == Block.leaves.blockID) {
			return new ItemStack(id, 1, meta & 3);
		} else if (id == Block.stone.blockID || id == Block.oreCoal.blockID || id == Block.blockClay.blockID || id == Block.deadBush.blockID || id == Block.bookShelf.blockID
				|| id == Block.fire.blockID || Block.blocksList[id] instanceof BlockStairs || id == Block.tilledField.blockID || id == Block.oreDiamond.blockID || id == Block.oreLapis.blockID
				|| Block.blocksList[id] instanceof BlockRedstoneOre || id == Block.glowStone.blockID || id == Block.ice.blockID || id == Block.blockSnow.blockID || id == Block.stoneBrick.blockID) {
			return new ItemStack(id, 1, meta);
		} else {
			return new ItemStack(Block.blocksList[id].idDropped(id, rand, meta), 1, Block.blocksList[id].damageDropped(meta));
		}
	}

	// test for cave filler
	public static boolean isSurface(int blockAt) {
		return (blockAt == Block.dirt.blockID || blockAt == Block.grass.blockID || blockAt == Block.stone.blockID || blockAt == Block.gravel.blockID || blockAt == Block.sandStone.blockID
				|| blockAt == Block.sand.blockID || blockAt == Block.bedrock.blockID || blockAt == Block.oreCoal.blockID || blockAt == Block.oreIron.blockID || blockAt == Block.oreGold.blockID
				|| blockAt == Block.oreDiamond.blockID || blockAt == Block.oreLapis.blockID);
	}

	protected static boolean emptyBuckets(int bucketId, EntityPlayer entityplayer, int neededItems) {
		if (MagicWands.free || entityplayer.capabilities.isCreativeMode) {
			return true;
		}
		int itemsInInventory = 0;
		for (int t = 0; t < entityplayer.inventory.getSizeInventory(); t++) {
			ItemStack currentItem = entityplayer.inventory.getStackInSlot(t);
			if (currentItem != null && currentItem.itemID == bucketId) // bucketWater
			{
				itemsInInventory++;
			}
		}
		// ? error - not enough items!
		if (itemsInInventory < neededItems) {
			return false;
		}
		// remove blocks from inventory, highest positions first (quickbar last)
		for (int t = entityplayer.inventory.getSizeInventory() - 1; t >= 0; t--) {
			ItemStack currentItem = entityplayer.inventory.getStackInSlot(t);
			if (currentItem != null && currentItem.itemID == bucketId) // bucketWater
			{
				entityplayer.inventory.setInventorySlotContents(t, new ItemStack(Item.bucketEmpty));
				if (--neededItems == 0)
					return true;
			}
		}
		return false;
	}

	protected static boolean isMiningOre(int id) {
		return m_ores.contains(id);
	}

	protected static boolean isOre(int id) {
		return ores.contains(id);
	}
}
