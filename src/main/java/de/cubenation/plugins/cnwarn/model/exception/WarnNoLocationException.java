package de.cubenation.plugins.cnwarn.model.exception;

import de.cubenation.plugins.utils.chatapi.ResourceConverter;
import de.cubenation.plugins.utils.exceptionapi.MessageableException;

public class WarnNoLocationException extends MessageableException {
    private static final long serialVersionUID = 6051672205233537812L;

    private final int warnId;

    public WarnNoLocationException(int warnId) {
        super("no location for warn with id " + Integer.toString(warnId));

        this.warnId = warnId;
    }

    @Override
    public String getLocaleMessage(ResourceConverter converter) {
        return converter.convert("exception.warnNoLocation", Integer.toString(warnId));
    }

    public int getWarnId() {
        return warnId;
    }
}
