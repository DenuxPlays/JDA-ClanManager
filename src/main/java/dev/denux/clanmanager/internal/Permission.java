package dev.denux.clanmanager.internal;

//TODO add java docs

import javax.annotation.Nonnull;

/**
 * Represents the permission of a {@link dev.denux.clanmanager.entities.ClanMember}.
 */
public enum Permission {
    /**
     * The top permission that can only have one {@link net.dv8tion.jda.api.entities.User} at a time.
     */
    OWNER("owner"),
    /**
     * Helps the Clan owner and has nearly the same permissions as {@link #OWNER}.
     */
    CO_OWNER("co-owner"),
    /**
     * Can be compared with a moderator.
     */
    LEADERSHIP("leadership");

    private final String name;

    Permission(@Nonnull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
