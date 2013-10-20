package magicwands;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

public abstract class MagicWandPacket {
	int entityId;

	public final Packet getPacket() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bos);
		try {
			write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Packet250CustomPayload(getChannel(), bos.toByteArray());
	}

	abstract void execute(Entity entity);

	abstract String getChannel();

	void read(DataInput in) throws IOException {
		entityId = in.readInt();
	}

	void run(EntityPlayer player) {
		execute(player.worldObj.getEntityByID(entityId));
	}

	void write(DataOutput out) throws IOException {
		out.writeInt(entityId);
	}
}
