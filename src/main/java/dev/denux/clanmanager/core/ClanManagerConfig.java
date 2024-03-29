package dev.denux.clanmanager.core;

import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.core.features.reverifications.BasicReverificationJob;
import dev.denux.clanmanager.core.features.reverifications.ReverificationJob;
import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import net.dv8tion.jda.api.JDA;
import org.hibernate.SessionFactory;

/**
 * Data class which contains the configuration for a single {@link ClanManager} instance.
 */
public class ClanManagerConfig {
    private JDA jda;
    private boolean shouldLoadSchema = true;
    private boolean useOwnH2Database = false;
    private HikariDataSource dataSource;
    private SessionFactory sessionFactory;
    private ClanManager clanManager;
    private Class<? extends BasicReverificationJob> reverificationJobImpl = ReverificationJob.class;
    private ReverificationStateManager reverificationManager;
    private String queries =
            "CREATE TABLE IF NOT EXISTS \"clan\" (\n" +
                    "    \"id\" SERIAL PRIMARY KEY,\n" +
                    "    \"verificationCode\" TEXT NOT NULL UNIQUE,\n" +
                    "    \"name\" TEXT NOT NULL,\n" +
                    "    \"tag\" TEXT NOT NULL,\n" +
                    "    \"ownerId\" INT NOT NULL,\n" +
                    "    \"ownerUserId\" BIGINT NOT NULL,\n" +
                    "    \"discordGuildId\" BIGINT NOT NULL,\n" +
                    "    \"leaderShipRoleId\" BIGINT NOT NULL,\n" +
                    "    \"memberRoleId\" BIGINT NOT NULL,\n" +
                    "    \"discordChannelId\" BIGINT NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS \"clanMember\" (\n" +
                    "    \"id\" SERIAL PRIMARY KEY,\n" +
                    "    \"verificationTime\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    \"nickname\" TEXT NOT NULL,\n" +
                    "    \"permission\" TEXT NOT NULL DEFAULT 'MEMBER',\n" +
                    "    \"locale\" TEXT NOT NULL,\n" +
                    "    \"clanId\" INT NOT NULL,\n" +
                    "    \"discordUserId\" BIGINT NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS \"reverificationFeature\" (\n" +
                    "    \"clanId\" INT PRIMARY KEY,\n" +
                    "    \"numberOfDays\" SMALLINT NOT NULL DEFAULT 90\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS \"blockedUsers\" (\n" +
                    "    \"clanId\" INT UNIQUE,\n" +
                    "    \"discordUserId\" BIGINT UNIQUE,\n" +
                    "    PRIMARY KEY (\"clanId\", \"discordUserId\")\n" +
                    ");";


    public boolean isUseOwnH2Database() {
        return useOwnH2Database;
    }

    public void setUseOwnH2Database(boolean useOwnH2Database) {
        this.useOwnH2Database = useOwnH2Database;
    }

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

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}