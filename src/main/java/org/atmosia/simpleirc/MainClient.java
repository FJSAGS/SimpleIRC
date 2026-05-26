package org.atmosia.simpleirc;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.atmosia.simpleirc.commands.SimpleIrcCommand;
import org.atmosia.simpleirc.irc.IRCNetwork;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Objects;

public class MainClient implements ClientModInitializer {

    public static boolean autoconnect = true;

    public static final String MOD_ID = "simpleirc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static org.atmosia.simpleirc.SimpleIRCConfig settings = org.atmosia.simpleirc.SimpleIRCConfig.createAndLoad();

	public static IRCNetwork irc = null;
    public static Boolean DefaultToMinecraftChat = false;
    public static Character MinecraftChatPrefix = '!';
    public static KeyMapping.Category cat = KeyMapping.Category.register(Identifier.parse("simpleirc"));
	
	private static KeyMapping KBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
    	    "key.simpleirc.toggleconnect", // translation key
    	    InputConstants.Type.KEYSYM, // type
    	    GLFW.GLFW_KEY_K, // keycode
    	    cat // category key
    	));

    private static KeyMapping Ibind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.simpleirc.togglemcchat", // translation key
            InputConstants.Type.KEYSYM, // type
            GLFW.GLFW_KEY_I, // keycode
            cat // category key
    ));
	@Override
	public void onInitializeClient() {
		/*if (FabricLoader.getInstance().isModLoaded("cloth-config2")) 
        {
           ConfigScreenBuilder.setMain(Main.MOD_ID, new ClothConfigScreenBuilder());
        }*/

        settings.subscribeToBackupnick((value) -> {
            irc.SetBackupNickname(value);
        });
        settings.subscribeToIp((value) -> {
            irc.SetIp(value);
            ChatUtils.Notify("The IP in config has changed. <click:run_command:/simpleirc connect>Click here if you want to reconnect using new settings</click>");
        });
        settings.subscribeToPort((value) -> {
            irc.SetPort(value);
            ChatUtils.Notify("The port in config has changed. <click:run_command:/simpleirc connect>Click here if you want to reconnect using new settings</click>");
        });
        settings.subscribeToVerbosity((value) -> {
            irc.SetVerbosity(value);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(Ibind.consumeClick())
            {
                if (DefaultToMinecraftChat){
                    DefaultToMinecraftChat = false;
                    ChatUtils.Notify("Default chat is now irc");
                }else{
                    DefaultToMinecraftChat = true;
                    ChatUtils.Notify("Default chat is now minecraft chat");
                }
            }




            while(KBind.consumeClick())
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
        irc = new IRCNetwork(settings.ip(), settings.port(), ChatUtils.getUsername(),
                settings.backupnick(), settings.verbosity());
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
