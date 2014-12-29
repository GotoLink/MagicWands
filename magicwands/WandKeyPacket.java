package magicwands;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public final class WandKeyPacket extends MagicWandPacket {
    int keyCode;

    public WandKeyPacket() {
    }

    public WandKeyPacket(int id, int key) {
        this.entityId = id;
        this.keyCode = key;
    }

    @Override
    FMLProxyPacket execute(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
                if (!player.getCurrentEquippedItem().hasTagCompound()) {
                    player.getCurrentEquippedItem().stackTagCompound = new NBTTagCompound();
                }
                player.getCurrentEquippedItem().getTagCompound().setInteger("Keys", keyCode);
                if (player instanceof EntityPlayerMP) {
                    return getPacket(Side.CLIENT);
                }
            }
        }
        return null;
    }

    @Override
    String getChannel() {
        return PacketHandler.CHANNEL;
    }

    @Override
    public void fromBytes(ByteBuf in) {
        super.fromBytes(in);
        this.keyCode = in.readInt();
    }

    @Override
    public void toBytes(ByteBuf out) {
        super.toBytes(out);
        out.writeInt(keyCode);
    }
}
