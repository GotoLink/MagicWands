package magicwands;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Olivier on 30/11/2014.
 */
public class ClientProxy implements MagicWands.IProxy {
    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().thePlayer;
    }

    @Override
    public void register() {
        FMLCommonHandler.instance().bus().register(WandKeyHandler.INSTANCE);
    }

    @Override
    public void trySendUpdate() {
        try {
            Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                    FMLCommonHandler.instance().findContainerFor(MagicWands.INSTANCE),
                    "https://raw.github.com/GotoLink/MagicWands/master/update.xml",
                    "https://raw.github.com/GotoLink/MagicWands/master/changelog.md"
            );
        } catch (Throwable e) {
        }
    }
}
