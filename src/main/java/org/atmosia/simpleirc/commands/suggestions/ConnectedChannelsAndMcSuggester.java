package org.atmosia.simpleirc.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atmosia.simpleirc.MainClient;

import java.util.concurrent.CompletableFuture;

public class ConnectedChannelsAndMcSuggester implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        if (MainClient.irc == null) {
            suggestionsBuilder.suggest("#minecraft_chat");
            return suggestionsBuilder.buildFuture();
        }

        for (var channel : MainClient.irc.channels()) {
            suggestionsBuilder.suggest(channel.Name);
        }
        suggestionsBuilder.suggest("#minecraft_chat");
        return suggestionsBuilder.buildFuture();
    }
}