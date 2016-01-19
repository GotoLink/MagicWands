package magicwands;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class BuildWand extends WandItem {
    protected static final int BUILD_BOX = 100, BUILD_ROOM = 10, BUILD_FRAME = 1, BUILD_WATER = 110, BUILD_TORCHES = 101, BUILD_CAVES = 11, BUILD_LAVA = 111;

    public BuildWand(boolean reinforced) {
        super(reinforced);
        setMaxDamage(reinforced ? 200 : 30);// 20/200 uses
    }

    @Override
    public boolean canAlter(int keys, Block block) {
        if (block instanceof BlockAir || block == Blocks.snow || block instanceof BlockFire || block instanceof BlockVine || block instanceof BlockBush || block instanceof BlockLeavesBase) {
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
                return (block instanceof BlockTorch || block instanceof BlockLiquid);
        }
        return false;
    }

    @Override
    public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, IBlockState idOrig, IBlockState id) {
        if (id.getBlock() != info.id() && (keys == BUILD_BOX || keys == BUILD_ROOM || keys == BUILD_FRAME || keys == BUILD_TORCHES)) {
            error(entityplayer, clicked_current, "notsamecorner");
            return false;
        }
        if (id != info.state && (keys == BUILD_BOX || keys == BUILD_ROOM || keys == BUILD_FRAME || keys == BUILD_TORCHES) && !(id.getBlock() == Blocks.cactus || id.getBlock() == Blocks.reeds)) {
            if (!(id.getBlock() instanceof BlockLeaves) || ((id.getBlock().damageDropped(id)) != (info.state.getBlock().damageDropped(info.state)))) {
                error(entityplayer, clicked_current, "notsamecorner");
                return false;
            }
        }
        boolean flag = do_Building(world, start, end, clicked_current, keys, entityplayer, idOrig);
        if (flag && keys != BUILD_WATER)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.pop", (world.rand.nextFloat() + 0.7F) / 2.0F,
                    1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
        if (flag && keys == BUILD_WATER)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "liquid.splash", (world.rand.nextFloat() + 0.7F) / 2.0F,
                    1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
        return flag;
    }

    @Override
    public double[] getParticleColor() {
        return new double[]{1.0D, 0.8D, 0.0D};
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
    private boolean do_Building(World world, WandCoord3D start, WandCoord3D end, WandCoord3D clicked, int keys, EntityPlayer entityplayer, IBlockState idOrig) {
        int X, Y = 0, Z;
        IBlockState blockAt;
        BlockPos pos;
        ItemStack neededStack = getNeededItem(clicked.state);
        int multiplier = getNeededCount(clicked.state);
        int neededItems;
        int affected = 0;
        boolean FREE = MagicWands.free || entityplayer.capabilities.isCreativeMode;
        switch (keys) {
            case BUILD_BOX:
                neededItems = 0;
                Iterable<BlockPos> itr = WandCoord3D.between(start, end);
                // count needed blocks
                for(BlockPos temp : itr) {
                    if (canPlace(world, temp, clicked.id(), keys)) {
                        neededItems += multiplier;
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
                    for (BlockPos temp : itr) {
                        if (canPlace(world, temp, clicked.id(), keys)) {
                            world.setBlockState(temp, clicked.state, 3);
                            if (itemRand.nextInt(neededItems / 50 + 1) == 0)
                                particles(world, temp, 0);
                            affected++;
                        }
                    }
                    if (idOrig.getBlock() == Blocks.grass && affected > 0) {
                        for (int run = 0; run <= 1; run++) {
                            if (run == 0)
                                Y = start.y;
                            if (run == 1)
                                Y = end.y;
                            for (X = start.x; X <= end.x; X++) {
                                for (Z = start.z; Z <= end.z; Z++) {
                                    pos = new BlockPos(X, Y, Z);
                                    if (world.getBlockState(pos) == Blocks.dirt && (world.getBlockState(pos.up()) == Blocks.air
                                            || !world.getBlockState(pos.up()).getBlock().isFullBlock())) {
                                        world.setBlockState(pos, Blocks.grass.getDefaultState());
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
                                if (canPlace(world, new BlockPos(X, Y, Z), clicked.id(), keys)) {
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
                                    pos = new BlockPos(X, Y, Z);
                                    if (canPlace(world, pos, clicked.id(), keys)) {
                                        world.setBlockState(pos, clicked.state, 3);
                                        if (itemRand.nextInt(neededItems / 50 + 1) == 0)
                                            particles(world, X, Y, Z, 0);
                                        affected++;
                                    }
                                }
                            }
                        }
                    }
                    if (idOrig.getBlock() == Blocks.grass && affected > 0) {
                        for (int run = 0; run <= 1; run++) {
                            if (run == 0)
                                Y = start.y;
                            if (run == 1)
                                Y = end.y;
                            for (X = start.x; X <= end.x; X++) {
                                for (Z = start.z; Z <= end.z; Z++) {
                                    pos = new BlockPos(X, Y, Z);
                                    if (world.getBlockState(pos) == Blocks.dirt && (world.getBlockState(pos.up()) == Blocks.air
                                            || !world.getBlockState(pos.up()).getBlock().isFullBlock())) {
                                        world.setBlockState(pos, Blocks.grass.getDefaultState());
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
                                if (canPlace(world, new BlockPos(X, Y, Z), clicked.id(), keys)) {
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
                                    if (canPlace(world, new BlockPos(X, Y, Z), clicked.id(), keys)) {
                                        world.setBlockState(new BlockPos(X, Y, Z), clicked.state, 3);
                                        if (itemRand.nextInt(neededItems / 50 + 1) == 0)
                                            particles(world, X, Y, Z, 0);
                                        affected++;
                                    }
                                }
                            }
                        }
                    }
                    if (idOrig.getBlock() == Blocks.grass && affected > 0) {
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
                                        pos = new BlockPos(X, Y, Z);
                                        if (world.getBlockState(pos) == Blocks.dirt && (world.getBlockState(pos.up()) == Blocks.air
                                                || !world.getBlockState(pos.up()).getBlock().isFullBlock())) {
                                            world.setBlockState(pos, Blocks.grass.getDefaultState());
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
                            if (canPlace(world, new BlockPos(X, Y, Z), clicked.id(), keys)) {
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
                                pos = new BlockPos(X, Y, Z);
                                if (canPlace(world, pos, clicked.id(), keys)) {
                                    world.setBlockState(pos, clicked.state, 3);
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
                itr = WandCoord3D.between(start, end);
                if (!FREE) {
                    // count items in inventory
                    neededItems = 0;
                    // count needed blocks
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (canAlter(keys, blockAt.getBlock())) {
                            neededItems++;
                        }
                    }
                    if (neededItems == 0) {
                        if (!world.isRemote)
                            entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
                        return false;
                    }
                }
                if (emptyBuckets(Items.water_bucket, entityplayer, 2)) {
                    // do the building
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (canAlter(keys, blockAt.getBlock())) {
                            world.setBlockState(temp, Blocks.flowing_water.getDefaultState());
                            affected++;
                        }
                    }
                    if (affected == 0)
                        return false;
                    // notification
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (blockAt.getBlock() == Blocks.flowing_water) {
                            world.notifyNeighborsRespectDebug(temp, Blocks.flowing_water);
                            if (world.isAirBlock(temp.up()))
                                particles(world, temp, 2);
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
                // count Items in inventory
                neededItems = 0;
                itr = WandCoord3D.between(start, end);
                if (!FREE) {
                    // count needed blocks
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (canAlter(keys, blockAt.getBlock()))
                            neededItems++;
                    }
                    if (neededItems == 0) {
                        if (!world.isRemote)
                            entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
                        return false;
                    }
                }
                if (emptyBuckets(Items.lava_bucket, entityplayer, neededItems)) {
                    // do the building
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (canAlter(keys, blockAt.getBlock())) {
                            world.setBlockState(temp, Blocks.flowing_lava.getDefaultState());
                            affected++;
                        }
                    }
                    if (affected == 0)
                        return false;
                    // notification
                    for (BlockPos temp : itr) {
                        blockAt = world.getBlockState(temp);
                        if (blockAt.getBlock() == Blocks.flowing_lava) {
                            world.notifyNeighborsRespectDebug(temp, Blocks.flowing_lava);
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
                            pos = new BlockPos(X, Y, Z);
                            blockAt = world.getBlockState(pos);
                            boolean surfaceBlock = isSurface(blockAt.getBlock());
                            if (!underground && surfaceBlock) {
                                underground = true;
                                continue;
                            }
                            if (!underground) {
                                continue;
                            }
                            if (canAlter(keys, blockAt.getBlock())) {
                                world.setBlockState(pos, Blocks.stone.getDefaultState());
                                cnt++;
                            }
                        }
                    }
                }
                if (cnt > 0) {
                    if (!world.isRemote)
                        entityplayer.addChatComponentMessage(new ChatComponentTranslation("result.wand.fill", cnt));
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
