package magicwands;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class WandKeyHandler extends KeyHandler {
	public static final boolean[] bools = { false, false, false, false };
	public static final KeyBinding help = new KeyBinding("Wand_Help", Keyboard.KEY_LCONTROL);
	public static final KeyBinding key_1 = new KeyBinding("Wand_Key_1", Keyboard.KEY_X);
	public static final KeyBinding key_2 = new KeyBinding("Wand_Key_2", Keyboard.KEY_C);
	public static final KeyBinding key_3 = new KeyBinding("Wand_Key_3", Keyboard.KEY_V);

	public WandKeyHandler() {
		super(new KeyBinding[] { key_1, key_2, key_3, help }, bools);
	}

	@Override
	public String getLabel() {
		return "MagicWandsKeys";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean end, boolean isRepeat) {
		if (end) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
				if (kb.keyDescription.equals(help.keyDescription)) {
					printHelp(player, (WandItem) player.getCurrentEquippedItem().getItem());
				}
				int keys = 0;
				if (kb.keyDescription.equals(key_1.keyDescription)) {
					keys = 100 + (key_2.isPressed() ? 10 : 0) + (key_3.isPressed() ? 1 : 0);
				} else if (kb.keyDescription.equals(key_2.keyDescription)) {
					keys = 10 + (key_1.isPressed() ? 100 : 0) + (key_3.isPressed() ? 1 : 0);
				} else if (kb.keyDescription.equals(key_3.keyDescription)) {
					keys = 1 + (key_1.isPressed() ? 100 : 0) + (key_2.isPressed() ? 10 : 0);
				}
				PacketDispatcher.sendPacketToServer(new WandKeyPacket(player.entityId, keys).getPacket());
			}
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean end) {
		if (end) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
				int keys = 0;
				if (kb.keyDescription.equals(key_1.keyDescription)) {
					keys = (key_2.isPressed() ? 10 : 0) + (key_3.isPressed() ? 1 : 0);
				} else if (kb.keyDescription.equals(key_2.keyDescription)) {
					keys = (key_1.isPressed() ? 100 : 0) + (key_3.isPressed() ? 1 : 0);
				} else if (kb.keyDescription.equals(key_3.keyDescription)) {
					keys = (key_1.isPressed() ? 100 : 0) + (key_2.isPressed() ? 10 : 0);
				}
				PacketDispatcher.sendPacketToServer(new WandKeyPacket(player.entityId, keys).getPacket());
			}
		}
	}

	// control - help
	public void printHelp(EntityPlayer player, WandItem wand) {
		if (wand instanceof BuildWand) {
			player.addChatMessage("=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rbuwand.name" : "item.buwand.name") + " ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - " + StatCollector.translateToLocal("help.build.key1"));
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - " + StatCollector.translateToLocal("help.build.key2"));
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - " + StatCollector.translateToLocal("help.build.key3"));
			if (wand.reinforced) {
				player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + " - " + StatCollector.translateToLocal("help.rbuild.key1.2"));
				player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - "
						+ StatCollector.translateToLocal("help.rbuild.key1.2.3"));
			}
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - " + StatCollector.translateToLocal("help.build.key1.3"));
			if (wand.reinforced) {
				player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - " + StatCollector.translateToLocal("help.rbuild.key2.3"));
			}
		} else if (wand instanceof BreakWand) {
			player.addChatMessage("=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rbrwand.name" : "item.brwand.name") + " ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - " + StatCollector.translateToLocal("help.break.key1"));
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - " + StatCollector.translateToLocal("help.break.key2"));
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - " + StatCollector.translateToLocal("help.break.key3"));
		} else if (wand instanceof MineWand) {
			player.addChatMessage("=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rmiwand.name" : "item.miwand.name") + " ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - " + StatCollector.translateToLocal("help.mine.key1"));
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - " + StatCollector.translateToLocal("help.mine.key2"));
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - " + StatCollector.translateToLocal("help.mine.key3"));
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + " - " + StatCollector.translateToLocal("help.mine.key1.2"));
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}
}
