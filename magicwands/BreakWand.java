package magicwands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockStationary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class BreakWand extends WandItem {
	protected static final int BREAK_XORES = 100, BREAK_ALL = 10, BREAK_WEAK = 1;

	public BreakWand(int i, boolean reinforced) {
		super(i, reinforced);
		setMaxDamage(reinforced ? 120 : 15);// 10/100 uses
	}

	@Override
	public boolean canAlter(int keys, int blockAt) {
		Block block = Block.blocksList[blockAt];
		switch (keys) {
		case BREAK_XORES:
			return (blockAt != Block.bedrock.blockID || MagicWands.bedrock) && !isOre(blockAt);
		case BREAK_ALL:
			return (blockAt != Block.bedrock.blockID || MagicWands.bedrock);
		case BREAK_WEAK:
			return (blockAt == Block.leaves.blockID || block instanceof BlockFlower || blockAt == Block.crops.blockID || blockAt == Block.snow.blockID || block instanceof BlockFluid
					|| block instanceof BlockStationary || blockAt == Block.fire.blockID || blockAt == Block.vine.blockID);
		}
		return false;
	}

	@Override
	public boolean doMagic(EntityPlayer entityplayer, World world, WandCoord3D start, WandCoord3D end, WandCoord3D info, WandCoord3D clicked_current, int keys, int idOrig, int id, int meta) {
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
		return new double[] { 0.5D, 0.5D, 0.5D };
	}

	@Override
	public boolean isTooFar(int keys, int range, int max, int range2d) {
		return reinforced ? range - 250 > max : range - 50 > max;
	}

	// ==== BREAKING ====
	private boolean do_Breaking(World world, WandCoord3D start, WandCoord3D end, WandCoord3D clicked, int keys, EntityPlayer entityplayer) {
		int X, Y, Z, blockAt;
		// First, see if there's any work to do--count the breakable blocks.
		// Not entirely sure a pre-count is necessary with a breaking wand.
		int cnt = 0;
		for (X = start.x; X <= end.x; X++) {
			for (Y = start.y; Y <= end.y; Y++) {
				for (Z = start.z; Z <= end.z; Z++) {
					blockAt = world.getBlockId(X, Y, Z);
					if (blockAt != 0) {
						if (canAlter(keys, blockAt))
							cnt++;
					}
				}
			}
		}
		if (cnt == 0) {
			if (!world.isRemote)
				entityplayer.addChatMessage(StatCollector.translateToLocal("message.wand.nowork"));
			return false;
		}
		for (X = start.x; X <= end.x; X++) {
			for (Y = start.y; Y <= end.y; Y++) {
				for (Z = start.z; Z <= end.z; Z++) {
					blockAt = world.getBlockId(X, Y, Z);
					if (blockAt != 0) {
						if (canAlter(keys, blockAt)) {
							int metaAt = world.getBlockMetadata(X, Y, Z);
							Block.blocksList[blockAt].onBlockDestroyedByPlayer(world, X, Y, Z, metaAt);
							world.setBlock(X, Y, Z, 0);
							if (rand.nextInt(cnt / 50 + 1) == 0)
								particles(world, X, Y, Z, 1);
						}
					}
				}
			}
		}
		return true;
	}
}
