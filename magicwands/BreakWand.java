package magicwands;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class BreakWand extends WandItem {
    protected static final int BREAK_XORES = 100, BREAK_ALL = 10, BREAK_WEAK = 1;

    public BreakWand(boolean reinforced) {
        super(reinforced);
        setMaxDamage(reinforced ? 120 : 15);// 10/100 uses
    }

    @Override
    public boolean canAlter(int keys, Block block) {
        if(block instanceof BlockAir)
            return false;
        switch (keys) {
            case BREAK_XORES:
                return (block != Blocks.bedrock || MagicWands.bedrock) && !isOre(block);
            case BREAK_ALL:
                return (block != Blocks.bedrock || MagicWands.bedrock);
            case BREAK_WEAK:
                return (block == Blocks.snow || block instanceof BlockFire || block instanceof BlockVine || block instanceof BlockBush || block instanceof BlockLiquid || block instanceof BlockLeavesBase
                );
        }
        return false;
    }

    @Override
    public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, IBlockState idOrig, IBlockState id) {
        boolean damage = do_Breaking(world, start, end, keys, entityplayer);
        if (damage)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.explode", 2.5F, 0.5F + world.rand.nextFloat() * 0.3F);
        return damage;
    }

    @Override
    public double[] getParticleColor() {
        return new double[]{0.5D, 0.5D, 0.5D};
    }

    @Override
    public boolean isTooFar(int keys, int range, int max, int range2d) {
        return reinforced ? range - 250 > max : range - 50 > max;
    }

    // ==== BREAKING ====
    private boolean do_Breaking(World world, WandCoord3D start, WandCoord3D end, int keys, EntityPlayer entityplayer) {
        IBlockState blockAt;
        // First, see if there's any work to do--count the breakable blocks.
        // Not entirely sure a pre-count is necessary with a breaking wand.
        int cnt = 0;
        Iterable<BlockPos> list = WandCoord3D.between(start, end);
        for(BlockPos pos : list){
            blockAt = world.getBlockState(pos);
            if (canAlter(keys, blockAt.getBlock())) {
                cnt++;
                if(blockAt.getBlock().removedByPlayer(world, pos, entityplayer, false)) {
                    blockAt.getBlock().onBlockDestroyedByPlayer(world, pos, blockAt);
                    if (itemRand.nextInt(cnt / 50 + 1) == 0)
                        particles(world, pos.getX(), pos.getY(), pos.getZ(), 1);
                }
            }
        }
        if (cnt == 0) {
            if (!world.isRemote)
                entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
            return false;
        }
        return true;
    }
}
