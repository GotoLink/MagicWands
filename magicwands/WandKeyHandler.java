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
import org.lwjgl.input.Mouse;

public final class WandKeyHandler {
    public static final WandKeyHandler INSTANCE = new WandKeyHandler();
    private final KeyBinding help, key_1, key_2, key_3;
    private String FIRST_KEY_NAME, SECOND_KEY_NAME, THIRD_KEY_NAME;

    private WandKeyHandler() {
        help = getCompatibleKey("Wand_Help", Keyboard.KEY_LCONTROL, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return (registeredKey.getKeyDescription().contains("help") || registeredKey.getKeyDescription().contains("Help"));
            }
        });
        key_1 = getCompatibleKey("Wand_Key_1", Keyboard.KEY_X, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey != help && registeredKey.getKeyCategory().contains("item");
            }
        });
        key_2 = getCompatibleKey("Wand_Key_2", Keyboard.KEY_C, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey != help && registeredKey != key_1 && registeredKey.getKeyCategory().contains("item");
            }
        });
        key_3 = getCompatibleKey("Wand_Key_3", Keyboard.KEY_V, "key.categories.item.special", new KeyMerger() {
            @Override
            public boolean canMergeWith(KeyBinding registeredKey) {
                return registeredKey != help && registeredKey != key_1 && registeredKey != key_2 && registeredKey.getKeyCategory().contains("item");
            }
        });
        nameKeys();
    }

    private void nameKeys() {
        FIRST_KEY_NAME = getKeyName(key_1.getKeyCode());
        SECOND_KEY_NAME = getKeyName(key_2.getKeyCode());
        THIRD_KEY_NAME = getKeyName(key_3.getKeyCode());
    }

    private String getKeyName(int code) {
        return code < 0 ? Mouse.getButtonName(code + 100) : Keyboard.getKeyName(code);
    }

    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof WandItem) {
            if (help.getIsKeyPressed()) {
                printHelp(player, (WandItem) player.getCurrentEquippedItem().getItem());
                return;
            }
            int keys = (key_1.getIsKeyPressed() ? 100 : 0) + (key_2.getIsKeyPressed() ? 10 : 0) + (key_3.getIsKeyPressed() ? 1 : 0);
            MagicWands.channel.sendToServer(new WandKeyPacket(player.getEntityId(), keys).getPacket(Side.SERVER));
        }
    }

    // control - help
    public void printHelp(EntityPlayer player, WandItem wand) {
        addChatMessage(player, "=== " + StatCollector.translateToLocal(wand.getUnlocalizedName() + ".name") + " ===");
        nameKeys();
        if (wand instanceof BuildWand) {
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.build.key1", FIRST_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.build.key2", SECOND_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.build.key3", THIRD_KEY_NAME));
            if (wand.reinforced) {
                addChatMessage(player, FIRST_KEY_NAME + "+" + SECOND_KEY_NAME + " - " + StatCollector.translateToLocal("help.rbuild.key1.2"));
                addChatMessage(player, FIRST_KEY_NAME + "+" + SECOND_KEY_NAME + "+" + THIRD_KEY_NAME + " - "
                        + StatCollector.translateToLocal("help.rbuild.key1.2.3"));
            }
            addChatMessage(player, FIRST_KEY_NAME + "+" + THIRD_KEY_NAME + " - " + StatCollector.translateToLocal("help.build.key1.3"));
            if (wand.reinforced) {
                addChatMessage(player, SECOND_KEY_NAME + "+" + THIRD_KEY_NAME + " - " + StatCollector.translateToLocal("help.rbuild.key2.3"));
            }
        } else if (wand instanceof BreakWand) {
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.break.key1", FIRST_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.break.key2", SECOND_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.break.key3", THIRD_KEY_NAME));
        } else if (wand instanceof MineWand) {
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.mine.key1", FIRST_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.mine.key2", SECOND_KEY_NAME));
            addChatMessage(player, StatCollector.translateToLocalFormatted("help.mine.key3", THIRD_KEY_NAME));
            addChatMessage(player, FIRST_KEY_NAME + "+" + SECOND_KEY_NAME + " - " + StatCollector.translateToLocal("help.mine.key1.2"));
        }
    }

    public static void addChatMessage(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public static interface KeyMerger {
        public boolean canMergeWith(KeyBinding registeredKey);
    }

    public KeyBinding getCompatibleKey(String name, int keyDefault, String category, KeyMerger merger) {
        for (KeyBinding key : Minecraft.getMinecraft().gameSettings.keyBindings) {
            if (merger.canMergeWith(key)) {
                return key;
            }
        }
        KeyBinding defaultkey = new KeyBinding(name, keyDefault, category);
        ClientRegistry.registerKeyBinding(defaultkey);
        return defaultkey;
    }
}
