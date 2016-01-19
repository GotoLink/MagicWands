package magicwands;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;

public class ClientProxy implements MagicWands.IProxy {
    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().thePlayer;
    }

    @Override
    public void register() {
        ItemModelMesher mesher = FMLClientHandler.instance().getClient().getRenderItem().getItemModelMesher();
        mesher.register(MagicWands.Break, 0, wand("Break"));
        mesher.register(MagicWands.Build, 0, wand("Build"));
        mesher.register(MagicWands.Mine, 0, wand("Mine"));
        mesher.register(MagicWands.rBreak, 0, wand("RBreak"));
        mesher.register(MagicWands.rBuild, 0, wand("RBuild"));
        mesher.register(MagicWands.rMine, 0, wand("RMine"));
        FMLCommonHandler.instance().bus().register(WandKeyHandler.INSTANCE);
    }

    private ModelResourceLocation wand(String name){
        return new ModelResourceLocation("magicwands:" + name + "Wand", "inventory");
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

    @Override
    public void scheduleTask(Runnable runner){
        FMLClientHandler.instance().getClient().addScheduledTask(runner);
    }
}
