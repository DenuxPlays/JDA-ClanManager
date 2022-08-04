package dev.denux.clanmanager.entities;

import dev.denux.clanmanager.core.interfaces.ClanManagerContainer;
import dev.denux.clanmanager.internal.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Set;
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

    int getPermissionsLevel();

    @Nullable
    Permission getPermission();

    /**
     * Checks if the clan member has the given permission.
     *
     * @param permission The permission to check.
     * @return True if the clan member has the permission.
     */
    boolean hasPermission(@Nonnull Permission permission);

    /**
     * Adds the {@link Permission#LEADERSHIP} to the clan member.
     */
    void addLeadershipPermission();

    /**
     * @param updateDiscordRoles False if you don't want to update the {@link Member}'s roles.
     */
    void addLeadershipPermission(boolean updateDiscordRoles);

    /**
     * Adds the {@link Permission#CO_OWNER} to the clan member.
     */
    void addCoOwnerPermission();

    /**
     * Removes a permission from a member
     * @param permission the permssions to remove.
     * @throws dev.denux.clanmanager.core.exceptions.PermissionExeption if you try to remove the {@link Permission#MEMBER} permission.
     */
    void removePermission(@Nonnull Permission permission);

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
     *
     * @return The member object.
     */
    CompletableFuture<Member> retrieveDiscordMember();

    /**
     * @param member The new member {@link net.dv8tion.jda.api.entities.Member} of the clan member.
     */
    void setDiscordMember(@Nonnull Member member);
}
