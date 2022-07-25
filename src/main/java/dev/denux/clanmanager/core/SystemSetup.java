package dev.denux.clanmanager.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.h2.tools.Server;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple class to set up all things needed for the manager.
 */
public class SystemSetup {
    private final ClanManagerConfig config;

    public SystemSetup(@Nonnull ClanManagerConfig config) {
        this.config = config;
    }

    private final static Logger log = JDALogger.getLog(SystemSetup.class);

    /**
     * Setting up essential things for the manager.
     */
    public void init() {
        log.info("Initializing ClanManager...");

        if (config.shouldLoadSchema()) {
            initSchema();
            log.info("\t[*] Schema init done.");
        }

        config.setReverificationManager(new ReverificationStateManager(config));
        log.info("\t[*] Reverification setup done.");

        log.info("Finished initializing ClanManager.");
    }

    /**
     * Set's up the H2 Database.
     */
    public void setupH2Database(@Nonnull HikariConfig hConfig) {
        Server server;
        try {
            server = Server.createTcpServer("-tcpPort", "9123", "-ifNotExists").start();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not start database server.", exception);
        }

        hConfig.setJdbcUrl("jdbc:h2:tcp://localhost:9123/./clanmanager");
        config.setDataSource(new HikariDataSource(hConfig));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.getDataSource().close();
            server.stop();
        }));
    }

    /**
     * Initializes the database schema.
     */
    private void initSchema() {
        try(Connection con = config.getDataSource().getConnection()) {
            List<String> queries = Arrays.stream(config.getQueries().split(";")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            log.debug("\t\t[*] Executing {} queries.", queries.size());
            for (String query : queries) {
                log.debug("\t\t[*] Executing query {}/{} \n{}", queries.indexOf(query) + 1, queries.size(), query);
                con.prepareStatement(query).executeUpdate();
            }
        } catch (SQLException exception) {
            log.error("Could not set up the database.", exception);
        }
    }
}
