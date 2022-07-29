package dev.denux.clanmanager.core.interfaces;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.ClanManagerConfig;

import javax.annotation.Nonnull;

/**
 * An interface for objects that have a corresponding {@link ClanManager} instance.
 */
public interface ClanManagerContainer {

    /**
     * The corresponding {@link ClanManager} instance.
     * @return the {@link ClanManager} instance.
     */
    @Nonnull
    ClanManager getClanManager();

    /**
     * The corresponding {@link ClanManagerConfig} instance.
     * @return the {@link ClanManagerConfig} instance.
     */
    @Nonnull
    default ClanManagerConfig getClanManagerConfig() {
        return getClanManager().getConfig();
    }
}
