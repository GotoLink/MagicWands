package magicwands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class WandKeyPacket extends MagicWandPacket {
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
				if (!player.getCurrentEquippedItem().hasTagCompound()) {
					player.getCurrentEquippedItem().stackTagCompound = new NBTTagCompound();
				}
				player.getCurrentEquippedItem().getTagCompound().setInteger("Keys", keyCode);
				if (player instanceof EntityPlayerMP) {
					PacketDispatcher.sendPacketToPlayer(getPacket(), (Player) player);
				}
			}
		}
	}

	@Override
	String getChannel() {
		return "MagicWandKey";
	}

	@Override
	void read(DataInput in) throws IOException {
		super.read(in);
		this.keyCode = in.readInt();
	}

	@Override
	void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(keyCode);
	}
}
