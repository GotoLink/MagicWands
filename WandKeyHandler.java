package magicwands;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

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
			player.addChatMessage("=== " + (wand.reinforced ? "Reinforced " : "") + "Building Wand ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - FULL box");
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - EMPTY room");
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - FRAME (good for fence)");
			if (wand.reinforced) {
				player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + " - building WATER");
			}
			if (wand.reinforced) {
				player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - building LAVA");
			}
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - TORCHES (anti-monster)");
			if (wand.reinforced) {
				player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + "+" + Keyboard.getKeyName(key_3.keyCode) + " - filling CAVES with stone");
			}
		} else if (wand instanceof BreakWand) {
			player.addChatMessage("=== " + (wand.reinforced ? "Reinforced " : "") + "Breaking Wand ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - break all but ORES");
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - break ALL");
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - remove LIQUIDS (lava, water) and PLANTS");
		} else if (wand instanceof MineWand) {
			player.addChatMessage("=== " + (wand.reinforced ? "Reinforced " : "") + "Mining Wand ===");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + " - mine ALL");
			player.addChatMessage(Keyboard.getKeyName(key_2.keyCode) + " - mine only DIRT, SAND, GRAVEL etc.");
			player.addChatMessage(Keyboard.getKeyName(key_3.keyCode) + " - mine only WOOD");
			player.addChatMessage(Keyboard.getKeyName(key_1.keyCode) + "+" + Keyboard.getKeyName(key_2.keyCode) + " - mining ORES from surface");
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}
}
