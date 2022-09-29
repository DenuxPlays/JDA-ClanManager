package dev.denux.clanmanager.internal;

/**
 * Represents the permission of a {@link dev.denux.clanmanager.internal.entities.ClanMember}.
 */
public enum CmPermission {
    /**
     * The top permission that can only have one {@link net.dv8tion.jda.api.entities.User} at a time.
     */
    OWNER(4),
    /**
     * Helps the Clan owner and has nearly the same permissions as {@link #OWNER}.
     */
    CO_OWNER(3),
    /**
     * Can be compared with a moderator.
     */
    LEADERSHIP(2),
    /**
     * The standard member permission.
     */
    MEMBER(1);

    private final int level;

    CmPermission(int level) {
        if (level > 5) {
            throw new IllegalArgumentException("Level can not be higher than 4");
        }
        this.level = level;
    }

    /**
     * Gets the level of the permission. (for internal use only)
     * @return the level.
     */
    public int getLevel() {
        return level;
    }
}
