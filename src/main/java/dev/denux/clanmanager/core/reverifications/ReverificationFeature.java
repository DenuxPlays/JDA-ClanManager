package dev.denux.clanmanager.core.reverifications;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.utils.CMChecks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReverificationFeature {
    private final static Logger log = JDALogger.getLog(ReverificationFeature.class);

    private final Clan clan;
    private final ClanManagerConfig config;

    public ReverificationFeature(@Nonnull Clan clan, @Nonnull ClanManagerConfig config) {
        if (new CMChecks(config).isReverificationEnabled(clan)) throw new IllegalArgumentException("Reverification is not enabled for this clan");
        this.clan = clan;
        this.config = config;
    }

    public Clan getClan() {
        return clan;
    }

    /**
     * Gets you the value from the key.
     * @param key The column name.
     * @return Returns null if there is an issue with the database.
     */
    @Nullable
    private <T> T get(@NotNull String key, @NotNull Class<T> type) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement(
                    String.format("SELECT \"%s\" FROM \"reverificationFeature\" WHERE \"clanId\" = ?", key));
            pstm.setInt(1, clan.getId());
            ResultSet rs = pstm.executeQuery();
            rs.next();
            T value = rs.getObject(key, type);
            con.close();
            return value;
        } catch (SQLException exception) {
            log.error(String.format("Failed to get clan %s", clan.getId()), exception);
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
                    String.format("UPDATE \"reverificationFeature\" SET \"%s\" = ? WHERE \"clanId\" = ?", key));
            pstm.setObject(1, value);
            pstm.setInt(2, clan.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            log.error("Failed to set database entry.", exception);
        }
    }

    /**
     * @return Returns the number of days to wait before reverification.
     */
    public short getNumberOfDays() {
        return get("numberOfDays", Short.class);
    }

    /**
     * @param numberOfDays The number of days to wait before reverification.
     */
    public void setNumberOfDays(short numberOfDays) {
        if (numberOfDays < 0) throw new IllegalArgumentException("The number of days must be greater than 0.");
        set("numberOfDays", numberOfDays);
    }
}
