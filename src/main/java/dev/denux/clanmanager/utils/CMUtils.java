package dev.denux.clanmanager.utils;

import dev.denux.clanmanager.entities.ClanMember;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

public class CMUtils {
    /**
     * Updates the role of a {@link net.dv8tion.jda.api.entities.Member}.
     * @param clanMember The clan member to update.
     * @param discordMember The discord member to update.
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
     * @param clanMember The clan member to update.
     * @param leadershipStatus The new leadership status of the clan member.
     */
    public void updateLeadershipRole(@Nonnull ClanMember clanMember, boolean leadershipStatus) {
        updateLeadershipRoles(clanMember, clanMember.getDiscordMember(), leadershipStatus);
    }
}
