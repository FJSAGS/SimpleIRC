package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atmosia.simpleirc.ChatUtils;
import org.atmosia.simpleirc.MainClient;
import org.atmosia.simpleirc.irc.IRCNetwork;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class ConnectCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command.then(ClientCommands.literal("connect").executes(ConnectCommand::Connect)
                .then(argument("server_ip", StringArgumentType.string())
                        .then(argument("server_port", IntegerArgumentType.integer(0, 65535))
                                .executes(ConnectCommand::ConnectWithArgs))
                )
        );
        return command;
    }

    private static int ConnectWithArgs(CommandContext<FabricClientCommandSource> ctx) {
        var server = ctx.getArgument("server_ip", String.class);
        var serverPort = ctx.getArgument("server_port", Integer.class);
        if (MainClient.irc != null && MainClient.irc.isConnected()) {
            MainClient.irc.Disconnect("Reconnecting...");
        }

        MainClient.irc = new IRCNetwork(server, serverPort, ChatUtils.getUsername(), MainClient.settings.backupnick(), MainClient.settings.verbosity());
        MainClient.irc.Connect();
        return 0;
    }


    private static int Connect(CommandContext<FabricClientCommandSource> ctx) {
        if (MainClient.irc != null && MainClient.irc.isConnected()) {
            MainClient.irc.Disconnect("Reconnecting...");
        }
        try {
            MainClient.Connect();
        }
        catch (IllegalStateException e) {
            ChatUtils.Error(e);
        }
        return 0;
    }
}
