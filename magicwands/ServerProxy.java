package magicwands;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Olivier on 30/11/2014.
 */
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
}
