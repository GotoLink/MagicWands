package magicwands;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.relauncher.Side;

public final class WandKeyPacket extends MagicWandPacket {
    int keyCode;

    public WandKeyPacket() {
    }

    public WandKeyPacket(int id, int key) {
        this.entityId = id;
        this.keyCode = key;
    }

    @Override
    void execute(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
                player.getCurrentEquippedItem().setTagInfo("Keys", new NBTTagInt(keyCode));
                if (player instanceof EntityPlayerMP) {
                    MagicWands.channel.sendTo(getPacket(Side.CLIENT), (EntityPlayerMP) player);
                }
            }
        }
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
