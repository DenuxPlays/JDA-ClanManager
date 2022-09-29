package dev.denux.clanmanager.internal.entities;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.exceptions.ClanManagerException;
import dev.denux.clanmanager.utils.CMChecks;
import dev.denux.clanmanager.utils.CMUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

public class Clan {

    private final Logger log = JDALogger.getLog(Clan.class);

    private final int id;
    private final ClanManagerConfig config;

    public Clan(ClanManagerConfig config, int id) {
        this.id = id;
        this.config = config;
    }

    /**
     * Gets you the value from the key.
     * @param key The column name.
     * @return Returns null if there is an issue with the database.
     */
    private <T> T get(@Nonnull String key, @Nonnull Class<T> type) {
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
    private void set(@Nonnull String key, @Nonnull Object value) {
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

    public int getId() {
        return id;
    }

    public String getVerificationCode() {
        return get("verificationCode", String.class);
    }

    public void setVerificationCode(@Nonnull String verificationCode) {
        new CMChecks(config).checkVerificationCode(verificationCode);
        set("verificationCode", verificationCode);
    }

    public String getName() {
        return get("name", String.class);
    }

    public void setName(@Nonnull String name) {
        set("name", name);
    }

    public String getTag() {
        return get("tag", String.class);
    }

    public void setTag(@Nonnull String tag) {
        set("tag", tag);
    }

    public long getDiscordGuildId() {
        return get("discordGuildId", Long.class);
    }

    public Guild getDiscordGuild() {
        return config.getJda().getGuildById(getDiscordGuildId());
    }

    public long getOwnerDiscordUserId() {
        return get("ownerUserId", Long.class);
    }

    public Member getOwnerAsDiscordMember() {
        return getDiscordGuild().getMemberById(getOwnerDiscordUserId());
    }

    public CompletableFuture<Member> retrieveOwnerAsDiscordMember() {
        return getDiscordGuild().retrieveMemberById(getOwnerDiscordUserId()).submit();
    }

    public void changeOwner(@Nonnull ClanMember owner) {
        set("ownerId", owner.getId());
        set("ownerUserId", owner.getDiscordUserId());
    }

    public int getOwnerClanMemberId() {
        return get("ownerId", int.class);
    }

    public ClanMember getOwnerAsClanMember() {
        return config.getClanManager().getClanMember(getOwnerClanMemberId());
    }

    public long getLeaderShipRoleId() {
        return get("leaderShipRoleId", Long.class);
    }

    public Role getLeaderShipRole() {
        return getDiscordGuild().getRoleById(getLeaderShipRoleId());
    }

    public void setLeaderShipRole(@Nonnull Role role) {
        set("leaderShipRoleId", role.getIdLong());
    }

    public long getMemberRoleId() {
        return get("memberRoleId", Long.class);
    }

    public Role getMemberRole() {
        return getDiscordGuild().getRoleById(getMemberRoleId());
    }

    public void setMemberRole(@Nonnull Role role) {
        set("memberRoleId", role.getIdLong());
    }

    public long getDiscordChannelId() {
        return get("discordChannelId", Long.class);
    }

    public TextChannel getDiscordChannel() {
        return getDiscordGuild().getTextChannelById(getDiscordChannelId());
    }

    public void setDiscordChannel(@Nonnull TextChannel channel) {
        set("discordChannelId", channel.getIdLong());
    }

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

    public ClanMember getClanMember(int clanMemberId) {
        return config.getClanManager().getClanMember(clanMemberId);
    }

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

    public int createClanMember(@Nonnull String nickname, @Nonnull DiscordLocale locale, @Nonnull Member member) {
        return createClanMember(nickname, locale, member, false, false, true);
    }

    public void deleteClanMember(@Nonnull ClanMember clanMember, boolean updateRoles) {
        if (updateRoles) new CMUtils().updateMemberRoles(clanMember, false);
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    "DELETE FROM \"clanMember\" WHERE \"id\" = ?");
            pstm.setInt(1, clanMember.getId());
            pstm.executeUpdate();
        }catch (SQLException exception) {
            log.error("Failed to delete clan member.");
            throw new ClanManagerException(exception);
        }
    }

    public void deleteClanMember(@Nonnull ClanMember clanMember) {
        deleteClanMember(clanMember, true);
    }

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

    public boolean isReverificationEnabled() {
        return new CMChecks(config).isReverificationEnabled(this);
    }

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

    public boolean isBlocked(@Nonnull Member member) {
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

    public void addMemberToBlocklist(@Nonnull Member member) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("INSERT INTO \"blockedUsers\" (\"clanId\", \"discordUserId\") VALUES (?, ?)");
            pstm.setInt(1, getId());
            pstm.setLong(2, member.getIdLong());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to add member to blocklist.", exception);
        }
    }

    public void removeMemberFromBlocklist(@Nonnull Member member) throws IllegalArgumentException {
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

    public void clearBlocklist() {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("DELETE FROM \"blockedUsers\" WHERE \"clanId\" = ?");
            pstm.setInt(1, getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to clear blocklist.", exception);
        }
    }

    @NotNull
    public ClanManager getClanManager() {
        return config.getClanManager();
    }
}
