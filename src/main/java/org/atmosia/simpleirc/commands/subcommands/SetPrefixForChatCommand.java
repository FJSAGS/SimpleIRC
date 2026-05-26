package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.atmosia.simpleirc.ChatUtils;
import org.atmosia.simpleirc.MainClient;
import org.atmosia.simpleirc.commands.suggestions.ConnectedChannelsAndMcSuggester;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class SetPrefixForChatCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command.then(ClientCommands.literal("channelprefix")
                .then(argument("[channel] [prefix]", StringArgumentType.greedyString())
                        .suggests(new ConnectedChannelsAndMcSuggester())
                                .executes(SetPrefixForChatCommand::SetPrefix)))
                .then(ClientCommands.literal("defaultchannel")
                        .then(argument("channel", StringArgumentType.greedyString())
                                .suggests(new ConnectedChannelsAndMcSuggester())
                                .executes(SetPrefixForChatCommand::SetDefaultChannel)));
        return command;
    }
    private static int SetPrefix(CommandContext<FabricClientCommandSource> context) {
        ChatUtils.RawOut("setting prefix command invoked");
        if (MainClient.irc == null || !MainClient.irc.isConnected()) {
            context.getSource().sendError(Component.literal("You are not connected to IRC server."));
            return -1;
        }
        var args = context.getArgument("[channel] [prefix]", String.class);
        var channel = args.split(" ")[0];
        var prefix = args.split(" ")[1];
        if (prefix.length() != 1) {
            context.getSource().sendError(Component.literal("Prefix must be a single character."));
            return -1;
        }
        if (MainClient.irc.GetChannel(channel) == null && !channel.equals("#minecraft_chat")) {
            context.getSource().sendError(Component.literal("You aren't connected to such channel."));
            return -1;
        }

        Character prefixChar = prefix.charAt(0);
        if (channel.equals("#minecraft_chat")) {
            MainClient.MinecraftChatPrefix = prefixChar;
            ChatUtils.Notify("Added prefix <gray>" + prefixChar + "</gray> to channel <gray>" + channel + "</gray>");
            return 1;
        }
        MainClient.irc.AddChannelByPrefix(prefixChar, MainClient.irc.GetChannel(channel));

        ChatUtils.Notify("Added prefix <gray>" + prefixChar + "</gray> to channel <gray>" + channel + "</gray>");

        return 0;
    }
    private static int SetDefaultChannel(CommandContext<FabricClientCommandSource> context) {
        if (MainClient.irc == null || !MainClient.irc.isConnected()) {
            context.getSource().sendError(Component.literal("You are not connected to IRC server."));
            return -1;
        }
        var channel = context.getArgument("channel", String.class);
        if (MainClient.irc.GetChannel(channel) == null && !channel.equals("#minecraft_chat")) {
            context.getSource().sendError(Component.literal("You aren't connected to such channel."));
            return -1;
        }
        else if (channel.equals("#minecraft_chat")) {
            MainClient.DefaultToMinecraftChat = true;
        } else {
            MainClient.DefaultToMinecraftChat = false;
            MainClient.irc.SetPrimaryChannel(channel);
        }
        ChatUtils.Notify("Set channel <gray>" + channel + "</gray> as the primary channel.");
        return 0;
    }
}
