package de.cubenation.plugins.cnwarn.model.exception;

import de.cubenation.plugins.utils.chatapi.ResourceConverter;
import de.cubenation.plugins.utils.exceptionapi.PlayerException;

public class WarnsNotFoundException extends PlayerException {
    private static final long serialVersionUID = 9039231744996564277L;

    public WarnsNotFoundException(String playerName) {
        super("warns not found for player " + playerName, playerName);
    }

    @Override
    public String getLocaleMessage(ResourceConverter converter) {
        return converter.convert("exception.warnsNotFound", getPlayerName());
    }
}
