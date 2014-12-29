package magicwands;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = "MagicWands", name = "MagicWands", version = "${version}")
public final class MagicWands {
    @Mod.Instance("MagicWands")
    public static MagicWands INSTANCE;
    @SidedProxy(modId = "MagicWands", clientSide = "magicwands.ClientProxy", serverSide = "magicwands.ServerProxy")
    public static IProxy proxy;
    public static boolean bedrock, disableNotify, free, obsidian;
    public static boolean[] allow = new boolean[3];
    public static boolean[] recipe = new boolean[3];
    public static Item Break, Build, Mine, rBreak, rBuild, rMine;
    public static String[] ores, spare;
    public static CreativeTabs wands = null;
    public static FMLEventChannel channel;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        if (allow[0] && recipe[0]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Build), "pGp", "pGp", "pGp", 'p', "plankWood", 'G', "ingotGold"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rBuild), "oGo", "oGo", "oGo", 'o', Blocks.obsidian, 'G', "blockGold"));
        }
        if (allow[1] && recipe[1]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Break), "pIp", "pIp", "pIp", 'p', "plankWood", 'I', "ingotIron"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rBreak), "oIo", "oIo", "oIo", 'o', Blocks.obsidian, 'I', "blockIron"));
        }
        if (allow[2] && recipe[2]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Mine), "pDp", "pDp", "pDp", 'p', "plankWood", 'D', "gemDiamond"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rMine), "oDo", "oDo", "oDo", 'o', Blocks.obsidian, 'D', "blockDiamond"));
        }
        // parsing ore list
        if (spare.length > 0) {
            for (String u : spare) {
                WandItem.addOre(u, false);
            }
        }
        if (ores.length > 0) {
            for (String u : ores) {
                WandItem.addOre(u, true);
            }
        }
        proxy.register();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL);
        channel.register(new PacketHandler());
    }

    @EventHandler
    public void preload(FMLPreInitializationEvent event) {
        Configuration conf = new Configuration(event.getSuggestedConfigurationFile());
        int i = 0;
        for (String s : new String[]{"Building_Wands", "Breaking_Wands", "Mining_Wands"}) {
            allow[i] = conf.get("Safety", "Enable_" + s, true).getBoolean();
            recipe[i] = conf.get("Safety", "Enable_" + s + "_recipe", true).getBoolean();
            i++;
        }
        bedrock = conf.get("Cheats", "Enable_bedrock_breaking", false).getBoolean();
        obsidian = conf.get("Cheats", "Enable_obsidian_easy_mining", false).getBoolean();
        disableNotify = conf.get("Cheats", "Enable_fast_mode", false).getBoolean();
        free = conf.get("Cheats", "Enable_free_build_mode", false).getBoolean();
        spare = conf.get("BLOCKS", "Destructive_wand_spared_ores", "gold_ore,iron_ore,coal_ore,lapis_ore,mossy_cobblestone,mob_spawner,chest,diamond_ore,redstone_ore,lit_redstone_ore").getString().split(",");
        ores = conf.get("BLOCKS", "Mining_wand_ores_for_surface_mining", "gold_ore,iron_ore,coal_ore,lapis_ore,diamond_ore,redstone_ore,lit_redstone_ore").getString().split(",");
        if (conf.hasChanged()) {
            conf.save();
        }
        if (allow[0]) {
            if (wands == null)
                wands = new CreativeTabs("MagicWands") {
                    @Override
                    public Item getTabIconItem() {
                        return MagicWands.Build;
                    }
                };
            Build = new BuildWand(false).setUnlocalizedName("buwand").setTextureName("MagicWands:wandbuild");
            rBuild = new BuildWand(true).setUnlocalizedName("rbuwand").setTextureName("MagicWands:rwandbuild");
            GameRegistry.registerItem(Build, "BuildWand");
            GameRegistry.registerItem(rBuild, "RBuildWand");
        }
        if (allow[1]) {
            if (wands == null)
                wands = new CreativeTabs("MagicWands") {
                    @Override
                    public Item getTabIconItem() {
                        return MagicWands.Break;
                    }
                };
            Break = new BreakWand(false).setUnlocalizedName("brwand").setTextureName("MagicWands:wandbreak");
            rBreak = new BreakWand(true).setUnlocalizedName("rbrwand").setTextureName("MagicWands:rwandbreak");
            GameRegistry.registerItem(Break, "BreakWand");
            GameRegistry.registerItem(rBreak, "RBreakWand");
        }
        if (allow[2]) {
            if (wands == null)
                wands = new CreativeTabs("MagicWands") {
                    @Override
                    public Item getTabIconItem() {
                        return MagicWands.Mine;
                    }
                };
            Mine = new MineWand(false).setUnlocalizedName("miwand").setTextureName("MagicWands:wandmine");
            rMine = new MineWand(true).setUnlocalizedName("rmiwand").setTextureName("MagicWands:rwandmine");
            GameRegistry.registerItem(Mine, "MineWand");
            GameRegistry.registerItem(rMine, "RMineWand");
        }
        if (event.getSourceFile().getName().endsWith(".jar")) {
            proxy.trySendUpdate();
        }
    }

    public interface IProxy {
        public EntityPlayer getPlayer();

        public void register();

        public void trySendUpdate();
    }
}
