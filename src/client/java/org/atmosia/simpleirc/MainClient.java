package org.atmosia.simpleirc;

import net.minecraft.util.Identifier;
import org.atmosia.simpleirc.commands.SimpleIrcCommand;
import org.atmosia.simpleirc.irc.IRCNetwork;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.Objects;

public class MainClient implements ClientModInitializer {	
	public static IRCNetwork irc = null;
    public static Boolean DefaultToMinecraftChat = false;
    public static Character MinecraftChatPrefix = '!';
    public static KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("simpleirc"));
	
	private static KeyBinding KBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    	    "key.simpleirc.toggleconnect", // translation key
    	    InputUtil.Type.KEYSYM, // type
    	    GLFW.GLFW_KEY_K, // keycode
    	    cat // category key
    	));

    private static KeyBinding Ibind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.simpleirc.togglemcchat", // translation key
            InputUtil.Type.KEYSYM, // type
            GLFW.GLFW_KEY_I, // keycode
            cat // category key
    ));
	@Override
	public void onInitializeClient() {
		/*if (FabricLoader.getInstance().isModLoaded("cloth-config2")) 
        {
           ConfigScreenBuilder.setMain(Main.MOD_ID, new ClothConfigScreenBuilder());
        }*/

        Main.settings.subscribeToBackupnick((value) -> {
            irc.SetBackupNickname(value);
        });
        Main.settings.subscribeToIp((value) -> {
            irc.SetIp(value);
            ChatUtils.Notify("The IP in config has changed. <click:run_command:/simpleirc connect>Click here if you want to reconnect using new settings</click>");
        });
        Main.settings.subscribeToPort((value) -> {
            irc.SetPort(value);
            ChatUtils.Notify("The port in config has changed. <click:run_command:/simpleirc connect>Click here if you want to reconnect using new settings</click>");
        });
        Main.settings.subscribeToVerbosity((value) -> {
            irc.SetVerbosity(value);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(Ibind.wasPressed())
            {
                if (DefaultToMinecraftChat){
                    DefaultToMinecraftChat = false;
                    ChatUtils.Notify("Default chat is now irc");
                }else{
                    DefaultToMinecraftChat = true;
                    ChatUtils.Notify("Default chat is now minecraft chat");
                }
            }




            while(KBind.wasPressed())
            {
            	if(irc==null || !irc.isConnected())
            	{
            		Connect();
            	}
            	else
            	{
            		Disconnect("Disconnected via command");
            	}
            }
        });
        SimpleIrcCommand.Register();

	}
    public static void Connect() {
        MainClient.irc = new IRCNetwork(Main.settings.ip(), Main.settings.port(), ChatUtils.getUsername(),
                Main.settings.backupnick(), Main.settings.verbosity());
        try {
            MainClient.irc.Connect();
        } catch (IllegalStateException e) {
            ChatUtils.Error("Failed to connect to IRC:");
            ChatUtils.Error(e);
        }
    }
    public static void Disconnect(String reason) {
        if (Objects.equals(reason, "")) {
            reason = "Leaving";
        }
        MainClient.irc.Disconnect(reason);
    }
}
