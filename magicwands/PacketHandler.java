package magicwands;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;

public class PacketHandler {
    public static final String CHANNEL = "MagicWands:Key";
    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        event.reply = handlePacket(event.packet, ((NetHandlerPlayServer)event.handler).playerEntity);
    }

    private FMLProxyPacket handlePacket(FMLProxyPacket packet, EntityPlayer player) {
        MagicWandPacket pkt = null;
        if (packet.channel().equals(CHANNEL)) {
            pkt = new WandKeyPacket();
        }
        if(pkt!=null){
            pkt.fromBytes(packet.payload());
            FMLProxyPacket result =  pkt.run(player);
            if(result!=null){
                result.setDispatcher(packet.getDispatcher());
            }
            return result;
        }
        return null;
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event){
        handlePacket(event.packet, getPlayer());
    }

    @SideOnly(Side.CLIENT)
    public static EntityPlayer getPlayer(){
        return FMLClientHandler.instance().getClient().thePlayer;
    }
}
