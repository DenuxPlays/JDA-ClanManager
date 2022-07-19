package dev.denux.clanmanager.core;

import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.reverifications.BasicReverificationJob;
import dev.denux.clanmanager.core.reverifications.ReverificationJob;
import dev.denux.clanmanager.core.reverifications.ReverificationStateManager;
import dev.denux.clanmanager.internal.ClanManagerImpl;
import net.dv8tion.jda.api.JDA;

/**
 * Data class which contains the configuration for a single {@link ClanManager} instance.
 */
public class ClanManagerConfig {
    private JDA jda;
    private boolean shouldLoadSchema = true;
    private HikariDataSource dataSource;
    private ClanManager clanManager;
    private Class<? extends ClanManager> CMImplementation = ClanManagerImpl.class;
    private Class<? extends BasicReverificationJob> reverificationJobImpl = ReverificationJob.class;
    private ReverificationStateManager reverificationManager;
    private String queries = "CREATE TABLE IF NOT EXISTS \"clan\" (\n" +
            "    \"id\" SERIAL PRIMARY KEY,\n" +
            "    \"verificationCode\" TEXT NOT NULL UNIQUE,\n" +
            "    \"name\" TEXT NOT NULL,\n" +
            "    \"tag\" TEXT NOT NULL,\n" +
            "    \"discordGuildId\" BIGINT NOT NULL,\n" +
            "    \"ownerId\" BIGINT NOT NULL,\n" +
            "    \"leaderShipRoleId\" INT NOT NULL,\n" +
            "    \"memberRoleId\" BIGINT NOT NULL,\n" +
            "    \"discordChannelId\" BIGINT NOT NULL\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE IF NOT EXISTS \"clanMember\" (\n" +
            "    \"id\" SERIAL PRIMARY KEY,\n" +
            "    \"verificationTime\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "    \"nickname\" TEXT NOT NULL,\n" +
            "    \"leaderShipStatus\" BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "    \"coOwnerStatus\" BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "    \"locale\" TEXT NOT NULL,\n" +
            "    \"clanId\" INT NOT NULL,\n" +
            "    \"discordUserId\" BIGINT NOT NULL\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE IF NOT EXISTS \"reverificationFeature\" (\n" +
            "    \"clanId\" INT PRIMARY KEY,\n" +
            "    \"numberOfDays\" SMALLINT NOT NULL DEFAULT 90\n" +
            ");";


    public Class<? extends BasicReverificationJob> getReverificationJobImpl() {
        return reverificationJobImpl;
    }

    public void setReverificationJobImpl(Class<? extends BasicReverificationJob> reverificationJobImpl) {
        this.reverificationJobImpl = reverificationJobImpl;
    }

    public ReverificationStateManager getReverificationManager() {
        return reverificationManager;
    }

    public void setReverificationManager(ReverificationStateManager reverificationManager) {
        this.reverificationManager = reverificationManager;
    }

    public String getQueries() {
        return queries;
    }

    public void setQueries(String queries) {
        this.queries = queries;
    }

    public Class<? extends ClanManager> getCMImplementation() {
        return CMImplementation;
    }

    public void setCMImplementation(Class<? extends ClanManager> CMImplementation) {
        this.CMImplementation = CMImplementation;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public void setClanManager(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public boolean shouldLoadSchema() {
        return shouldLoadSchema;
    }

    public void setShouldLoadSchema(boolean shouldLoadSchema) {
        this.shouldLoadSchema = shouldLoadSchema;
    }

    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
}