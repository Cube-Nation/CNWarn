package de.cubenation.plugins.cnwarn.model.exception;

import de.cubenation.plugins.utils.chatapi.ResourceConverter;
import de.cubenation.plugins.utils.exceptionapi.MessageableException;

public class WarnNotFoundException extends MessageableException {
    private static final long serialVersionUID = 6051672205231537812L;

    private final int warnId;

    public WarnNotFoundException(int warnId) {
        super("warn not found for id " + Integer.toString(warnId));

        this.warnId = warnId;
    }

    @Override
    public String getLocaleMessage(ResourceConverter converter) {
        return converter.convert("exception.warnNotFound", Integer.toString(warnId));
    }

    public int getWarnId() {
        return warnId;
    }
}
