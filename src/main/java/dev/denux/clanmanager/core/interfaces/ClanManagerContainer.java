package dev.denux.clanmanager.core.interfaces;

import dev.denux.clanmanager.ClanManager;

import javax.annotation.Nonnull;

/**
 * An interface for objects that have a corresponding {@link ClanManager} instance.
 */
public interface ClanManagerContainer {

    /**
     * The corresponding {@link ClanManager} instance.
     * @return The {@link ClanManager} instance.
     */
    @Nonnull
    ClanManager getClanManager();
}
