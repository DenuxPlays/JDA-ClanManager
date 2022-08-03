package dev.denux.clanmanager.entities;

import dev.denux.clanmanager.core.interfaces.ClanManagerContainer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a clan member stored in the database.
 *
 * @see Clan#getClanMember(Member)
 * @see Clan#getClanMember(int)
 * @see Clan#getAllClanMembers()
 */
public interface ClanMember extends ClanManagerContainer {

    /**
     * @return The id of the clan member.
     */
    int getId();

    /**
     * @return The date when the clan member joined the clan.
     */
    Timestamp getVerificationDate();

    /**
     * Should only be used for the reverification feature.
     *
     * @param verificationDate The new date when the clan member joined the clan.
     */
    void setVerificationDate(@Nonnull Timestamp verificationDate);

    /**
     * @return The nickname of the clan member.
     */
    String getNickname();

    /**
     * @param nickname The new nickname of the clan member.
     */
    void setNickname(@Nonnull String nickname);

    /**
     * @return The true if the clan member has a leadership position.
     */
    boolean getLeaderShipStatus();

    /**
     * @param leaderShipStatus The new leadership status of the clan member.
     */
    void setLeaderShipStatus(boolean leaderShipStatus);

    /**
     * @param leaderShipStatus The new leadership status of the clan member.
     * @param updateDiscordRoles False if you don't want to update the {@link Member}'s roles.
     */
    void setLeaderShipStatus(boolean leaderShipStatus, boolean updateDiscordRoles);

    /**
     * @return True if the clan member is a co owner.
     */
    boolean getCoOwnerStatus();

    /**
     * @param status The new co owner status of the clan member.
     */
    void setCoOwnerStatus(boolean status);

    /**
     * @return True if the clan member is an owner.
     * @see Clan#getOwner()
     * @see Clan#getOwnerId()
     */
    boolean getOwnerStatus();

    /**
     * Sets the clan member as the owner of the clan.
     * @see Clan#setOwner(Member)
     */
    void setAsOwner();
    /**
     * @return The locale of the clan member.
     */
    DiscordLocale getLocale();

    /**
     * @param locale The new locale of the clan member.
     */
    void setLocale(@Nonnull DiscordLocale locale);

    /**
     * @return The id of the clan the member is in.
     */
    int getClanId();

    /**
     * @return The clan of the clan member.
     */
    Clan getClan();

    /**
     * @return The user id {@link net.dv8tion.jda.api.entities.User} of the clan member.
     */
    long getDiscordUserId();

    /**
     * @return The member {@link net.dv8tion.jda.api.entities.Member} of the clan member.
     */
    Member getDiscordMember();

    /**
     * Makes an API call to retrieve the member object.
     * @return The member object.
     */
    CompletableFuture<Member> retrieveDiscordMember();

    /**
     * @param member The new member {@link net.dv8tion.jda.api.entities.Member} of the clan member.
     */
    void setDiscordMember(@Nonnull Member member);
}
