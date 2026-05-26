package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atmosia.simpleirc.MainClient;

public class DisconnectCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command.then(ClientCommands.literal("disconnect").executes(DisconnectCommand::Disconnect));
        return command;
    }

    private static int Disconnect(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        MainClient.Disconnect("Disconnected via command");
        return 0;
    }
}
