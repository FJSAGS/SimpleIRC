package org.atmosia.simpleirc.commands.subcommands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atmosia.simpleirc.*;
import org.atmosia.simpleirc.commands.suggestions.VerbositySuggestionProvider;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class ConfigCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> Register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        command
                .then(ClientCommands.literal("config")
                        .then(ClientCommands.literal("server").executes(ConfigCommand::ShowServer)
                                .then(argument("value", StringArgumentType.string())
                                        .executes(ctx -> EditConfig(ctx, "server"))
                                )
                )
                        .then(ClientCommands.literal("port").executes(ConfigCommand::ShowPort)
                                .then(argument("int_value", IntegerArgumentType.integer(0, 65535))
                                        .executes(ctx -> EditConfig(ctx, "port"))
                                )
                )
                        .then(ClientCommands.literal("backup_nickname")
                                .then(argument("value", StringArgumentType.string())
                                        .executes(ctx -> EditConfig(ctx, "backup_nickname"))
                                )
                )
                        .then(ClientCommands.literal("autoconnect").executes(ConfigCommand::ShowAutoconect)
                                .then(argument("bool_value", BoolArgumentType.bool())
                                        .executes(ctx -> EditConfig(ctx, "autoconnect"))
                                )
                )
                        .then(ClientCommands.literal("verbosity")
                                .then(argument("value", StringArgumentType.string())
                                        .suggests(new VerbositySuggestionProvider())
                                        .executes(ctx -> EditConfig(ctx, "verbosity"))
                                )
                )
        );
        return command;
    }
    private static int EditConfig(CommandContext<FabricClientCommandSource> context, String option) {
        switch (option) {
            case "server" -> {
                var server = context.getArgument("value", String.class);
                MainClient.settings.ip(server);
                ChatUtils.Notify("Set config option <gray>Server IP</gray> to '<gray>" + server + "</gray>'");
            }
            case "port" -> {
                var port = context.getArgument("int_value", Integer.class);
                MainClient.settings.port(port);
                ChatUtils.Notify("Set config option <gray>Port</gray> to <gray>" + port + "</gray>");
            }
//            case "channel" -> {
//                var channel = context.getArgument("value", String.class);
//                Main.getSettings().channel = channel;
//                ChatUtils.Notify("Set config option <gray>Channel</gray> to '<gray>" + channel + "</gray>'");
//            }
//            case "channel_password" -> {
//                var password = context.getArgument("value", String.class);
//                Main.getSettings().password = password;
//                ChatUtils.Notify("Set config option <gray>Password</gray> to '<gray>" + password + "</gray>'");
//            }
            case "backup_nickname" -> {
                var backup_nick = context.getArgument("value", String.class);
                MainClient.settings.backupnick(backup_nick);
                ChatUtils.Notify("Set config option <gray>Backup Nickname</gray> to '<gray>" + backup_nick + "</gray>'");
            }
            case "autoconnect" -> {
                var autoconnect = context.getArgument("bool_value", Boolean.class);
                MainClient.settings.autoconnect(autoconnect);
                ChatUtils.Notify("Set the config option <gray>Auto Connection</gray> to " + (autoconnect ? "<green>True</green>" : "<red>False</red>"));
            }
            case "verbosity" -> {
                var verbosityString = context.getArgument("value", String.class);
                var verbosity = IrcVerbosity.NORMAL;
                verbosity = switch (verbosityString.toLowerCase()) {
                    case "raw" -> IrcVerbosity.RAW;
                    case "verbose" -> IrcVerbosity.VERBOSE;
                    case "normal" -> IrcVerbosity.NORMAL;
                    case "quiet" -> IrcVerbosity.QUIET;
                    default -> verbosity;
                };
                MainClient.settings.verbosity(verbosity);
                ChatUtils.Notify("Set config option <gray>Verbosity</gray> to <gray>" + verbosityString.toUpperCase() + "</gray>");
                if (MainClient.irc != null) {
                    MainClient.irc.SetVerbosity(verbosity);
                }
            }
        }
        // AutoConfig.getConfigHolder(Ircgroup.class).save();
        return 0;
    }
    private static int ShowServer(CommandContext<FabricClientCommandSource> ctx) {
        ChatUtils.Notify("Server IP in config is: <gray>" + MainClient.settings.ip() + "</gray>");
        return 1;
    }
    private static int ShowPort(CommandContext<FabricClientCommandSource> ctx) {
        ChatUtils.Notify("Server port in config is: <gray>" + MainClient.settings.port() + "</gray>");
        return 1;
    }
    private static int ShowChannel(CommandContext<FabricClientCommandSource> ctx) {
        // ChatUtils.Notify("Auto-connect channel in config is: <gray>" + Main.getSettings().channel + "</gray>");
        return 1;
    }
    private static int ShowAutoconect(CommandContext<FabricClientCommandSource> ctx) {
        ChatUtils.Notify("Do I autoconnect?: <gray>" + (MainClient.settings.autoconnect() ? "<green>Yes</green>" : "<red>No</red>") + "</gray>");
        return 1;
    }
}
