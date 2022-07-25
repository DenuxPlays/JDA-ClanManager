package dev.denux.clanmanager.entities;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a clan stored in the database.
 *
 * @see ClanManager#getClan(int)
 */
public interface Clan {

    /**
     * @return The id of the clan.
     */
    int getId();

    /**
     * @return The verification code of the clan.
     */
    String getVerificationCode();

    /**
     * @param verificationCode The new verification code of the clan.
     * @throws IllegalArgumentException If the verification code is invalid.
     */
    void setVerificationCode(@Nonnull String verificationCode);

    /**
     * @return The name of the clan.
     */
    String getName();

    /**
     * @param name The new name of the clan.
     */
    void setName(@Nonnull String name);

    /**
     * @return The clan tag.
     */
    String getTag();

    /**
     * @param tag The new clan tag.
     */
    void setTag(@Nonnull String tag);

    /**
     * @return The discord guild id from the clan.
     */
    long getDiscordGuildId();

    /**
     * @return The discord guild from the clan.
     */
    Guild getDiscordGuild();

    /**
     * @return The discord user id {@link net.dv8tion.jda.api.entities.User} from the clan owner.
     */
    long getOwnerId();

    /**
     * @return The discord member {@link Member} from the clan owner.
     */
    Member getOwner();

    /**
     * Makes an API call to retrieve the member object.
     * @return The member object.
     */
    CompletableFuture<Member> retrieveOwner();

    /**
     * @param owner The discord member {@link Member} from the clan owner.
     */
    void setOwner(@Nonnull Member owner);

    /**
     * @return The id of the clans' leadership role.
     */
    long getLeaderShipRoleId();

    /**
     * @return The clans' leadership role.
     */
    Role getLeaderShipRole();

    /**
     * @param role The clans' leadership role.
     */
    void setLeaderShipRole(@Nonnull Role role);

    /**
     * @return The id of the clans' member role.
     */
    long getMemberRoleId();

    /**
     * @return The clans' member role.
     */
    Role getMemberRole();

    /**
     * @param role The clans' member role.
     */
    void setMemberRole(@Nonnull Role role);

    /**
     * @return The id of the clans' channel.
     */
    long getDiscordChannelId();

    /**
     * @return The clans' channel.
     */
    TextChannel getDiscordChannel();

    /**
     * @param channel The new clans' channel.
     */
    void setDiscordChannel(@Nonnull TextChannel channel);

    /**
     * @param member The user you want the clan member of.
     * @return The clan member with the discord user id from the given user or null.
     * @throws ClanManagerException if there is a problem with the database.
     */
    ClanMember getClanMember(@Nonnull Member member);

    /**
     * @param clanMemberId The id of the clan member you want to get.
     * @return null if no clan member with the given id exists.
     */
    ClanMember getClanMember(int clanMemberId);

    /**
     * @return a list of all clan members.
     * @throws ClanManagerException if there is a problem with the database.
     */
    List<ClanMember> getAllClanMembers();

    /**
     * Creates a new clan member for this clan.
     * @param nickname The nickname of the new clan member.
     * @param locale The locale or language of the new clan member.
     * @param member The discord member {@link Member} of the new clan member.
     * @param leaderShipStatus If the new clan member is in a leadership position or not.
     * @param isCoOwner If the new clan member is a co-owner or not.
     * @param updateRoles True if the roles of the new clan member should be updated.
     * @return The id of the new clan member.
     */
    int createClanMember(@Nonnull String nickname, @Nonnull DiscordLocale locale, @Nonnull Member member, boolean leaderShipStatus, boolean isCoOwner, boolean updateRoles);

    /**
     * Creates a new clan member for this clan.
     * @param nickname The nickname of the new clan member.
     * @param locale The locale or language of the new clan member.
     * @param member The discord member {@link Member} of the new clan member.
     * @return The id of the new clan member.
     */
    int createClanMember(@Nonnull String nickname, @Nonnull DiscordLocale locale, @Nonnull Member member);

    /**
     * Deletes a clan member from this clan.
     * @param clanMember The clan member you want to delete.
     * @param updateRoles True if the roles of the deleted clan member should be updated.
     */
    void deleteClanMember(@Nonnull ClanMember clanMember, boolean updateRoles);

    /**
     * Deletes a clan member from this clan.
     * @param clanMember The clan member you want to delete/remove.
     */
    void deleteClanMember(@Nonnull ClanMember clanMember);

    /**
     * Enables the reverification feature.
     */
    void enableReverification();

    /**
     * Disables the reverification feature.
     */
    void disableReverification();

    /**
     * @return True if the reverification feature is enabled.
     */
    boolean isReverificationEnabled();

    /**
     * @return A list of {@link net.dv8tion.jda.api.entities.User} ids that are not allowed to join this clan.
     */
    @Nonnull
    List<Long> getBlockedUserIds();

    /**
     * Says if the member is blocked for this clan or not.
     * @param member The member
     * @return True if the member is blocked.
     */
    boolean isBlocked(@Nonnull Member member);

    /**
     * Adds a member to the clan's blocked list.
     * @param member The member you want to add to the blocked list.
     */
    void addMemberToBlocklist(@Nonnull Member member);

    /**
     * Removes a member from the clan's blocked list.
     * @param member The member you want to remove from the blocked list.
     * @throws IllegalArgumentException If the member is not in the blocked list.
     */
    void removeMemberFromBlocklist(@Nonnull Member member) throws IllegalArgumentException;

    /**
     * Deletes everyone from the clan's blocked list -> clears the list.
     */
    void clearBlocklist();
}