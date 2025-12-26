package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atmosia.simpleirc.ChatUtils;
import org.atmosia.simpleirc.MainClient;
import org.atmosia.simpleirc.irc.IRCChannel;
import org.atmosia.simpleirc.commands.suggestions.ConnectedChannelsSuggester;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class IRCBasicCommands {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command.then(ClientCommandManager.literal("join")
                .then(argument("[channel] [password (optional)]", StringArgumentType.greedyString()).executes(IRCBasicCommands::Join)))
                .then(ClientCommandManager.literal("part")
                        .then(argument("channel", StringArgumentType.string())
                                .suggests(new ConnectedChannelsSuggester())
                                .executes(IRCBasicCommands::Part)))
                .then(ClientCommandManager.literal("raw")
                        .then(argument("command", StringArgumentType.greedyString()).executes(IRCBasicCommands::Raw))
                );
        return command;
    }

    private static int Part(CommandContext<FabricClientCommandSource> context) {
        if (MainClient.irc == null || !MainClient.irc.isConnected()) {
            ChatUtils.Error("Error on parting: You are not connected to IRC server.");
            return -1;
        }
        var channel = context.getArgument("channel", String.class);
        if (channel.isEmpty()) {
            ChatUtils.Error("Error on parting: Please specify a channel.");
        }
        var ircChannel = MainClient.irc.GetChannel(channel);
        MainClient.irc.SendLine(ircChannel.Part("Leaving"));
        return 0;
    }

    private static int Raw(CommandContext<FabricClientCommandSource> context) {
        MainClient.irc.SendLine(context.getArgument("command", String.class));
        return 0;
    }
    private static int Join(CommandContext<FabricClientCommandSource> context) {

        if (MainClient.irc == null || !MainClient.irc.isConnected()) {
            ChatUtils.Error("Error on joining: You are not connected to IRC server.");
            return -1;
        }
        var arguments = context.getArgument("[channel] [password (optional)]", String.class);
        var splits = arguments.split(" ");
        var channel = splits[0].trim();
        var password = splits.length > 1 ? splits[1] : "";
        MainClient.irc.AddChannel(new IRCChannel(channel, password, false), true);
        return 0;
    }
}
