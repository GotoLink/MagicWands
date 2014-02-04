package magicwands;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public abstract class WandItem extends Item {
	static ArrayList<Block> m_ores = new ArrayList<Block>();
	static ArrayList<Block> ores = new ArrayList<Block>();
	public boolean reinforced;
	static Random rand = new Random();

	public WandItem(boolean reinforced) {
		super();
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
	 *            the block
	 * @return true if magic can be performed on the block
	 */
	public abstract boolean canAlter(int keys, Block blockAt);

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
	 *            the original current block
	 * @param id
	 *            the modified current block
	 * @param meta
	 *            the current block metadata
	 * @return true if something changed, thus damaging the wand
	 */
	public abstract boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, Block idOrig, Block id, int meta);

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
		Block id = world.func_147439_a(i, j, k);
		int meta = world.getBlockMetadata(i, j, k);
		Block idOrig = id;
		// general changes
		if (id == Blocks.grass) {
			id = Blocks.dirt;
		} // Grass->Dirt
		if (id == Blocks.unlit_redstone_torch) {
			id = Blocks.redstone_torch;
		} // RStorch off->on
		if (id == Blocks.unpowered_repeater) {
			id = Blocks.powered_repeater;
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
			world.playSoundEffect(i + 0.5F, j + 0.5F, k + 0.5F, id.field_149762_H.func_150496_b(), (id.field_149762_H.func_150497_c() + 1.0F) / 2.0F, id.field_149762_H.func_150494_d() * 0.8F);
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
			boolean damage = doMagic(entityplayer, world, Start, End, Info, clicked_current, keys, idOrig, id, meta);
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
	protected boolean canPlace(World world, int i, int j, int k, Block block, int keys) {
		if (canAlter(keys, world.func_147439_a(i, j, k))) {
			if (block.func_149742_c(world, i, j, k))
				return true;
			if ((block == Blocks.cactus || block == Blocks.reeds) || (block instanceof BlockFlower)) {
				return block.func_149742_c(world, i, j, k);
			}
			if (block == Blocks.redstone_wire || block == Blocks.stone_pressure_plate || block == Blocks.wooden_pressure_plate || block == Blocks.snow || block instanceof BlockTorch) {
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
			entityplayer.func_146105_b(new ChatComponentTranslation("error.wand." + reason));
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
	protected boolean isIncompatibleBlock(Block id) {
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
			if (l == 0 && !world.func_147439_a(i, j + 1, k).func_149662_c()) {
				d2 = j + 1 + d;
			}
			if (l == 1 && !world.func_147439_a(i, j - 1, k).func_149662_c()) {
				d2 = j + 0 - d;
			}
			if (l == 2 && !world.func_147439_a(i, j, k + 1).func_149662_c()) {
				d3 = k + 1 + d;
			}
			if (l == 3 && !world.func_147439_a(i, j, k - 1).func_149662_c()) {
				d3 = k + 0 - d;
			}
			if (l == 4 && !world.func_147439_a(i + 1, j, k).func_149662_c()) {
				d1 = i + 1 + d;
			}
			if (l == 5 && !world.func_147439_a(i - 1, j, k).func_149662_c()) {
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

	public static int getNeededCount(Block id, int meta) {
		if (id instanceof BlockSlab) {
			return 2;
		} else {
			return 1;
		}
	}

	public static ItemStack getNeededItem(Block id, int meta) {
		if (id == Blocks.leaves) {
			return new ItemStack(id, 1, meta & 3);
		} else if (id == Blocks.stone || id == Blocks.coal_ore || id == Blocks.clay || id == Blocks.deadbush || id == Blocks.bookshelf
				|| id == Blocks.fire || id instanceof BlockStairs || id == Blocks.farmland || id == Blocks.diamond_ore || id == Blocks.lapis_ore
				|| id instanceof BlockRedstoneOre || id == Blocks.glowstone || id == Blocks.ice || id == Blocks.snow || id == Blocks.stonebrick) {
			return new ItemStack(id, 1, meta);
		} else {
			return new ItemStack(id.func_149650_a(meta, rand, 0), 1, id.func_149692_a(meta));
		}
	}

	// test for cave filler
	public static boolean isSurface(Block blockAt) {
		return (blockAt == Blocks.dirt || blockAt == Blocks.grass || blockAt == Blocks.stone || blockAt == Blocks.gravel || blockAt == Blocks.sandstone
				|| blockAt == Blocks.sand || blockAt == Blocks.bedrock || blockAt == Blocks.coal_ore || blockAt == Blocks.iron_ore || blockAt == Blocks.gold_ore
				|| blockAt == Blocks.diamond_ore || blockAt == Blocks.lapis_ore);
	}

	protected static boolean emptyBuckets(Item bucketId, EntityPlayer entityplayer, int neededItems) {
		if (MagicWands.free || entityplayer.capabilities.isCreativeMode) {
			return true;
		}
		int itemsInInventory = 0;
		for (int t = 0; t < entityplayer.inventory.getSizeInventory(); t++) {
			ItemStack currentItem = entityplayer.inventory.getStackInSlot(t);
			if (currentItem != null && currentItem.getItem() == bucketId) // bucketWater
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
			if (currentItem != null && currentItem.getItem() == bucketId) // bucketWater
			{
				entityplayer.inventory.setInventorySlotContents(t, new ItemStack(Items.bucket));
				if (--neededItems == 0)
					return true;
			}
		}
		return false;
	}

	protected static boolean isMiningOre(Block id) {
		return m_ores.contains(id);
	}

	protected static boolean isOre(Block id) {
		return ores.contains(id);
	}
}
