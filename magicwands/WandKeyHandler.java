package magicwands;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

public class WandKeyHandler {
	public static KeyBinding help;
	public static KeyBinding key_1;
	public static KeyBinding key_2;
	public static KeyBinding key_3;

	public WandKeyHandler() {
        help = getCompatibleKey("Wand_Help", Keyboard.KEY_LCONTROL, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return (registeredKey.getKeyDescription().contains("help") || registeredKey.getKeyDescription().contains("Help"));
            }
        });
        key_1 = getCompatibleKey("Wand_Key_1", Keyboard.KEY_X, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey!=help && registeredKey.getKeyCategory().contains("item");
            }
        });
        key_2 = getCompatibleKey("Wand_Key_2", Keyboard.KEY_C, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey!=help && registeredKey!=key_1 && registeredKey.getKeyCategory().contains("item");
            }
        });
        key_3 = getCompatibleKey("Wand_Key_3", Keyboard.KEY_V, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey!=help && registeredKey!=key_1 && registeredKey!=key_2 && registeredKey.getKeyCategory().contains("item");
            }
        });
	}

	@SubscribeEvent
	public void keyDown(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
            if (help.getIsKeyPressed()) {
                printHelp(player, (WandItem) player.getCurrentEquippedItem().getItem());
                return;
            }
            int keys = (key_1.getIsKeyPressed()?100:0) + (key_2.getIsKeyPressed() ? 10 : 0) + (key_3.getIsKeyPressed() ? 1 : 0);
            MagicWands.channel.sendToServer(new WandKeyPacket(player.getEntityId(), keys).getPacket(Side.SERVER));
        }
	}

	// control - help
	public void printHelp(EntityPlayer player, WandItem wand) {
		if (wand instanceof BuildWand) {
			addChatMessage(player, "=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rbuwand.name" : "item.buwand.name") + " ===");
			addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + " - " + StatCollector.translateToLocal("help.build.key1"));
			addChatMessage(player, Keyboard.getKeyName(key_2.getKeyCode()) + " - " + StatCollector.translateToLocal("help.build.key2"));
			addChatMessage(player, Keyboard.getKeyName(key_3.getKeyCode()) + " - " + StatCollector.translateToLocal("help.build.key3"));
			if (wand.reinforced) {
				addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + "+" + Keyboard.getKeyName(key_2.getKeyCode()) + " - " + StatCollector.translateToLocal("help.rbuild.key1.2"));
				addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + "+" + Keyboard.getKeyName(key_2.getKeyCode()) + "+" + Keyboard.getKeyName(key_3.getKeyCode()) + " - "
                        + StatCollector.translateToLocal("help.rbuild.key1.2.3"));
			}
			addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + "+" + Keyboard.getKeyName(key_3.getKeyCode()) + " - " + StatCollector.translateToLocal("help.build.key1.3"));
			if (wand.reinforced) {
				addChatMessage(player, Keyboard.getKeyName(key_2.getKeyCode()) + "+" + Keyboard.getKeyName(key_3.getKeyCode()) + " - " + StatCollector.translateToLocal("help.rbuild.key2.3"));
			}
		} else if (wand instanceof BreakWand) {
			addChatMessage(player, "=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rbrwand.name" : "item.brwand.name") + " ===");
			addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + " - " + StatCollector.translateToLocal("help.break.key1"));
			addChatMessage(player, Keyboard.getKeyName(key_2.getKeyCode()) + " - " + StatCollector.translateToLocal("help.break.key2"));
			addChatMessage(player, Keyboard.getKeyName(key_3.getKeyCode()) + " - " + StatCollector.translateToLocal("help.break.key3"));
		} else if (wand instanceof MineWand) {
			addChatMessage(player, "=== " + StatCollector.translateToLocal(wand.reinforced ? "item.rmiwand.name" : "item.miwand.name") + " ===");
			addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + " - " + StatCollector.translateToLocal("help.mine.key1"));
			addChatMessage(player, Keyboard.getKeyName(key_2.getKeyCode()) + " - " + StatCollector.translateToLocal("help.mine.key2"));
			addChatMessage(player, Keyboard.getKeyName(key_3.getKeyCode()) + " - " + StatCollector.translateToLocal("help.mine.key3"));
			addChatMessage(player, Keyboard.getKeyName(key_1.getKeyCode()) + "+" + Keyboard.getKeyName(key_2.getKeyCode()) + " - " + StatCollector.translateToLocal("help.mine.key1.2"));
		}
	}

    public static void addChatMessage(EntityPlayer player, String message){
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public static interface KeyMerger{
        public boolean canMergeWith(KeyBinding registeredKey);
    }

    public KeyBinding getCompatibleKey(String name, int keyDefault, String category, KeyMerger merger){
        for(KeyBinding key:Minecraft.getMinecraft().gameSettings.keyBindings){
            if(merger.canMergeWith(key)){
                return key;
            }
        }
        KeyBinding defaultkey = new KeyBinding(name, keyDefault, category);
        ClientRegistry.registerKeyBinding(defaultkey);
        return defaultkey;
    }
}
