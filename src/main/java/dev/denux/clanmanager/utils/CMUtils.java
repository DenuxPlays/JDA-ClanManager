package dev.denux.clanmanager.utils;

import dev.denux.clanmanager.entities.ClanMember;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

/**
 * Basic utility class for the ClanManager.
 */
public class CMUtils {

    /**
     * Updates the role of a {@link Member}
     * @param clanMember The {@link ClanMember} to update.
     * @param discordMember The {@link Member} to update.
     * @param memberStatus True if he is in the clan, false if he is not.
     */
    public void updateMemberRoles(@Nonnull ClanMember clanMember, @Nonnull Member discordMember, boolean memberStatus) {
        if (memberStatus) {
            clanMember.getClan().getDiscordGuild().addRoleToMember(discordMember, clanMember.getClan().getMemberRole()).queue();
        } else {
            clanMember.getClan().getDiscordGuild().removeRoleFromMember(discordMember, clanMember.getClan().getMemberRole()).queue();
        }
    }

    /**
     * Updates the role of a {@link Member}
     * @param clanMember The {@link ClanMember} to update.
     * @param memberStatus True if he is in the clan, false if he is not.
     */
    public void updateMemberRoles(@Nonnull ClanMember clanMember, boolean memberStatus) {
        updateMemberRoles(clanMember, clanMember.getDiscordMember(), memberStatus);
    }

    /**
     * Updates the role of a {@link Member}.
     *
     * @param clanMember       The clan member to update.
     * @param discordMember    The discord member to update.
     * @param leadershipStatus The new leadership status of the clan member.
     */
    public void updateLeadershipRoles(@Nonnull ClanMember clanMember, @Nonnull Member discordMember, boolean leadershipStatus) {
        if (leadershipStatus) {
            clanMember.getClan().getDiscordGuild().addRoleToMember(discordMember, clanMember.getClan().getLeaderShipRole()).queue();
        } else {
            clanMember.getClan().getDiscordGuild().removeRoleFromMember(discordMember, clanMember.getClan().getLeaderShipRole()).queue();
        }
    }

    /**
     * Updates the role of a {@link net.dv8tion.jda.api.entities.Member}.
     *
     * @param clanMember       The clan member to update.
     * @param leadershipStatus The new leadership status of the clan member.
     */
    public void updateLeadershipRole(@Nonnull ClanMember clanMember, boolean leadershipStatus) {
        updateLeadershipRoles(clanMember, clanMember.getDiscordMember(), leadershipStatus);
    }
}
