package dev.denux.clanmanager;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import dev.denux.clanmanager.internal.entities.Clan;
import dev.denux.clanmanager.internal.entities.ClanMember;
import dev.denux.clanmanager.utils.CMChecks;
import dev.denux.clanmanager.utils.CMUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanManager {
    private static final Logger log = JDALogger.getLog(ClanManager.class);

    private final ClanManagerConfig config;

    public ClanManager(@Nonnull ClanManagerConfig config) {
        this.config = config;
        config.setClanManager(this);
    }

    public @Nullable Clan getClan(int id) {
        try {
            new CMChecks(config).checkClan(id);
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            return null;
        }
        return new Clan(config, id);
    }

    public @Nullable Clan getClanByVerificationCode(@Nonnull String code) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clan\" WHERE \"verificationCode\" = ?");
            pstm.setString(1, code);
            ResultSet rs = pstm.executeQuery();
            if(!rs.next()) {
                return null;
            }
            return getClan(rs.getInt("id"));
        } catch (SQLException exception) {
            log.error("Error while getting clan by verification code", exception);
            return null;
        }
    }

    public @Nonnull List<Clan> getAllClansFromAGuild(@Nonnull Guild guild) {
        List<Clan> clans = new ArrayList<>();
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clan\" WHERE \"discordGuildId\" = ?");
            pstm.setLong(1, guild.getIdLong());
            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                clans.add(getClan(resultSet.getInt(1)));
            }
        } catch (SQLException exception) {
            log.error("Error while getting all clans from a guild", exception);
        }
        return clans;
    }

    public @Nullable ClanMember getClanMember(int id) {
        try {
            new CMChecks(config).checkClanMember(id);
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            return null;
        }
        return new ClanMember(config, id);
    }

    public @Nonnull List<ClanMember> getAllClanMembersByDiscordMember(@Nonnull Member member) {
        List<ClanMember> clanMembers = new ArrayList<>();
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "SELECT DISTINCT \"clanMember\".\"id\" FROM \"clanMember\", \"clan\" WHERE \"clanMember\".\"discordUserId\" = ? AND \"clan\".\"discordGuildId\" = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstm.setLong(1, member.getIdLong());
            pstm.setLong(2, member.getGuild().getIdLong());
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                clanMembers.add(config.getClanManager().getClanMember(rs.getInt(1)));
            }
        } catch (SQLException exception) {
            log.error("Error while getting all clan members by discord member", exception);
        }
        return clanMembers;
    }

    public int createClan(@Nonnull String name, @Nonnull String tag, @Nonnull String verificationCode, @Nonnull Guild guild, @Nonnull Member owner, @Nonnull TextChannel channel, @Nonnull Role leadershipRole, @Nonnull Role memberRole) {
        new CMChecks(config).checkClanBeforeCreation(name, tag, verificationCode, guild, owner, channel, leadershipRole, memberRole);
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "INSERT INTO \"clan\" (\"name\", \"tag\", \"verificationCode\", \"discordGuildId\", \"ownerId\", \"ownerUserId\", \"discordChannelId\",\"leaderShipRoleId\", \"memberRoleId\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pstm.setString(1, name);
            pstm.setString(2, tag);
            pstm.setString(3, verificationCode);
            pstm.setLong(4, guild.getIdLong());
            pstm.setLong(5, owner.getIdLong());
            pstm.setLong(6, channel.getIdLong());
            pstm.setLong(7, leadershipRole.getIdLong());
            pstm.setLong(8, memberRole.getIdLong());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if(!rs.next()) {
                return -1;
            }
            int clanId = rs.getInt(1);
            con.close();

            Clan clan = getClan(clanId);
            if (clan == null) throw new ClanManagerException("Clan not found directly after creation");
            int cmId = clan.createClanMember(owner.getEffectiveName(), owner.getGuild().getLocale(), owner, true, true, true);
            ClanMember clanMember = getClanMember(cmId);
            if (clanMember == null) throw new ClanManagerException("Clan member not found directly after creation");
            new CMUtils().updateMemberRoles(clanMember, true);
            new CMUtils().updateLeadershipRole(clanMember, true);

            return clanId;
        } catch (SQLException exception) {
            log.error("Error while creating clan", exception);
        }
        return -1;
    }

    public void deleteClan(@Nonnull Clan clan) {
        int clanId = clan.getId();
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("DELETE FROM \"clan\" WHERE \"id\" = ?");
            pstm.setInt(1, clanId);
            pstm.executeUpdate();

            pstm = con.prepareStatement("DELETE FROM \"clanMember\" WHERE \"clanId\" = ?");
            pstm.setInt(1, clanId);
            pstm.executeUpdate();

            pstm = con.prepareStatement("DELETE FROM \"reverificationFeature\" WHERE \"clanId\" = ?");
            pstm.setInt(1, clanId);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Error while deleting clan", exception);
        }
    }

    public @Nonnull ReverificationStateManager getReverificationStateManager() {
        return config.getReverificationManager();
    }

    public @Nonnull ClanManagerConfig getConfig() {
        return config;
    }
}
