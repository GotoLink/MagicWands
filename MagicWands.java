package magicwands;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "MagicWands", name = "MagicWands", version = "1.0.0")
@NetworkMod(clientSideRequired = true, packetHandler = PacketHandler.class, channels = { "MagicWandKey" })
public class MagicWands {
	public static boolean bedrock, disableNotify, free, obsidian;
	public static Item Break, Build, Mine, rBreak, rBuild, rMine;
	public static String[] ores, spare;
	public static CreativeTabs wands = new CreativeTabWands("MagicWands");

	@EventHandler
	public void load(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ItemStack(Build), "pGp", "pGp", "pGp", 'p', Block.planks, 'G', Item.ingotGold);
		GameRegistry.addRecipe(new ItemStack(Break), "pIp", "pIp", "pIp", 'p', Block.planks, 'I', Item.ingotIron);
		GameRegistry.addRecipe(new ItemStack(Mine), "pDp", "pDp", "pDp", 'p', Block.planks, 'D', Item.diamond);
		GameRegistry.addRecipe(new ItemStack(rBuild), "oGo", "oGo", "oGo", 'o', Block.obsidian, 'G', Block.blockGold);
		GameRegistry.addRecipe(new ItemStack(rBreak), "oIo", "oIo", "oIo", 'o', Block.obsidian, 'I', Block.blockIron);
		GameRegistry.addRecipe(new ItemStack(rMine), "oDo", "oDo", "oDo", 'o', Block.obsidian, 'D', Block.blockDiamond);
		// parsing ore list
		if (spare.length > 0) {
			for (String u : spare) {
				try {
					WandItem.ores.add(Integer.parseInt(u));
				} catch (NumberFormatException q) {
				}
			}
		}
		if (ores.length > 0) {
			for (String u : ores) {
				try {
					WandItem.m_ores.add(Integer.parseInt(u));
				} catch (NumberFormatException q) {
				}
			}
		}
		if (event.getSide().isClient()) {
			registerClientSideThings();
		}
	}

	@EventHandler
	public void preload(FMLPreInitializationEvent event) {
		Configuration conf = new Configuration(event.getSuggestedConfigurationFile());
		conf.load();
		Build = new BuildWand(conf.getItem("BuildingWand", 9301).getInt(), false).setUnlocalizedName("buwand").setTextureName("MagicWands:wandbuild");
		Break = new BreakWand(conf.getItem("BreakingWand", 9302).getInt(), false).setUnlocalizedName("brwand").setTextureName("MagicWands:wandbreak");
		Mine = new MineWand(conf.getItem("MiningWand", 9303).getInt(), false).setUnlocalizedName("miwand").setTextureName("MagicWands:wandmine");
		rBuild = new BuildWand(conf.getItem("ReinforcedBuildingWand", 9304).getInt(), true).setUnlocalizedName("rbuwand").setTextureName("MagicWands:rwandbuild");
		rBreak = new BreakWand(conf.getItem("ReinforcedBreakingWand", 9305).getInt(), true).setUnlocalizedName("rbrwand").setTextureName("MagicWands:rwandbreak");
		rMine = new MineWand(conf.getItem("ReinforceMiningWand", 9306).getInt(), true).setUnlocalizedName("rmiwand").setTextureName("MagicWands:rwandmine");
		bedrock = conf.get("Cheats", "Enable_bedrock_breaking", false).getBoolean(false);
		obsidian = conf.get("Cheats", "Enable_obsidian_easy_mining", false).getBoolean(false);
		disableNotify = conf.get("Cheats", "Enable_fast_mode", false).getBoolean(false);
		free = conf.get("Cheats", "Enable_free_build_mode", false).getBoolean(false);
		spare = conf.get("BLOCKS", "Destructive_wand_spared_ores", "14,15,16,21,56,73,74,48,54,52").getString().split(",");
		ores = conf.get("BLOCKS", "Mining_wand_ores_for_surface_mining", "14,15,16,21,56,73,74").getString().split(",");
		if (conf.hasChanged()) {
			conf.save();
		}
	}

	@SideOnly(Side.CLIENT)
	private void registerClientSideThings() {
		LanguageRegistry.addName(Build, "Building Wand");
		LanguageRegistry.addName(Break, "Breaking Wand");
		LanguageRegistry.addName(Mine, "Mining Wand");
		LanguageRegistry.addName(rBuild, "Reinforced Building Wand");
		LanguageRegistry.addName(rBreak, "Reinforced Breaking Wand");
		LanguageRegistry.addName(rMine, "Reinforced Mining Wand");
		KeyBindingRegistry.registerKeyBinding(new WandKeyHandler());
	}
}
