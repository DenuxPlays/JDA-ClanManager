package dev.denux.clanmanager.core;

import dev.denux.clanmanager.core.features.reverifications.ReverificationStateManager;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

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

    public SystemSetup(ClanManagerConfig config) {
        this.config = config;
    }

    private final static Logger log = JDALogger.getLog(SystemSetup.class);

    /**
     * Setting up essential things for the manager.
     */
    public void init() {
        log.info("Initializing ClanManager...");

        if (config.shouldLoadSchema()) {
            setUpDatabase();
            log.info("\t[*] Database setup done.");
        }

        config.setReverificationManager(new ReverificationStateManager(config));
        log.info("\t[*] Reverification setup done.");

        log.info("Finished initializing ClanManager.");
    }

    /**
     * Sets up all the needed tables and checks the general connection to the database.
     */
    private void setUpDatabase() {
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
