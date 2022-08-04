package dev.denux.clanmanager.internal.entities;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.exceptions.PermissionExeption;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.internal.Permission;
import dev.denux.clanmanager.utils.CMUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

public class ClanMemberImpl implements ClanMember {

    private final Logger log = JDALogger.getLog(ClanImpl.class);

    private final int id;
    private final ClanManagerConfig config;

    public ClanMemberImpl(ClanManagerConfig config, int id) {
        this.id = id;
        this.config = config;
    }

    /**
     * Gets you the value from the key.
     *
     * @param key The column name.
     * @return Null if there is an issue with the database.
     */
    private <T> T get(String key, Class<T> type) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    String.format("SELECT \"%s\" FROM \"clanMember\" WHERE \"id\" = ?", key));
            pstm.setLong(1, id);
            ResultSet rs = pstm.executeQuery();
            if (!rs.next()) {
                con.close();
                return null;
            }
            T value = rs.getObject(key, type);
            con.close();
            return value;
        } catch (SQLException exception) {
            log.error(String.format("Failed to get clan %s", id), exception);
            return null;
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
                    String.format("UPDATE \"clanMember\" SET \"%s\" = ? WHERE \"id\" = ?", key));
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
    public Timestamp getVerificationDate() {
        return get("verificationTime", Timestamp.class);
    }

    @Override
    public void setVerificationDate(@Nonnull Timestamp verificationDate) {
        set("verificationTime", verificationDate);
    }

    @Override
    public String getNickname() {
        return get("nickname", String.class);
    }

    @Override
    public void setNickname(@Nonnull String nickname) {
        set("nickname", nickname);
    }

    @Override
    public int getPermissionsLevel() {
        return get("permission", int.class);
    }

    @Nullable
    @Override
    public Permission getPermission() {
        return Permission.fromLevel(getPermissionsLevel());
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return permission.getLevel() == getPermissionsLevel();
    }

    @Override
    public void addLeadershipPermission(boolean updateDiscordRoles) {
        set("permission", Permission.LEADERSHIP.getLevel());
        if (updateDiscordRoles) new CMUtils().updateLeadershipRole(this, true);
    }

    @Override
    public void addLeadershipPermission() {
        addLeadershipPermission(true);
    }


    @Override
    public void addCoOwnerPermission() {
        set("permission", Permission.CO_OWNER.getLevel());
    }

    @Override
    public void removePermission(@NotNull Permission permission) {
        if (permission == Permission.MEMBER) {
            throw new PermissionExeption("You can't remove the member permission. Use the kick method instead.");
        }
        set("permission", Permission.fromLevel(permission.getLevel() - 1));
    }

    @Override
    public DiscordLocale getLocale() {
        return DiscordLocale.from(get("locale", String.class));
    }

    @Override
    public void setLocale(DiscordLocale locale) {
        set("locale", locale.getLocale());
    }

    @Override
    public int getClanId() {
        return get("clanId", Integer.class);
    }

    @Override
    public Clan getClan() {
        return config.getClanManager().getClan(getClanId());
    }

    @Override
    public long getDiscordUserId() {
        return get("discordUserId", Long.class);
    }

    @Override
    public Member getDiscordMember() {
        return getClan().getDiscordGuild().getMemberById(getDiscordUserId());
    }

    @Override
    public CompletableFuture<Member> retrieveDiscordMember() {
        return getClan().getDiscordGuild().retrieveMemberById(getDiscordUserId()).submit();
    }

    @Override
    public void setDiscordMember(Member member) {
        set("discordUserId", member.getIdLong());
    }

    @NotNull
    @Override
    public ClanManager getClanManager() {
        return config.getClanManager();
    }
}
