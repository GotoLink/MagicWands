package magicwands;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "MagicWands", name = "MagicWands", version = "1.1.0")
public class MagicWands {
	public static boolean bedrock, disableNotify, free, obsidian;
	public static boolean[] allow = new boolean[3];
	public static Item Break, Build, Mine, rBreak, rBuild, rMine;
	public static String[] ores, spare;
	public static final CreativeTabs wands = new CreativeTabs("MagicWands") {
        @Override
        public Item getTabIconItem() {
            return MagicWands.Break;
        }
    };
    public static FMLEventChannel channel;

    @EventHandler
	public void load(FMLInitializationEvent event) {
        if (allow[0]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Build), "pGp", "pGp", "pGp", 'p', "plankWood", 'G', Items.gold_ingot));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rBuild), "oGo", "oGo", "oGo", 'o', Blocks.obsidian, 'G', Blocks.gold_block));
        }
        if (allow[1]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Break), "pIp", "pIp", "pIp", 'p', "plankWood", 'I', Items.iron_ingot));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rBreak), "oIo", "oIo", "oIo", 'o', Blocks.obsidian, 'I', Blocks.iron_block));
        }
        if (allow[2]) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Mine), "pDp", "pDp", "pDp", 'p', "plankWood", 'D', Items.diamond));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rMine), "oDo", "oDo", "oDo", 'o', Blocks.obsidian, 'D', Blocks.diamond_block));
        }
		// parsing ore list
		if (spare.length > 0) {
			for (String u : spare) {
				try {
					WandItem.ores.add(GameData.blockRegistry.getObject(u));
				} catch (Exception q) {
				}
			}
		}
		if (ores.length > 0) {
			for (String u : ores) {
				try {
					WandItem.m_ores.add(GameData.blockRegistry.getObject(u));
				} catch (Exception q) {
				}
			}
		}
		if (event.getSide().isClient()) {
			registerClientSideThings();
		}
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL);
        channel.register(new PacketHandler());
	}

	@EventHandler
	public void preload(FMLPreInitializationEvent event) {
		Configuration conf = new Configuration(event.getSuggestedConfigurationFile());
		conf.load();
		int i = 0;
		for (String s : new String[] { "Building_Wands", "Breaking_Wands", "Mining_Wands" }) {
			allow[i] = conf.get("Safety", "Enable_" + s, true).getBoolean(false);
			i++;
		}
		bedrock = conf.get("Cheats", "Enable_bedrock_breaking", false).getBoolean(false);
		obsidian = conf.get("Cheats", "Enable_obsidian_easy_mining", false).getBoolean(false);
		disableNotify = conf.get("Cheats", "Enable_fast_mode", false).getBoolean(false);
		free = conf.get("Cheats", "Enable_free_build_mode", false).getBoolean(false);
		spare = conf.get("BLOCKS", "Destructive_wand_spared_ores", "gold_ore,iron_ore,coal_ore,lapis_ore,mossy_cobblestone,mob_spawner,chest,diamond_ore,redstone_ore,lit_redstone_ore").getString().split(",");
		ores = conf.get("BLOCKS", "Mining_wand_ores_for_surface_mining", "gold_ore,iron_ore,coal_ore,lapis_ore,diamond_ore,redstone_ore,lit_redstone_ore").getString().split(",");
		if (conf.hasChanged()) {
			conf.save();
		}
        if (allow[0]) {
            Build = new BuildWand(false).setUnlocalizedName("buwand").setTextureName("MagicWands:wandbuild");
            rBuild = new BuildWand(true).setUnlocalizedName("rbuwand").setTextureName("MagicWands:rwandbuild");
            GameRegistry.registerItem(Build, "BuildWand");
            GameRegistry.registerItem(rBuild, "RBuildWand");
        }
        if (allow[1]) {
            Break = new BreakWand(false).setUnlocalizedName("brwand").setTextureName("MagicWands:wandbreak");
            rBreak = new BreakWand(true).setUnlocalizedName("rbrwand").setTextureName("MagicWands:rwandbreak");
            GameRegistry.registerItem(Break, "BreakWand");
            GameRegistry.registerItem(rBreak, "RBreakWand");
        }
        if (allow[2]) {
            Mine = new MineWand(false).setUnlocalizedName("miwand").setTextureName("MagicWands:wandmine");
            rMine = new MineWand(true).setUnlocalizedName("rmiwand").setTextureName("MagicWands:rwandmine");
            GameRegistry.registerItem(Mine, "MineWand");
            GameRegistry.registerItem(rMine, "RMineWand");
        }
    }

	@SideOnly(Side.CLIENT)
	private void registerClientSideThings() {
		FMLCommonHandler.instance().bus().register(new WandKeyHandler());
	}
}
