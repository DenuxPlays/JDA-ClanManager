package dev.denux.clanmanager.core.exceptions;

import javax.annotation.Nonnull;

public class PermissionException extends ClanManagerException {

    public PermissionException(@Nonnull String message) {
        super(message);
    }
}
