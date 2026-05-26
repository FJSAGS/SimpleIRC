package org.atmosia.simpleirc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.commands.CommandBuildContext;
import org.atmosia.simpleirc.ChatUtils;
import org.atmosia.simpleirc.commands.subcommands.*;


public class SimpleIrcCommand {
    public static void Register() {
        ClientCommandRegistrationCallback.EVENT.register(SimpleIrcCommand::RegisterForReal);
    }
    private static void RegisterForReal(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        var command = ClientCommands.literal("simpleirc").executes(SimpleIrcCommand::SendHelp)
                .then(ClientCommands.literal("help").executes(SimpleIrcCommand::SendHelp));
        ConfigCommand.Register(command);
        ConnectCommand.Register(command);
        DisconnectCommand.Register(command);
        StatusCommand.Register(command);
        IRCBasicCommands.Register(command);
        SetPrefixForChatCommand.Register(command);
        dispatcher.register(command);
    }
    private static int SendHelp(CommandContext<FabricClientCommandSource> ctx) {
        Component message = MiniMessage.miniMessage().deserialize(
                """
                        SimpleIRC command help:
                        /simpleirc config [option] - sends the current configuration.
                        /simpleirc config [option] [value] - sets the value of an option.
                        
                        /simpleirc connect - connects you using information contained in config.
                        Alternatively you can use /simpleirc connect [server] [port]
                        
                        /simpleirc disconnect - disconnects you from the server you are connected to.
                        /simpleirc disconnect [reason] - adds a reason to QUIT message.
                        
                        /simpleirc defaultchannel [channel] - sets the channel you send messages to by default. Pick #minecraftchat to send messages outside IRC by default.
                        /simpleirc channelprefix [channel] [prefix] - makes messages starting with [prefix] go to the [channel]
                        
                        /simpleirc join [channel] [password] - joins the channel, optionally with a password.
                        /simpleirc part [channel] - leaves the channel.
                        /simpleirc status - shows the server you are connected to and their prefixes.
                        /simpleirc raw [command] - sends a command to the irc server.
                        """
        );


        ChatUtils.message(message);
        return 1;
    }
}
