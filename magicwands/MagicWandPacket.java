package magicwands;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public abstract class MagicWandPacket implements IMessage {
    int entityId;

    public final FMLProxyPacket getPacket(Side side) {
        ByteBuf out = Unpooled.buffer();
        toBytes(out);
        FMLProxyPacket packet = new FMLProxyPacket(out, getChannel());
        packet.setTarget(side);
        return packet;
    }

    abstract FMLProxyPacket execute(Entity entity);

    abstract String getChannel();

    public void fromBytes(ByteBuf in) {
        entityId = in.readInt();
    }

    FMLProxyPacket run(EntityPlayer player) {
        return execute(player.worldObj.getEntityByID(entityId));
    }

    public void toBytes(ByteBuf out) {
        out.writeInt(entityId);
    }
}
