package dev.denux.clanmanager.internal.entities;

import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.utils.CMChecks;
import dev.denux.clanmanager.utils.CMUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClanImpl implements Clan {

    private final Logger log = JDALogger.getLog(ClanImpl.class);

    private final int id;
    private final ClanManagerConfig config;

    public ClanImpl(ClanManagerConfig config, int id) {
        this.id = id;
        this.config = config;
    }

    /**
     * Gets you the value from the key.
     * @param key The column name.
     * @return Returns null if there is an issue with the database.
     */
    private <T> T get(@NotNull String key, @NotNull Class<T> type) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    String.format("SELECT \"%s\" FROM \"clan\" WHERE \"id\" = ?", key));
            pstm.setLong(1, id);
            ResultSet rs = pstm.executeQuery();
            rs.next();
            T result = rs.getObject(key, type);
            con.close();
            return result;
        } catch (SQLException exception) {
            log.error("Failed to get clan {}", id, exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * Sets the value to the key.
     * @param key The column name.
     * @param value The value to set.
     */
    private void set(String key, Object value) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    String.format("UPDATE \"clan\" SET \"%s\" = ? WHERE \"id\" = ?", key));
            pstm.setObject(1, value);
            pstm.setLong(2, id);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to set database entry.", exception);
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getVerificationCode() {
        return get("verificationCode", String.class);
    }

    @Override
    public void setVerificationCode(@Nonnull String verificationCode) {
        new CMChecks(config).checkVerificationCode(verificationCode);
        set("verificationCode", verificationCode);
    }

    @Override
    public String getName() {
        return get("name", String.class);
    }

    @Override
    public void setName(@Nonnull String name) {
        set("name", name);
    }

    @Override
    public String getTag() {
        return get("tag", String.class);
    }

    @Override
    public void setTag(@Nonnull String tag) {
        set("tag", tag);
    }

    @Override
    public long getDiscordGuildId() {
        return get("discordGuildId", Long.class);
    }

    @Override
    public Guild getDiscordGuild() {
        return config.getJda().getGuildById(getDiscordGuildId());
    }

    @Override
    public long getOwnerId() {
        return get("ownerId", Long.class);
    }

    @Override
    public Member getOwner() {
        return getDiscordGuild().getMemberById(getOwnerId());
    }

    @Override
    public CompletableFuture<Member> retrieveOwner() {
        return getDiscordGuild().retrieveMemberById(getOwnerId()).submit();
    }

    @Override
    public void setOwner(@Nonnull Member owner) {
        set("ownerId", owner.getIdLong());
    }

    @Override
    public long getLeaderShipRoleId() {
        return get("leaderShipRoleId", Long.class);
    }

    @Override
    public Role getLeaderShipRole() {
        return getDiscordGuild().getRoleById(getLeaderShipRoleId());
    }

    @Override
    public void setLeaderShipRole(@Nonnull Role role) {
        set("leaderShipRoleId", role.getIdLong());
    }

    @Override
    public long getMemberRoleId() {
        return get("memberRoleId", Long.class);
    }

    @Override
    public Role getMemberRole() {
        return getDiscordGuild().getRoleById(getMemberRoleId());
    }

    @Override
    public void setMemberRole(@Nonnull Role role) {
        set("memberRoleId", role.getIdLong());
    }

    @Override
    public long getDiscordChannelId() {
        return get("discordChannelId", Long.class);
    }

    @Override
    public TextChannel getDiscordChannel() {
        return getDiscordGuild().getTextChannelById(getDiscordChannelId());
    }

    @Override
    public void setDiscordChannel(@Nonnull TextChannel channel) {
        set("discordChannelId", channel.getIdLong());
    }

    @Override
    public ClanMember getClanMember(@Nonnull Member member) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "SELECT * FROM \"clanMember\" WHERE \"discordUserId\" = ? AND \"clanId\" = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstm.setLong(1, member.getIdLong());
            pstm.setInt(2, getId());
            ResultSet rs = pstm.executeQuery();
            if (!rs.next()) {
                con.close();
                return null;
            }
            int memberId = rs.getInt(1);
            con.close();
            return config.getClanManager().getClanMember(memberId);
        } catch (SQLException exception) {
            log.error("Failed to get clan member.");
            throw new ClanManagerException(exception);
        }
    }

    @Override
    public ClanMember getClanMember(int clanMemberId) {
        return config.getClanManager().getClanMember(clanMemberId);
    }

    @Override
    public List<ClanMember> getAllClanMembers() {
        List<ClanMember> clanMembers = new ArrayList<>();
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clanMember\" WHERE \"clanId\" = ?");
            pstm.setInt(1, getId());
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                clanMembers.add(getClanMember(rs.getInt(1)));
            }
        } catch (SQLException exception) {
            log.error("Failed to get all clan members from the clan {}.", getId());
            throw new ClanManagerException(exception);
        }
        return clanMembers;
    }

    @Override
    public int createClanMember(@Nonnull String nickname, @Nonnull DiscordLocale locale, @Nonnull Member member, boolean leaderShipStatus, boolean isCoOwner, boolean updateRoles) {
        new CMChecks(config).checkClanMemberDuplication(this, member);
        if (isBlocked(member)) throw new IllegalArgumentException("The member is blocked.");

        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "INSERT INTO \"clanMember\" (\"clanId\", \"nickname\", \"locale\", \"discordUserId\", \"leaderShipStatus\", \"coOwnerStatus\") VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, getId());
            pstm.setString(2, nickname);
            pstm.setString(3, locale.getLocale());
            pstm.setLong(4, member.getIdLong());
            pstm.setBoolean(5, leaderShipStatus);
            pstm.setBoolean(6, isCoOwner);
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            rs.next();
            int memberId = rs.getInt(1);
            con.close();
            ClanMember clanMember = getClanMember(memberId);
            if (updateRoles) new CMUtils().updateMemberRoles(clanMember, true);
            if (leaderShipStatus && updateRoles) new CMUtils().updateLeadershipRole(clanMember, true);
            return memberId;
        } catch (SQLException exception) {
            log.error("Failed to create clan member.");
            throw new ClanManagerException(exception);
        }
    }

    @Override
    public int createClanMember(@Nonnull String nickname, @Nonnull DiscordLocale locale, @Nonnull Member member) {
        return createClanMember(nickname, locale, member, false, false, true);
    }

    @Override
    public void deleteClanMember(@Nonnull ClanMember clanMember, boolean updateRoles) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "DELETE FROM \"clanMember\" WHERE \"id\" = ?");
            pstm.setInt(1, clanMember.getId());
            pstm.executeUpdate();
            if (updateRoles) new CMUtils().updateMemberRoles(clanMember, false);
        }catch (SQLException exception) {
            log.error("Failed to delete clan member.");
            throw new ClanManagerException(exception);
        }
    }

    @Override
    public void deleteClanMember(@NotNull ClanMember clanMember) {
        deleteClanMember(clanMember, true);
    }

    @Override
    public void enableReverification() {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("INSERT INTO \"reverificationFeature\" (\"clanId\") VALUES (?)");
            pstm.setInt(1, getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to enable reverification feature.", exception);
        }
        for (ClanMember clanMember : getAllClanMembers()) {
            config.getReverificationManager().scheduleReverification(clanMember);
        }
    }

    @Override
    public void disableReverification() {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("DELETE FROM \"reverificationFeature\" WHERE \"clanId\" = ?");
            pstm.setInt(1, getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to disable reverification feature.", exception);
        }
        for (ClanMember clanMember : getAllClanMembers()) {
            config.getReverificationManager().cancelSchedule(clanMember);
        }
    }

    @Override
    public boolean isReverificationEnabled() {
        return new CMChecks(config).isReverificationEnabled(this);
    }

    @Override
    @Nonnull
    public List<Long> getBlockedUserIds() {
        List<Long> blockedUserIds = new ArrayList<>();
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"discordUserId\" FROM \"blockedUsers\" WHERE \"clanId\" = ?");
            pstm.setInt(1, getId());
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                blockedUserIds.add(rs.getLong(1));
            }
        } catch (SQLException exception) {
            log.error("Failed to get blocked user ids.", exception);
        }
        return blockedUserIds;
    }

    @Override
    public boolean isBlocked(@NotNull Member member) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"discordUserId\" FROM \"blockedUsers\" WHERE \"clanId\" = ? AND \"discordUserId\" = ?");
            pstm.setInt(1, getId());
            pstm.setLong(2, member.getIdLong());
            ResultSet rs = pstm.executeQuery();
            boolean isBlocked = rs.next();
            con.close();
            return isBlocked;
        } catch (SQLException exception) {
            log.error("Failed to check if user is blocked.");
            throw new ClanManagerException(exception);
        }
    }

    @Override
    public void addMemberToBlocklist(@NotNull Member member) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("INSERT INTO \"blockedUsers\" (\"clanId\", \"discordUserId\") VALUES (?, ?)");
            pstm.setInt(1, getId());
            pstm.setLong(2, member.getIdLong());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to add member to blocklist.", exception);
        }
    }

    @Override
    public void removeMemberFromBlocklist(@NotNull Member member) throws IllegalArgumentException {
        if (!isBlocked(member)) throw new IllegalArgumentException("Member is not blocked.");

        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("DELETE FROM \"blockedUsers\" WHERE \"clanId\" = ? AND \"discordUserId\" = ?");
            pstm.setInt(1, getId());
            pstm.setLong(2, member.getIdLong());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to remove member from blocklist.", exception);
        }
    }

    @Override
    public void clearBlocklist() {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("DELETE FROM \"blockedUsers\" WHERE \"clanId\" = ?");
            pstm.setInt(1, getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to clear blocklist.", exception);
        }
    }
}
