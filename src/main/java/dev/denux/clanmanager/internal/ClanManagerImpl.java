package dev.denux.clanmanager.internal;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.internal.entities.ClanImpl;
import dev.denux.clanmanager.internal.entities.ClanMemberImpl;
import dev.denux.clanmanager.utils.CMChecks;
import dev.denux.clanmanager.utils.CMUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanManagerImpl implements ClanManager {
    private static final Logger log = JDALogger.getLog(ClanManagerImpl.class);

    private final ClanManagerConfig config;

    public ClanManagerImpl(ClanManagerConfig config) {
        this.config = config;
        config.setClanManager(this);
    }

    @Override
    @Nullable
    public Clan getClan(int id) {
        try {
            new CMChecks(config).checkClan(id);
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            return null;
        }
        return new ClanImpl(config, id);
    }

    @Override
    @Nullable
    public Clan getClanByVerificationCode(@NotNull String code) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clan\" WHERE \"verificationCode\" = ?");
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

    @NotNull
    @Override
    public List<Clan> getAllClansFromAGuild(@NotNull Guild guild) {
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

    @Override
    @Nullable
    public ClanMember getClanMember(int id) {
        try {
            new CMChecks(config).checkClanMember(id);
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            return null;
        }
        return new ClanMemberImpl(config, id);
    }

    @Override
    @Nonnull
    public List<ClanMember> getAllClanMembersByDiscordMember(@NotNull Member member) {
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

    @Override
    public int createClan(@NotNull String name, @NotNull String tag, @NotNull String verificationCode, @NotNull Guild guild, @NotNull Member owner, @NotNull TextChannel channel, @NotNull Role leadershipRole, @NotNull Role memberRole) {
        new CMChecks(config).checkClanBeforeCreation(name, tag, verificationCode, guild, owner, channel, leadershipRole, memberRole);
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "INSERT INTO \"clan\" (\"name\", \"tag\", \"verificationCode\", \"discordGuildId\", \"ownerId\", \"discordChannelId\",\"leaderShipRoleId\", \"memberRoleId\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
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

    @Override
    public void deleteClan(@NotNull Clan clan) {
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

    @Override
    public ReverificationStateManager getReverificationStateManager() {
        return config.getReverificationManager();
    }

    @Override
    public ClanManagerConfig getConfig() {
        return config;
    }
}
