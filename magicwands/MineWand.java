package magicwands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
    public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, Block idOrig, Block id, int meta) {
        if (MagicWands.disableNotify)
            world.scheduledUpdatesAreImmediate = true; // scheduledUpdatesAreImmediate
        boolean damage = do_Mining(world, start, end, clicked_current, keys, entityplayer);
        if (damage)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.explode", 2.5F, 0.5F + world.rand.nextFloat() * 0.3F);
        if (MagicWands.disableNotify)
            world.scheduledUpdatesAreImmediate = false;
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
        int X, Y, Z, metaAt;
        Block blockAt;
        int blocks2Dig = 0;
        // MINING OBSIDIAN 1x1x1
        if (keys == MINE_ALL && start.x == end.x && start.y == end.y && start.z == end.z && !MagicWands.obsidian && world.getBlock(start.x, start.y, start.z) == Blocks.obsidian) {
            Blocks.obsidian.onBlockDestroyedByPlayer(world, start.x, start.y, start.z, 0);
            Blocks.obsidian.harvestBlock(world, entityplayer, start.x, start.y, start.z, 0);
            particles(world, start.x, start.y, start.z, 1);
            return true;
        }
        boolean FREE = MagicWands.free || entityplayer.capabilities.isCreativeMode;
        int max = (reinforced || FREE) ? 1024 : 512;
        // MINING ORES
        if (keys == MINE_ORES) {
            // counting ores to mine
            for (X = start.x; X <= end.x; X++) {
                for (Y = 1; Y < 128; Y++) {
                    for (Z = start.z; Z <= end.z; Z++) {
                        blockAt = world.getBlock(X, Y, Z);
                        if (isMiningOre(blockAt)) {
                            // add more drops for redstone and lapis
                            if (blockAt == Blocks.redstone_ore || blockAt == Blocks.lit_redstone_ore || blockAt == Blocks.lapis_ore) {
                                blocks2Dig += 4;
                            } else {
                                blocks2Dig++;
                            }
                        }
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
            for (X = start.x; X <= end.x; X++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    underground = false;
                    for (Y = 127; Y > 1; Y--) {
                        blockAt = world.getBlock(X, Y, Z);
                        if (!underground && world.isAirBlock(X, Y, Z)) {
                            surface = Y;
                        }
                        surfaceBlock = isSurface(blockAt);
                        if (!underground && surfaceBlock)
                            underground = true;
                        if (isMiningOre(blockAt)) {
                            metaAt = world.getBlockMetadata(X, Y, Z);
                            world.setBlock(X, Y, Z, Blocks.stone);
                            blockAt.onBlockDestroyedByPlayer(world, X, surface, Z, metaAt);
                            blockAt.harvestBlock(world, entityplayer, X, surface, Z, metaAt);
                            cnt++;
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
        for (X = start.x; X <= end.x; X++) {
            for (Y = start.y; Y <= end.y; Y++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    blockAt = world.getBlock(X, Y, Z);
                    if (canAlter(keys, blockAt)) {
                        // add more drops for redstone and lapis
                        if (blockAt == Blocks.redstone_ore || blockAt == Blocks.lit_redstone_ore || blockAt == Blocks.lapis_ore) {
                            blocks2Dig += 4;
                        } else {
                            blocks2Dig++;
                        }
                    }
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
        for (X = start.x; X <= end.x; X++) {
            for (Y = start.y; Y <= end.y; Y++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    blockAt = world.getBlock(X, Y, Z);
                    metaAt = world.getBlockMetadata(X, Y, Z);
                    if (canAlter(keys, blockAt)) {
                        world.setBlockToAir(X, Y, Z);
                        blockAt.onBlockDestroyedByPlayer(world, X, Y, Z, metaAt);
                        blockAt.harvestBlock(world, entityplayer, X, Y, Z, metaAt);
                        if (itemRand.nextInt(blocks2Dig / 50 + 1) == 0)
                            particles(world, X, Y, Z, 1);
                    }
                }
            }
        }
        return true;
    }
}
