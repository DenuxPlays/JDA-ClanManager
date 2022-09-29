package dev.denux.clanmanager.internal.entities;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.exceptions.PermissionException;
import dev.denux.clanmanager.internal.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

public class ClanMember {

    private final Logger log = JDALogger.getLog(Clan.class);

    private final int id;
    private final ClanManagerConfig config;

    public ClanMember(ClanManagerConfig config, int id) {
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

    public int getId() {
        return id;
    }

    public Timestamp getVerificationDate() {
        return get("verificationTime", Timestamp.class);
    }

    public void setVerificationDate(@Nonnull Timestamp verificationDate) {
        set("verificationTime", verificationDate);
    }

    public String getNickname() {
        return get("nickname", String.class);
    }

    public void setNickname(@Nonnull String nickname) {
        set("nickname", nickname);
    }

    @Nonnull
    public Permission getPermission() {
        return Permission.valueOf(get("permission", String.class));
    }

    public boolean hasPermission(@Nonnull Permission permission) {
        return permission.getLevel() > getPermission().getLevel();
    }

    public void removePermission(@Nonnull Permission permission) {
        if (permission == Permission.MEMBER) {
            throw new PermissionException("You can't remove the member permission. Use the kick method instead.");
        }
        if (permission == Permission.OWNER) {
            throw new PermissionException("Owner can't be removed it can just be changed. Use Clan.changeOwner() for that.");
        }
        set("permission", permission);
    }

    public DiscordLocale getLocale() {
        return DiscordLocale.from(get("locale", String.class));
    }

    public void setLocale(@Nonnull DiscordLocale locale) {
        set("locale", locale.getLocale());
    }

    public int getClanId() {
        return get("clanId", Integer.class);
    }

    public Clan getClan() {
        return config.getClanManager().getClan(getClanId());
    }

    public long getDiscordUserId() {
        return get("discordUserId", Long.class);
    }

    public Member getDiscordMember() {
        return getClan().getDiscordGuild().getMemberById(getDiscordUserId());
    }

    public CompletableFuture<Member> retrieveDiscordMember() {
        return getClan().getDiscordGuild().retrieveMemberById(getDiscordUserId()).submit();
    }

    public void setDiscordMember(@Nonnull Member member) {
        set("discordUserId", member.getIdLong());
    }

    @Nonnull
    public ClanManager getClanManager() {
        return config.getClanManager();
    }
}
