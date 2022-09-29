package dev.denux.clanmanager.utils;

import dev.denux.clanmanager.internal.Permission;
import dev.denux.clanmanager.internal.entities.Clan;
import dev.denux.clanmanager.internal.entities.ClanMember;

import javax.annotation.Nonnull;

/**
 * A utility class for the users of the clan manager that should help to simplify some things.
 */
public class CMUtilsChecks {

    /***
     * Checks if the leadership Member has the permissions to change something for the clanMember member.
     * @param leadership The leadership Member.
     * @param clanMember The clanMember Member.
     * @return True if he has the permissions, false if not.
     */
    public boolean clanMemberIsAbove(@Nonnull ClanMember leadership, @Nonnull ClanMember clanMember) {
        if (leadership.getClan().getId() != clanMember.getClan().getId()) return false;
        if (leadership.getId() == clanMember.getId()) return false;
        if (!leadership.hasPermission(Permission.LEADERSHIP)) return false;
        Clan clan = leadership.getClan();
        if (clan.getOwnerDiscordUserId() == clanMember.getDiscordUserId()) return false;

        if (clanMember.hasPermission(Permission.CO_OWNER) && leadership.getDiscordUserId() == clan.getOwnerDiscordUserId()) return true;
        if (leadership.hasPermission(Permission.LEADERSHIP) && !clanMember.hasPermission(Permission.LEADERSHIP)) return true;
        if (leadership.hasPermission(Permission.CO_OWNER) && !clanMember.hasPermission(Permission.CO_OWNER)) return true;

        return  false;
    }
}
