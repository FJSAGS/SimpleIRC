package org.atmosia.simpleirc;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.client.MinecraftClient;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.atmosia.simpleirc.irc.IRCMessageHandler;

public enum ChatUtils{
	; // <-- WHAT THE FUCK?????
    // I just tried to remove it and got 34 compilation errors.
    // I cannot describe my fucking confusion - FJSAGS
	
	private static final MinecraftClient MCInstance = MinecraftClient.getInstance();
    private static MiniMessage mm = MiniMessage.miniMessage();
	public static void message(Component message)
	{
		Audience player = MCInstance.player;
        try {
            player.sendMessage(message);
        } catch (Exception e) {
            Main.LOGGER.warn("Exception while displaying message: ", e);
        }
	}
	
	public static String getUsername()
	{
		return MCInstance.player.getName().getString();
	}

    public static void Error(Exception e) {
        Component parsedMsg = mm.deserialize("<red>[ERR] " + e.getMessage() + "</red>");
        message(parsedMsg);
    }
    public static void Error(String message) {
        Component parsedMsg = mm.deserialize("<red>[ERR] " + message + "</red>");
        message(parsedMsg);
    }
    public static void Info(String message) {
        Component parsedMsg = mm.deserialize("<gray>[INFO] " + message + "</gray>");
        message(parsedMsg);
    }
    public static void Warn(String message) {
        Component parsedMsg = mm.deserialize("<#FFA500>[WARN] " + message + "</#FFA500>");
        message(parsedMsg);
    }
    public static void RawOut(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.RAW) return;
        Component parsedMsg = mm.deserialize("<yellow>[RAW =>] " + message + "</yellow>");
        message(parsedMsg);
    }
    public static void RawIn(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.RAW && MainClient.irc.Verbosity != IrcVerbosity.DEBUG) return;
        Component parsedMsg = mm.deserialize("<dark_gray>[RAW <=]</dark_gray> <gray>" + message + "</gray>");
        message(parsedMsg);
    }
    public static void Success(String message) {
        Component parsedMsg = mm.deserialize("<green>[INFO] " + message + "</green>");
        message(parsedMsg);
    }
    public static void Notify(String message) {
        Component parsedMsg = mm.deserialize("<blue>[INFO] " + message + "</blue>");
        message(parsedMsg);

    }
    public static void Verbose(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.VERBOSE &&
        MainClient.irc.Verbosity != IrcVerbosity.RAW) return;
        Component parsedMsg = mm.deserialize("<gray>[VERB] " + message + "</gray>");
        message(parsedMsg);
    }

    public static void Debug(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.DEBUG) return;
        Component parsedMsg = mm.deserialize("<#00FFFF>[DEBUG] " + message + "</#00FFFF>");
        message(parsedMsg);
    }

    public static class IRCMessageTemplates {
        public static void Notice(String source, String channel, String content) {
            // Target:
            // &9[Channel]&r | &6[NOTICE]&r <&[Source]&r> [Contents]
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>[NOTICE]</gold> | <<red>"
                    + IRCMessageHandler.GetFormattedUser(source, channel) + "</red>> " + content);
            message(parsedMsg);
        }
        public static void Message(String source, String channel, String content) {
            // #ss15 | <FJSAGS_Web> i need this for testing and understanding of IRC protocol...
            // §9#ss15§r | <§cFJSAGS_Web§r> i need this for testing and understanding of IRC protocol...
            if (channel.startsWith("#")) {
                Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <<red>"
                        + IRCMessageHandler.GetFormattedUser(source, channel) + "</red>> " + content);
                message(parsedMsg);
            }
            else {
                Component parsedMsg = mm.deserialize("<gold>" + channel + " [PM]</gold> | <<red>" + source + "</red>> " + content);
                message(parsedMsg);
            }
        }
        public static void Topic(String channel, String content) {
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>Topic</gold>: " + content);
            message(parsedMsg);
        }
        public static void TopicSetBy(String channel, String content) {
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>Topic set by</gold>: "
                    + IRCMessageHandler.GetFormattedUser(content, channel));
            message(parsedMsg);
        }
        public static void Join(String source, String channel) {
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <green>+</green> " + IRCMessageHandler.GetFormattedUser(source, channel));
            message(parsedMsg);
        }
        public static void Part(String source, String channel, String reason) {
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <red>-</red> " + IRCMessageHandler.GetFormattedUser(source, channel));
            message(parsedMsg);
        }
        public static void Quit(String person, String content) {
            Component parsedMsg = mm.deserialize("<red>" + person + "</red> | <gold>Quitting: " + content + "</gold> ");
            message(parsedMsg);
        }
    }
}
