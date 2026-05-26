package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.atmosia.simpleirc.ChatUtils;
import org.atmosia.simpleirc.MainClient;

public class StatusCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command.then(ClientCommands.literal("status").executes(StatusCommand::Status));
        return command;
    }
    public static int Status(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if(MainClient.irc!=null && MainClient.irc.isConnected())
        {
            StringBuilder channelString = new StringBuilder();
            for (var channel : MainClient.irc.channels()) {
                channelString.append("<gold>").append(channel.Prefix).append("</gold> | <blue>").append(channel.Name).append("</blue>");
                if (channel.Name.equals(MainClient.irc.GetPrimaryChannel())) {
                    channelString.append(" | <red>Default channel</red>");
                }
                channelString.append("\n");
            }
            channelString.append("<gold>").append(MainClient.MinecraftChatPrefix).append("</gold> | <blue>").append("#minecraft_chat").append("</blue>");
            if (MainClient.irc.GetPrimaryChannel().equals("#minecraft_chat")) {
                channelString.append(" | <red>Default channel</red>");
            }
            Component message = MiniMessage.miniMessage().deserialize(
                    "<green>Connected to server:</green> <yellow>" + MainClient.irc.ip() + "</yellow>\n" +
                            channelString.toString().replace("<gold>null</gold>", "<gold>Not Set</gold>")
            );
            ChatUtils.message(message);
            // TODO: refactor to not use ChatUtils.message(). Maybe .info()?
        }
        else
        {
            ChatUtils.Info("Not connected to a server.");
        }
        return 0;
    }
}
