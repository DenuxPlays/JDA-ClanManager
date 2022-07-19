package dev.denux.clanmanager.internal;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import dev.denux.clanmanager.core.reverifications.ReverificationStateManager;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.internal.entities.ClanImpl;
import dev.denux.clanmanager.internal.entities.ClanMemberImpl;
import dev.denux.clanmanager.utils.CMChecks;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            Clan clan = getClan(clanId);
            if (clan == null) throw new ClanManagerException("Clan not found directly after creation");
            clan.createClanMember(owner.getEffectiveName(), owner.getGuild().getLocale(), owner, true, true);
            con.close();
            return clanId;
        } catch (SQLException exception) {
            log.error("Error while creating clan", exception);
        }
        return -1;
    }

    @Override
    public ReverificationStateManager getReverificationStateManager() {
        return config.getReverificationManager();
    }
}
