package dev.denux.clanmanager.utils;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.internal.entities.Clan;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Some utility methods for the ClanManager.
 */
public class CMChecks {
    private static final Logger log = JDALogger.getLog(CMChecks.class);
    private final ClanManagerConfig config;

    public CMChecks(@Nonnull ClanManagerConfig config) {
        this.config = config;
    }

    /**
     * @param clan The clan to check.
     * @return True if reverification feature is enabled for the clan.
     */
    public boolean isReverificationEnabled(@Nonnull Clan clan) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"numberOfDays\" FROM \"reverificationFeature\" WHERE \"clanId\" = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstm.setInt(1, clan.getId());
            ResultSet rs = pstm.executeQuery();
            boolean result = rs.first();
            con.close();
            return result;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Checks all the attributes of a Clan for validation.
     * @param name The name of the Clan.
     * @param tag The tag of the Clan.
     * @param verificationCode The verification code of the Clan.
     * @param guild The Guild the Clan is in.
     * @param owner The owner of the Clan.
     * @param channel The TextChannel the Clan is in.
     * @param leadershipRole The Role the Clan leadership is in.
     * @param memberRole The Role the Clan members are in.
     */
    public void checkClanBeforeCreation(@Nonnull String name, @Nonnull String tag, @Nonnull String verificationCode, @Nonnull Guild guild, @Nonnull Member owner, @Nonnull TextChannel channel, @Nonnull Role leadershipRole, @Nonnull Role memberRole) {
        if (config.getClanManager().getClanByVerificationCode(verificationCode) != null) throw new IllegalArgumentException("Clan with the given code already exists.");
        if (name.isEmpty() || name.isBlank()) throw new IllegalArgumentException("Clan name cannot be empty.");
        if (tag.isEmpty() || tag.isBlank()) throw new IllegalArgumentException("Clan tag cannot be empty.");
        if (owner.getUser().isBot() || owner.getUser().isSystem()) throw new IllegalArgumentException("Owner is a bot.");
        if (!owner.getGuild().equals(guild)) throw new IllegalArgumentException("Owner must be in the same guild as the clan.");
        if (!memberRole.getGuild().equals(guild)) throw new IllegalArgumentException("Member role must be in the same guild as the clan.");
        if (!leadershipRole.getGuild().equals(guild)) throw new IllegalArgumentException("Leadership role must be in the same guild as the clan.");
        if (!channel.getGuild().equals(guild)) throw new IllegalArgumentException("Channel must be in the same guild as the clan.");
    }

    /**
     * Checks if a clan with the given id exists.
     * @param id The id to check.
     */
    public void checkClan(int id) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT * FROM \"clan\" WHERE \"id\" = ?");
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException(String.format("Clan with the id %d does not exist", id));
            }
        } catch (SQLException exception) {
            log.error("Failed to get clan {}", id, exception);
        }
    }

    /**
     * Checks if a clan member with the given id exists.
     * @param id The id to check.
     */
    public void checkClanMember(int id) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT * FROM \"clanMember\" WHERE \"id\" = ?");
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException(String.format("Clan member with the id %d does not exist", id));
            }
        } catch (SQLException exception) {
            log.error("Failed to get clan {}", id, exception);
        }
    }

    /**
     * Checks if the verification code is null, empty or is already used.
     * @param verificationCode The verification code to check.
     */
    public void checkVerificationCode(@Nonnull String verificationCode) {
        if (verificationCode.isEmpty()) throw new IllegalArgumentException("Verification code cannot be null or empty");
        if (!checkVerificationCodeAvailability(verificationCode)) throw new IllegalArgumentException("Verification code is not available");
    }

    /**
     * Checks if the clan member is already in the clan.
     * @param clan The clan to check.
     * @param member The member to check.
     */
    public void checkClanMemberDuplication(@Nonnull Clan clan, @Nonnull Member member) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clanMember\" WHERE \"clanId\" = ? AND \"discordUserId\" = ?");
            pstm.setInt(1, clan.getId());
            pstm.setLong(2, member.getIdLong());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                con.close();
                throw new IllegalArgumentException(String.format("Member %s is already a member of clan %s.", member.getUser().getAsTag(), clan.getName()));
            }
        } catch (SQLException exception) {
            log.error("Failed to check clan member duplication", exception);
        }
    }

    /**
     * Checks if the verification code is already used.
     * @param verificationCode The verification code to check.
     * @return True if the verification code is used, false if not.
     */
    private boolean checkVerificationCodeAvailability(@Nonnull String verificationCode) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clan\" WHERE \"verificationCode\" = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstm.setString(1, verificationCode);
            boolean result = !pstm.executeQuery().first();
            con.close();
            return result;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
