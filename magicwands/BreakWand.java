package magicwands;

import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
                return (block == Blocks.snow || block == Blocks.fire || block == Blocks.vine || block instanceof BlockBush || block instanceof BlockLiquid || block instanceof BlockLeavesBase
                );
        }
        return false;
    }

    @Override
    public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, Block idOrig, Block id, int meta) {
        if (MagicWands.disableNotify)
            world.scheduledUpdatesAreImmediate = true;
        boolean damage = do_Breaking(world, start, end, clicked_current, keys, entityplayer);
        if (damage)
            world.playSoundEffect(clicked_current.x, clicked_current.y, clicked_current.z, "random.explode", 2.5F, 0.5F + world.rand.nextFloat() * 0.3F);
        if (MagicWands.disableNotify)
            world.scheduledUpdatesAreImmediate = false;
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
    private boolean do_Breaking(World world, WandCoord3D start, WandCoord3D end, WandCoord3D clicked, int keys, EntityPlayer entityplayer) {
        int X, Y, Z;
        Block blockAt;
        // First, see if there's any work to do--count the breakable blocks.
        // Not entirely sure a pre-count is necessary with a breaking wand.
        int cnt = 0;
        for (X = start.x; X <= end.x; X++) {
            for (Y = start.y; Y <= end.y; Y++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    blockAt = world.getBlock(X, Y, Z);
                    if (canAlter(keys, blockAt))
                        cnt++;
                }
            }
        }
        if (cnt == 0) {
            if (!world.isRemote)
                entityplayer.addChatComponentMessage(new ChatComponentTranslation("message.wand.nowork"));
            return false;
        }
        for (X = start.x; X <= end.x; X++) {
            for (Y = start.y; Y <= end.y; Y++) {
                for (Z = start.z; Z <= end.z; Z++) {
                    blockAt = world.getBlock(X, Y, Z);
                    if (canAlter(keys, blockAt)) {
                        int metaAt = world.getBlockMetadata(X, Y, Z);
                        blockAt.onBlockDestroyedByPlayer(world, X, Y, Z, metaAt);
                        world.setBlockToAir(X, Y, Z);
                        if (itemRand.nextInt(cnt / 50 + 1) == 0)
                            particles(world, X, Y, Z, 1);
                    }
                }
            }
        }
        return true;
    }
}
