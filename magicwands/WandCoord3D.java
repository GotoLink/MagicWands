package magicwands;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;

public final class WandCoord3D {
    public int x, y, z;
    public IBlockState state;

    public WandCoord3D() {
        this(BlockPos.ORIGIN, Blocks.air.getDefaultState());
    }

    public WandCoord3D(int i, int j, int k) {
        x = i;
        y = j;
        z = k;
    }

    public WandCoord3D(BlockPos pos, IBlockState ID) {
        this(pos.getX(), pos.getY(), pos.getZ());
        state = ID;
    }

    public WandCoord3D(int i, int j, int k, int ID, int met) {
        this(i, j, k);
        state = Block.getBlockById(ID).getStateFromMeta(met);
    }

    public WandCoord3D(WandCoord3D a) {
        this(a.x, a.y, a.z);
        state = a.state;
    }

    public WandCoord3D copy() {
        return new WandCoord3D(this);
    }

    public Block id(){
        return state.getBlock();
    }

    public int meta(){
        return id().getMetaFromState(state);
    }

    public BlockPos toPos(){
        return new BlockPos(x, y, z);
    }

    public int getArea(WandCoord3D b) {
        return Math.abs(x - b.x + 1) * Math.abs(y - b.y + 1) * Math.abs(z - b.z + 1);
    }

    public double getDistance(WandCoord3D b) {
        double d3 = x - b.x;
        double d4 = y - b.y;
        double d5 = z - b.z;
        return MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
    }

    public double getDistanceFlat(WandCoord3D b) {
        double d3 = x - b.x;
        double d5 = z - b.z;
        return MathHelper.sqrt_double(d3 * d3 + d5 * d5);
    }

    public int getFlatArea(WandCoord3D b) {
        return Math.abs(x - b.x + 1) * Math.abs(z - b.z + 1);
    }

    public void set(int i, int j, int k) {
        x = i;
        y = j;
        z = k;
    }

    public void setTo(WandCoord3D a) {
        set(a.x, a.y, a.z);
    }

    public void writeToNBT(NBTTagCompound compound, String key) {
        if (!compound.hasKey("Coord3d")) {
            compound.setTag("Coord3d", new NBTTagCompound());
        }
        compound.getCompoundTag("Coord3d").setIntArray(key, new int[]{x, y, z, Block.getIdFromBlock(state.getBlock()), state.getBlock().getMetaFromState(state)});
    }

    public static void findEnds(WandCoord3D a, WandCoord3D b) {
        WandCoord3D n = new WandCoord3D();
        WandCoord3D m = new WandCoord3D();
        n.x = a.x > b.x ? b.x : a.x;
        n.y = a.y > b.y ? b.y : a.y;
        n.z = a.z > b.z ? b.z : a.z;
        m.x = a.x < b.x ? b.x : a.x;
        m.y = a.y < b.y ? b.y : a.y;
        m.z = a.z < b.z ? b.z : a.z;
        a.setTo(n);
        b.setTo(m);
    }

    public static int getArea(WandCoord3D a, WandCoord3D b) {
        return Math.abs(a.x - b.x + 1) * Math.abs(a.y - b.y + 1) * Math.abs(a.z - b.z + 1);
    }

    public static int getFlatArea(WandCoord3D a, WandCoord3D b) {
        return Math.abs(a.x - b.x + 1) * Math.abs(a.z - b.z + 1);
    }

    public static WandCoord3D getFromNBT(NBTTagCompound compound, String key) {
        if (compound.hasKey("Coord3d")) {
            NBTTagCompound nbt = compound.getCompoundTag("Coord3d");
            if (nbt.hasKey(key)) {
                int[] coord = nbt.getIntArray(key);
                if (coord.length == 5) {
                    return new WandCoord3D(coord[0], coord[1], coord[2], coord[3], coord[4]);
                }
            }
        }
        return null;
    }

    public static Iterable<BlockPos> between(WandCoord3D start, WandCoord3D end){
        ArrayList<BlockPos> list = new ArrayList<BlockPos>(getArea(start, end));
        for (int X = start.x; X <= end.x; X++) {
            for (int Z = start.z; Z <= end.z; Z++) {
                for (int Y = start.y; Y <= end.y; Y++) {
                    list.add(new BlockPos(X, Y, Z));
                }
            }
        }
        return list;
    }
}
