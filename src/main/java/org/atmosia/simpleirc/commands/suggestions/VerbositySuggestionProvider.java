package org.atmosia.simpleirc.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class VerbositySuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        suggestionsBuilder.suggest("raw");
        suggestionsBuilder.suggest("verbose");
        suggestionsBuilder.suggest("normal");
        suggestionsBuilder.suggest("quiet");
        return CompletableFuture.completedFuture(suggestionsBuilder.build());
    }
}
