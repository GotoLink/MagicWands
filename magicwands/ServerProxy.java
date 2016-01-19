package magicwands;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class ServerProxy implements MagicWands.IProxy {
    @Override
    public EntityPlayer getPlayer() {
        return null;
    }

    @Override
    public void register() {

    }

    @Override
    public void trySendUpdate() {

    }

    @Override
    public void scheduleTask(Runnable runner){
        MinecraftServer.getServer().addScheduledTask(runner);
    }
}
