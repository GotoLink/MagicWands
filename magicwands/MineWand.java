package magicwands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class MineWand extends WandItem {
    protected static final int MINE_ALL = 100, MINE_DIRT = 10, MINE_WOOD = 1, MINE_ORES = 110;

    public MineWand(boolean reinforced) {
        super(reinforced);
        setMaxDamage(reinforced ? 120 : 15);// 8/80 uses
    }

    @Override
    public boolean canAlter(int keys, Block block) {
        if(block instanceof BlockAir)
            return false;
        switch (keys) {
            case MINE_ALL:
                return (block != Blocks.bedrock || MagicWands.bedrock) && (block != Blocks.obsidian || MagicWands.obsidian);
            case MINE_DIRT:
                return (block == Blocks.grass || block == Blocks.dirt || block == Blocks.sand || block == Blocks.gravel || block == Blocks.leaves
                        || block == Blocks.farmland || block == Blocks.snow || block == Blocks.soul_sand || block == Blocks.vine || block instanceof BlockFlower);
            case MINE_WOOD:
                return block.getMaterial() == Material.wood;
            case MINE_ORES:
                return isMiningOre(block) && (block != Blocks.bedrock || MagicWands.bedrock) && (block != Blocks.obsidian || MagicWands.obsidian);
        }
        return false;
    }

    @Override
    public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, IBlockState idOrig, IBlockState id) {
        boolean damage = do_Mining(world, start, end, clicked_current, keys, entityplayer);
        if (damage)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.explode", 2.5F, 0.5F + world.rand.nextFloat() * 0.3F);
        return damage;
    }

    @Override
    public double[] getParticleColor() {
        return new double[]{0.01D, 0.8D, 1.0D};
    }

    @Override
    public boolean isTooFar(int keys, int range, int max, int range2d) {
        switch (keys) {
            case MINE_ALL:
            case MINE_DIRT:
                return reinforced ? range - 250 > max : range - 50 > max;
            case MINE_WOOD:
                return reinforced ? range2d - 400 > max : range2d - 50 > max;
            case MINE_ORES:
                return reinforced ? range2d - 60 > max : range2d - 30 > max;
        }
        return true;
    }

    // ==== MINING ====
    private boolean do_Mining(World world, WandCoord3D start, WandCoord3D end, WandCoord3D clicked, int keys, EntityPlayer entityplayer) {
        // MINING OBSIDIAN 1x1x1
        BlockPos startPos = start.toPos();
        BlockPos endPos = end.toPos();
        if (keys == MINE_ALL && startPos.equals(endPos) && !MagicWands.obsidian) {
            IBlockState state = world.getBlockState(startPos);
            if (state.getBlock() == Blocks.obsidian){
                Blocks.obsidian.onBlockDestroyedByPlayer(world, startPos, state);
                Blocks.obsidian.harvestBlock(world, entityplayer, startPos, state, world.getTileEntity(startPos));
                particles(world, startPos, 1);
                return true;
            }
        }
        int X, Y, Z;
        IBlockState metaAt;
        Block blockAt;
        int blocks2Dig = 0;
        boolean FREE = MagicWands.free || entityplayer.capabilities.isCreativeMode;
        int max = (reinforced || FREE) ? 1024 : 512;
        // MINING ORES
        if (keys == MINE_ORES) {
            // counting ores to mine
            Iterable iterable = BlockPos.getAllInBox(new BlockPos(start.x, 1, start.z), new BlockPos(end.x, 128, end.z));
            for (Object object : iterable) {
                blockAt = world.getBlockState((BlockPos)object).getBlock();
                if (isMiningOre(blockAt)) {
                    // add more drops for redstone and lapis
                    if (blockAt == Blocks.redstone_ore || blockAt == Blocks.lit_redstone_ore || blockAt == Blocks.lapis_ore) {
                        blocks2Dig += 4;
                    } else {
                        blocks2Dig++;
                    }
                }
            }
            if (blocks2Dig - max > 10) {// 10 blocks tolerance
                error(entityplayer, clicked, "toomanytodig", blocks2Dig, max);
                return true;
            }
            // harvesting the ores
            boolean underground;
            int surface = 127;
            long cnt = 0;
            boolean surfaceBlock;
            BlockPos pos;
            for (X = start.x; X <= end.x; X++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    underground = false;
                    for (Y = 127; Y > 1; Y--) {
                        pos = new BlockPos(X, Y, Z);
                        metaAt = world.getBlockState(pos);
                        if (!underground && world.isAirBlock(pos)) {
                            surface = Y;
                        }
                        surfaceBlock = isSurface(metaAt.getBlock());
                        if (!underground && surfaceBlock)
                            underground = true;
                        if (isMiningOre(metaAt.getBlock())){
                            TileEntity tile = world.getTileEntity(pos);
                            if(world.setBlockState(pos, Blocks.stone.getDefaultState())) {
                                pos = new BlockPos(X, surface, Z);
                                metaAt.getBlock().onBlockDestroyedByPlayer(world, pos, metaAt);
                                metaAt.getBlock().harvestBlock(world, entityplayer, pos, metaAt, tile);
                                cnt++;
                            }
                        }
                    }
                }
            }
            if (cnt == 0) {
                if (!world.isRemote)
                    entityplayer.addChatComponentMessage(new ChatComponentTranslation("result.wand.mine"));
                return false;
            }
            return true;
        }
        // NORMAL MINING
        Iterable iterable = BlockPos.getAllInBox(startPos, endPos.up());
        for (Object object : iterable) {
            blockAt = world.getBlockState((BlockPos) object).getBlock();
            if (canAlter(keys, blockAt)) {
                // add more drops for redstone and lapis
                if (blockAt == Blocks.redstone_ore || blockAt == Blocks.lit_redstone_ore || blockAt == Blocks.lapis_ore) {
                    blocks2Dig += 4;
                } else {
                    blocks2Dig++;
                }
            }
        }
        if (blocks2Dig >= max) {
            error(entityplayer, clicked, "toomanytodig", blocks2Dig, max);
            return false;
        }
        // now the mining itself.
        if (blocks2Dig == 0) {
            if (!world.isRemote)
                entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
            return false;
        }
        iterable = BlockPos.getAllInBox(startPos, endPos.up());
        for (Object object : iterable) {
            metaAt = world.getBlockState((BlockPos) object);
            if (canAlter(keys, metaAt.getBlock())) {
                TileEntity tile = world.getTileEntity((BlockPos) object);
                if(metaAt.getBlock().removedByPlayer(world, (BlockPos) object, entityplayer, true)) {
                    metaAt.getBlock().onBlockDestroyedByPlayer(world, (BlockPos) object, metaAt);
                    metaAt.getBlock().harvestBlock(world, entityplayer, (BlockPos) object, metaAt, tile);
                    if (itemRand.nextInt(blocks2Dig / 50 + 1) == 0)
                        particles(world, (BlockPos) object, 1);
                }
            }
        }
        return true;
    }
}
