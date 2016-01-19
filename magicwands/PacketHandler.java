package magicwands;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public final class PacketHandler {
    public static final String CHANNEL = "MagicWands:Key";

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
    }

    private void handlePacket(final FMLProxyPacket packet, final EntityPlayer player) {
        if (packet.channel().equals(CHANNEL)) {
            final MagicWandPacket pkt = new WandKeyPacket();
            MagicWands.proxy.scheduleTask(new Runnable(){
                @Override
                public void run(){
                    pkt.fromBytes(packet.payload());
                    pkt.run(player);
                }
            });
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        handlePacket(event.packet, MagicWands.proxy.getPlayer());
    }
}
