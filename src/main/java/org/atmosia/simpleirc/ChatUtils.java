package org.atmosia.simpleirc;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.client.Minecraft;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.atmosia.simpleirc.irc.IRCMessageHandler;

public enum ChatUtils{
	; // <-- WHAT THE FUCK?????
    // I just tried to remove it and got 34 compilation errors.
    // I cannot describe my fucking confusion - FJSAGS
	
	private static final Minecraft MCInstance = Minecraft.getInstance();
    private static MiniMessage mm = MiniMessage.miniMessage();
	public static void message(Component message)
	{
        try {
            Minecraft.getInstance().execute(() -> MCInstance.player.sendMessage(message));
        } catch (Exception e) {
            MainClient.LOGGER.warn("Exception while displaying message: ", e);
        }
	}
	
	public static String getUsername()
	{
		return MCInstance.player.getName().getString();
	}

    public static void Error(Exception e) {
        String message = ReplaceFormatting(e.getMessage());
        Component parsedMsg = mm.deserialize("<red>[ERR] " + message + "</red>");
        message(parsedMsg);
    }
    public static void Error(String message) {
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<red>[ERR] " + message + "</red>");
        message(parsedMsg);
    }
    public static void Info(String message) {
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<gray>[INFO] " + message + "</gray>");
        message(parsedMsg);
    }
    public static void Warn(String message) {
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<#FFA500>[WARN] " + message + "</#FFA500>");
        message(parsedMsg);
    }
    public static void RawOut(String message) {
        message = ReplaceFormatting(message);
        if (MainClient.irc.Verbosity != IrcVerbosity.RAW) return;
        Component parsedMsg = mm.deserialize("<yellow>[RAW =>] " + message + "</yellow>");
        message(parsedMsg);
    }
    public static void RawIn(String message) {
        message = ReplaceFormatting(message);
        if (MainClient.irc.Verbosity != IrcVerbosity.RAW && MainClient.irc.Verbosity != IrcVerbosity.DEBUG) return;
        Component parsedMsg = mm.deserialize("<dark_gray>[RAW <=]</dark_gray> <gray>" + message + "</gray>");
        message(parsedMsg);
    }
    public static void Success(String message) {
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<green>[INFO] " + message + "</green>");
        message(parsedMsg);
    }
    public static void Notify(String message) {
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<blue><bold>[INFO] " + message + "</bold></blue>");
        message(parsedMsg);

    }
    public static void Verbose(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.VERBOSE &&
        MainClient.irc.Verbosity != IrcVerbosity.RAW) return;
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<gray>[VERB] " + message + "</gray>");
        message(parsedMsg);
    }

    public static void Debug(String message) {
        if (MainClient.irc.Verbosity != IrcVerbosity.DEBUG) return;
        message = ReplaceFormatting(message);
        Component parsedMsg = mm.deserialize("<#00FFFF>[DEBUG] " + message + "</#00FFFF>");
        message(parsedMsg);
    }

    public static class IRCMessageTemplates {
        public static void Notice(String source, String channel, String content) {
            // Target:
            // &9[Channel]&r | &6[NOTICE]&r <&[Source]&r> [Contents]
            source = ReplaceFormatting(source);
            channel = ReplaceFormatting(channel);
            content = ReplaceFormatting(content);

            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>[NOTICE]</gold> | <<red>"
                    + IRCMessageHandler.GetFormattedUser(source, channel) + "</red>> " + content);
            message(parsedMsg);
        }
        public static void Message(String source, String channel, String content) {
            // #ss15 | <FJSAGS_Web> i need this for testing and understanding of IRC protocol...
            // §9#ss15§r | <§cFJSAGS_Web§r> i need this for testing and understanding of IRC protocol...

            source = ReplaceFormatting(source);
            channel = ReplaceFormatting(channel);
            content = ReplaceFormatting(content);

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
            content = ReplaceFormatting(content);
            channel = ReplaceFormatting(channel);
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>Topic</gold>: " + content);
            message(parsedMsg);
        }
        public static void TopicSetBy(String channel, String content) {
            content = ReplaceFormatting(content);
            channel = ReplaceFormatting(channel);
            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <gold>Topic set by</gold>: "
                    + IRCMessageHandler.GetFormattedUser(content, channel));
            message(parsedMsg);
        }
        public static void Join(String source, String channel) {
            source = ReplaceFormatting(source);
            channel = ReplaceFormatting(channel);

            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <green>+</green> " + IRCMessageHandler.GetFormattedUser(source, channel));
            message(parsedMsg);
        }
        public static void Part(String source, String channel, String reason) {
            source = ReplaceFormatting(source);
            channel = ReplaceFormatting(channel);
            reason = ReplaceFormatting(reason);

            Component parsedMsg = mm.deserialize("<blue>" + channel + "</blue> | <red>-</red> " + IRCMessageHandler.GetFormattedUser(source, channel) + " - <gold>" + reason + "</gold>");
            message(parsedMsg);
        }
        public static void Quit(String person, String content) {
            content = ReplaceFormatting(content);
            person = ReplaceFormatting(person);

            Component parsedMsg = mm.deserialize("<red>" + person + "</red> | <gold>Quitting: " + content + "</gold> ");
            message(parsedMsg);
        }
    }

    public static String ReplaceFormatting(String content) {

        return content.replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§k", "<obf>")
                .replace("§l", "<b>")
                .replace("§m", "<st>")
                .replace("§n", "<u>")
                .replace("§o", "<i>")
                .replace("§r", "<reset>");
    }
}
