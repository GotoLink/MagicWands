package magicwands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MagicWandPacket implements IMessage {
    int entityId;

    public final FMLProxyPacket getPacket(Side side) {
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        toBytes(out);
        FMLProxyPacket packet = new FMLProxyPacket(out, getChannel());
        packet.setTarget(side);
        return packet;
    }

    abstract void execute(Entity entity);

    abstract String getChannel();

    public void fromBytes(ByteBuf in) {
        entityId = in.readInt();
    }

    void run(EntityPlayer player) {
        execute(player.worldObj.getEntityByID(entityId));
    }

    public void toBytes(ByteBuf out) {
        out.writeInt(entityId);
    }
}
