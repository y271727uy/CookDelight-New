package com.y271727uy.cookdelight.client.compat.manorsbounty;

import java.util.List;

public record ManorsMachineDefinition(
        List<String> identifiers,
        String titleKey,
        List<String> recipePatterns
) {
    public ManorsMachineDefinition {
        identifiers = List.copyOf(identifiers);
        titleKey = titleKey == null ? "" : titleKey;
        recipePatterns = List.copyOf(recipePatterns);
    }
}


