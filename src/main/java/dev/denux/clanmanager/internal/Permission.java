package dev.denux.clanmanager.internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the permission of a {@link dev.denux.clanmanager.entities.ClanMember}.
 */
public enum Permission {
    /**
     * The top permission that can only have one {@link net.dv8tion.jda.api.entities.User} at a time.
     */
    OWNER("owner", 50),
    /**
     * Helps the Clan owner and has nearly the same permissions as {@link #OWNER}.
     */
    CO_OWNER("co-owner", 49),
    /**
     * Can be compared with a moderator.
     */
    LEADERSHIP("leadership", 48),
    /**
     * The standard member permission.
     */
    MEMBER("member", 0);

    private final String name;
    private final int level;

    Permission(@Nonnull String name, int level) {
        this.name = name;
        this.level = level;
    }

    /**
     * Gets you the permission from the given level.
     * @param level The level of the permission.
     * @return the permission or null if the level was not found.
     */
    public static @Nullable Permission fromLevel(int level) {
        if (level == 0) return MEMBER;
        for (Permission permission : values()) {
            if (permission.level == level) {
                return permission;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
