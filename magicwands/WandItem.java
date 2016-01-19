package magicwands;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.ArrayList;

public abstract class WandItem extends Item {
    private static ArrayList<Block> m_ores = new ArrayList<Block>();
    private static ArrayList<Block> ores = new ArrayList<Block>();
    public final boolean reinforced;

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
     * @param keys    type of magic, as a key combination
     * @param blockAt the block
     * @return true if magic can be performed on the block
     */
    public abstract boolean canAlter(int keys, Block blockAt);

    /**
     * Perform the wand magic
     *
     * @param entityplayer    player using the wand
     * @param world           world inside which the player is
     * @param start           calculated start block coordinate
     * @param end             calculated end block coordinate
     * @param clicked_current the current block
     * @param keys            type of magic, as key combination
     * @param idOrig          the original current block
     * @param id              the modified current block
     * @return true if something changed, thus damaging the wand
     */
    public abstract boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, IBlockState idOrig, IBlockState id);

    /**
     * Used by {@link #particles(World, BlockPos, int)} when effect type is 0
     *
     * @return array of R,G,B
     */
    public abstract double[] getParticleColor();

    /**
     * Check range of block against wand range of effect
     *
     * @param keys    type of magic, as key combination
     * @param range   volume distance to check against
     * @param max     maximum variation to introduce in the result
     * @param range2D horizontal distance to check against
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
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState id = world.getBlockState(pos);
        IBlockState idOrig = id;
        // general changes
        if (id.getBlock() == Blocks.grass) {
            int meta = id.getBlock().getMetaFromState(id);
            id = Blocks.dirt.getStateFromMeta(meta);
        } // Grass->Dirt
        if (id.getBlock() == Blocks.unlit_redstone_torch) {
            int meta = id.getBlock().getMetaFromState(id);
            id = Blocks.redstone_torch.getStateFromMeta(meta);
        } // RStorch off->on
        if (id.getBlock() == Blocks.unpowered_repeater) {
            int meta = id.getBlock().getMetaFromState(id);
            id = Blocks.powered_repeater.getStateFromMeta(meta);
        } // repeater off->on
        WandCoord3D clicked_current = new WandCoord3D(pos, id);
        // invalid blocks for building
        if (isIncompatibleBlock(id.getBlock())) {
            error(entityplayer, clicked_current, "cantbuild");
            return true;
        }
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new NBTTagCompound());
        }
        int keys = itemstack.getTagCompound().getInteger("Keys");
        if (keys == 0) {
            // MARKING START BLOCK
            world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, id.getBlock().stepSound.getPlaceSound(), (id.getBlock().stepSound.getVolume() + 1.0F) / 2.0F, id.getBlock().stepSound.getFrequency() * 0.8F);
            // saving current block info...
            clicked_current.writeToNBT(itemstack.getTagCompound(), "Start");
            // coloured particles
            particles(world, clicked_current.toPos(), 0);
            // Next use can be target-selection
            itemstack.getTagCompound().setBoolean("Started", true);
            return true;
        } else {
            clicked_current.writeToNBT(itemstack.getTagCompound(), "End");
            // Some keys pressed - END BLOCK
            if (!itemstack.getTagCompound().getBoolean("Started")) {
                error(entityplayer, clicked_current, "nostart");
                return true;
            }
            // find the smaller coords
            WandCoord3D Info = WandCoord3D.getFromNBT(itemstack.getTagCompound(), "Start");
            WandCoord3D Start = Info.copy();
            WandCoord3D End = clicked_current.copy();
            WandCoord3D.findEnds(Start, End);
            if (isTooFar(Start, End, keys, MagicWands.free || entityplayer.capabilities.isCreativeMode)) {
                error(entityplayer, clicked_current, "toofar");
                return true;
            }
            boolean damage = doMagic(entityplayer, world, Start, End, Info, clicked_current, keys, idOrig, id);
            if (damage) {
                itemstack.getTagCompound().setBoolean("Started", false);
                if (!(MagicWands.free || entityplayer.capabilities.isCreativeMode)) {
                    itemstack.damageItem(1, entityplayer);
                    return true;
                }
            }
        }
        return true;
    }

    // CLICKED
    protected boolean canPlace(World world, BlockPos pos, Block block, int keys) {
        if (canAlter(keys, world.getBlockState(pos).getBlock())) {
            if (block.canPlaceBlockAt(world, pos))
                return true;
            if (block == Blocks.cactus || block == Blocks.reeds || block == Blocks.redstone_wire || block == Blocks.stone_pressure_plate || block == Blocks.wooden_pressure_plate || block == Blocks.snow) {
                return false;
            }
            return !(block instanceof BlockTorch || block instanceof BlockFlower);
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
            error(entityplayer, clicked, "toofewitems", neededItems, invItems);
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
     * @param entityplayer player to warn
     * @param pos          position of the error
     * @param reason       message given to the player
     */
    protected void error(EntityPlayer entityplayer, WandCoord3D pos, String reason, Object... info) {
        entityplayer.worldObj.playSoundEffect(pos.x, pos.y, pos.z, "damage.fallsmall", (entityplayer.worldObj.rand.nextFloat() + 0.7F) / 2.0F, 0.5F + entityplayer.worldObj.rand.nextFloat() * 0.3F);
        if (!entityplayer.worldObj.isRemote)
            entityplayer.addChatComponentMessage(new ChatComponentTranslation("error.wand." + reason, info));
        particles(entityplayer.worldObj, pos.x, pos.y, pos.z, 3);
    }

    /**
     * First check for block compatibility with the wand power. Overridden in
     * {@link BuildWand}
     *
     * @param id the block id to check
     * @return true if the wand can't interact with this block
     */
    protected boolean isIncompatibleBlock(Block id) {
        return false;
    }

    /**
     * Spawn particles near coordinates, depending on effect type
     *
     * @param effect 0 - wand, 1 - smoke, 2 - splash, other - reddust
     */
    protected void particles(World world, int i, int j, int k, int effect) {
        double d = 0.0625D;
        if (effect == 1) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, i + 0.5D, j + 0.5D, k + 0.5D, 0.0D, 0.0D, 0.0D);
            return;
        } else if (effect == 2) {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH, i + 0.5D, j + 1D, k + 0.5D, 0.0D, 0.0D, 0.0D);
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
            double d1 = i + itemRand.nextFloat();
            double d2 = j + itemRand.nextFloat();
            double d3 = k + itemRand.nextFloat();
            if (l == 0 && !world.getBlockState(new BlockPos(i, j + 1, k)).getBlock().isOpaqueCube()) {
                d2 = j + 1 + d;
            }
            if (l == 1 && !world.getBlockState(new BlockPos(i, j - 1, k)).getBlock().isOpaqueCube()) {
                d2 = j + 0 - d;
            }
            if (l == 2 && !world.getBlockState(new BlockPos(i, j, k + 1)).getBlock().isOpaqueCube()) {
                d3 = k + 1 + d;
            }
            if (l == 3 && !world.getBlockState(new BlockPos(i, j, k - 1)).getBlock().isOpaqueCube()) {
                d3 = k + 0 - d;
            }
            if (l == 4 && !world.getBlockState(new BlockPos(i + 1, j, k)).getBlock().isOpaqueCube()) {
                d1 = i + 1 + d;
            }
            if (l == 5 && !world.getBlockState(new BlockPos(i - 1, j, k)).getBlock().isOpaqueCube()) {
                d1 = i + 0 - d;
            }
            if (d1 < i || d1 > i + 1 || d2 < 0.0D || d2 > j + 1 || d3 < k || d3 > k + 1) {
                world.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, R, G, B);
            }
        }
    }

    /**
     * @param effect 0 -wand, 1 - smoke, 2 - splash, 3-error
     */
    protected void particles(World world, BlockPos c, int effect) {
        particles(world, c.getX(), c.getY(), c.getZ(), effect);
    }

    public static int getNeededCount(IBlockState id) {
        if (id.getBlock() instanceof BlockSlab) {
            return 2;
        } else {
            return 1;
        }
    }

    public static ItemStack getNeededItem(IBlockState state) {
        Block id = state.getBlock();
        if (id instanceof BlockLeaves) {
            return new ItemStack(id, 1, id.damageDropped(state));
        } else if (id == Blocks.stone || id == Blocks.coal_ore || id == Blocks.clay || id == Blocks.deadbush || id == Blocks.bookshelf
                || id == Blocks.fire || id instanceof BlockStairs || id == Blocks.farmland || id == Blocks.diamond_ore || id == Blocks.lapis_ore
                || id instanceof BlockRedstoneOre || id == Blocks.glowstone || id == Blocks.ice || id == Blocks.snow || id == Blocks.stonebrick) {
            return new ItemStack(id, 1, id.getMetaFromState(state));
        } else {
            return new ItemStack(id.getItemDropped(state, itemRand, 0), 1, id.damageDropped(state));
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

    public static void addOre(String oreName, boolean toMine) {
        if (!oreName.equals("")) {
            Block temp = GameData.getBlockRegistry().getObject(oreName);
            if (temp != Blocks.air) {
                if (toMine)
                    m_ores.add(temp);
                else
                    ores.add(temp);
            }
        }
    }
}
