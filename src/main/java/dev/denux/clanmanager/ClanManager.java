package dev.denux.clanmanager;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The core of this manager.
 *
 * <pre>{@code
 * ClanManager clanManager = ClanManagerBuilder
 *             .setJDA(jda) // Your JDA instance
 *             .setJdbcUrl("jdbc:postgresql://localhost:5432/clanManager") // Your Jdbc URL to your SQL Database
 *             .build();
 * }</pre>
 *
 * @see ClanManagerBuilder
 */
public interface ClanManager {

    /**
     * @param id The id of the clan.
     * @return The {@link Clan} with the given id or null.
     */
    @Nullable
    Clan getClan(int id);

    /**
     * @param code The code of the clan.
     * @return The {@link Clan} with the given code or null.
     */
    @Nullable
    Clan getClanByVerificationCode(@Nonnull String code);

    /**
     * @param guild The guild of the clans.
     * @return A list of all clans that a registered in the given guild.
     */
    @Nonnull
    List<Clan> getAllClansFromAGuild(@Nonnull Guild guild);

    /**
     * @param id The id of the clan member.
     * @return The {@link ClanMember} with the given id or null.
     * @throws IllegalArgumentException If the clan with the given id does not exist.
     */
    @Nullable
    ClanMember getClanMember(int id);

    /**
     * Gives you all ClanMembers for the given Discord User.
     *
     * @param member The Discord User you want to search after. {@link Member}
     * @return A List of ClanMember or an empty List if the User is has no ClanMember entry's.
     */
    @Nonnull
    List<ClanMember> getAllClanMembersByDiscordMember(@Nonnull Member member);

    /**
     * Creates a new clan with the given attributes.
     * @param name The name of the clan.
     * @param tag The tag of the clan.
     * @param verificationCode The verification code of the clan.
     * @param guild The guild of the clan.
     * @param owner The owner of the clan.
     * @param channel The channel of the clan.
     * @param leadershipRole The leadership role of the clan.
     * @param memberRole The member role of the clan.
     * @return The clan's id or -1 if the clan could not be created.
     * @throws IllegalArgumentException If the given attributes are invalid.
     */
    int createClan(@Nonnull String name, @Nonnull String tag, @Nonnull String verificationCode, @Nonnull Guild guild, @Nonnull Member owner,
                   @Nonnull TextChannel channel, @Nonnull Role leadershipRole, @Nonnull Role memberRole);

    /**
     * Deletes the clan and all of its members.
     * @param clan The clan you want to delete.
     */
    void deleteClan(@Nonnull Clan clan);

    /**
     * @return The {@link ReverificationStateManager} instance.
     */
    ReverificationStateManager getReverificationStateManager();

    /**
     * @return The {@link ClanManagerConfig} instance.
     */
    ClanManagerConfig getConfig();
}
