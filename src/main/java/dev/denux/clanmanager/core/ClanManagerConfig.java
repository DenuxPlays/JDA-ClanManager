package dev.denux.clanmanager.core;

import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.ClanManager;
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
