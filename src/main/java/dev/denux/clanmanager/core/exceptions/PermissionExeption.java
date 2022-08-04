package dev.denux.clanmanager.core.exceptions;

import javax.annotation.Nonnull;

public class PermissionExeption extends ClanManagerException {

    public PermissionExeption(@Nonnull String message) {
        super(message);
    }
}
