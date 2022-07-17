package dev.denux.clanmanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.SystemSetup;
import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;

/**
 * Build system to initialize a {@link ClanManager} instance.
 */
public class ClanManagerBuilder {

    private final ClanManagerConfig config;

    private ClanManagerBuilder(@Nonnull JDA jda) {
        this.config = new ClanManagerConfig();
        this.config.setJda(jda);
    }

    /**
     * Sets the {@link JDA} instance the manager will use.
     *
     * @param jda The JDA instance to use.
     */
    public static ClanManagerBuilder setJDA(JDA jda) {
        return new ClanManagerBuilder(jda);
    }

    /**
     * Sets the {@link HikariDataSource} instance the manager will use to connect to the database.
     *
     * @param dataSource The HikariDataSource instance to use.
     */
    @Nonnull
    public ClanManagerBuilder setDataSource(@Nonnull HikariDataSource dataSource) {
        config.setDataSource(dataSource);
        return this;
    }

    /**
     * Creates a default {@link HikariConfig} instance.
     *
     * @param jdbcUrl The JDBC URL to your Database.
     * @see ClanManagerBuilder#setDataSource(HikariDataSource)
     */
    @Nonnull
    public ClanManagerBuilder setJdbcUrl(@Nonnull String jdbcUrl) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        config.setDataSource(dataSource);
        return this;
    }

    /**
     * Sets a new implementation for the {@link ClanManager} interface.
     * @param CMImplementation The implementation to use.
     */
    @Nonnull
    public ClanManagerBuilder setClanManagerImplementation(@Nonnull Class<? extends ClanManager> CMImplementation) {
        config.setCMImplementation(CMImplementation);
        return this;
    }

    /**
     * Disables the schema loading.
     * It's not recommended to use this if you don't know what you're doing.
     * This will break the system without compensation.
     */
    @Nonnull
    public ClanManagerBuilder disableSchemaLoading() {
        config.setShouldLoadSchema(false);
        return this;
    }

    /**
     * Returns a {@link ClanManager} instance that has been validated.
     * @return the built, usable {@link ClanManager}
     * @throws ReflectiveOperationException If your {@link ClanManager} implementation has an invalid constructor.
     */
    @Nonnull
    public ClanManager build() throws ReflectiveOperationException {
        new SystemSetup(config).init();
        return config.getCMImplementation().getDeclaredConstructor(ClanManagerConfig.class).newInstance(config);
    }
}
