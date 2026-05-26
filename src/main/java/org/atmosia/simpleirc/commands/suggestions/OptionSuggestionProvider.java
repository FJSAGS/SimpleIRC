package org.atmosia.simpleirc.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class OptionSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        suggestionsBuilder.suggest("server");
        suggestionsBuilder.suggest("port");
        suggestionsBuilder.suggest("channel");
        suggestionsBuilder.suggest("channel_password");
        suggestionsBuilder.suggest("backup_nickname");
        suggestionsBuilder.suggest("verbosity");
        return CompletableFuture.completedFuture(suggestionsBuilder.build());
    }
}
