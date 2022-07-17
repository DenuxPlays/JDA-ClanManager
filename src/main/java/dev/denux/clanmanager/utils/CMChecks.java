package dev.denux.clanmanager.utils;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.entities.Clan;
import net.dv8tion.jda.api.entities.Member;
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
        try(Connection con = config.getDataSource().getConnection();) {
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
