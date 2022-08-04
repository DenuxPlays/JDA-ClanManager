package dev.denux.clanmanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.core.SystemSetup;
import dev.denux.clanmanager.core.features.reverifications.BasicReverificationJob;
import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;

/**
 * Build system to initialize a {@link ClanManager} instance.
 */
public class ClanManagerBuilder {

    private final ClanManagerConfig config;
    private HikariConfig hikariConfig = new HikariConfig();
    private ClanManagerBuilder(@Nonnull JDA jda) {
        this.config = new ClanManagerConfig();
        this.config.setJda(jda);
    }

    /**
     * Sets the {@link JDA} instance the manager will use.
     *
     * @param jda The JDA instance to use.
     */
    public static ClanManagerBuilder setJDA(@Nonnull JDA jda) {
        return new ClanManagerBuilder(jda);
    }

    /**
     * Sets the {@link HikariDataSource} instance the manager will use to connect to the database.
     *
     * @param dataSource The HikariDataSource instance to use.
     * @see ClanManagerBuilder#setJdbcUrl(String)
     * @see ClanManagerBuilder#enableOwnDatabase()
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
     * @see ClanManagerBuilder#enableOwnDatabase()
     */
    @Nonnull
    public ClanManagerBuilder setJdbcUrl(@Nonnull String jdbcUrl) {
        hikariConfig.setJdbcUrl(jdbcUrl);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        config.setDataSource(dataSource);
        Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));
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
     * Sets a new implementation for the {@link BasicReverificationJob} interface.
     * @param reverificationJobImpl The implementation to use.
     */
    @Nonnull
    public ClanManagerBuilder setReverificationJobImplementation(@Nonnull Class<? extends BasicReverificationJob> reverificationJobImpl) {
        config.setReverificationJobImpl(reverificationJobImpl);
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

    @Nonnull
    public ClanManagerBuilder setOwnSchema(@Nonnull String schema) {
        config.setQueries(schema);
        return this;
    }

    /**
     * Enables the system to use our own H2 Database.
     * Overrides every jdbcUrl you've maybe set earlier or later.
     * @see ClanManagerBuilder#setOwnHikariConfig(HikariConfig)
     */
    @Nonnull
    public ClanManagerBuilder enableOwnDatabase() {
        config.setUseOwnH2Database(true);
        return this;
    }

    /**
     * Enables the system to use your hikari setting with our H2 Database.
     * This can only be used together whit {@link ClanManagerBuilder#enableOwnDatabase()}.
     * @param config The HikariConfig instance to use.
     */
    @Nonnull
    public ClanManagerBuilder setOwnHikariConfig(@Nonnull HikariConfig config) {
        if (!this.config.isUseOwnH2Database()) {
            throw new IllegalStateException("You can only use this method after calling ClanManagerBuilder#enableOwnDatabase()");
        }
        hikariConfig = config;
        return this;
    }

    /**
     * Returns a {@link ClanManager} instance that has been validated.
     * @return the built, usable {@link ClanManager}
     * @throws ReflectiveOperationException If your {@link ClanManager} implementation has an invalid constructor.
     */
    @Nonnull
    public ClanManager build() throws ReflectiveOperationException {
        if (config.isUseOwnH2Database()) {
            new SystemSetup(config).setupH2Database(hikariConfig);
        }

        if (config.getJda() == null) throw new IllegalStateException("JDA instance is null");
        if (config.getDataSource() == null) throw new IllegalStateException("DataSource instance is null");
        if (config.getDataSource().getJdbcUrl().isEmpty() || config.getDataSource().getJdbcUrl().isBlank()) {
            throw new IllegalArgumentException("You need to set a JDBC URL before building the ClanManager!");
        }
        if (config.getQueries().isEmpty() || config.getQueries().isBlank() ||config.getQueries() == null) {
            throw new IllegalArgumentException("You need to set a queries before building the ClanManager!");
        }

        new SystemSetup(config).init();
        return config.getCMImplementation().getDeclaredConstructor(ClanManagerConfig.class).newInstance(config);
    }
}
