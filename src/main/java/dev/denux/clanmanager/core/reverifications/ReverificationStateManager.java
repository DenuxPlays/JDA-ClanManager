package dev.denux.clanmanager.core.reverifications;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.entities.ClanMember;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Handels the reverification process.
 */
public class ReverificationStateManager {
    private static final Logger log = JDALogger.getLog(ReverificationFeature.class);

    private Scheduler scheduler;
    private final ClanManagerConfig config;

    public ReverificationStateManager(ClanManagerConfig config) {
        this.config = config;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            List<Integer> clanMembers = new ArrayList<>();
            try(Connection con = config.getDataSource().getConnection()) {
                PreparedStatement pstm = con.prepareStatement("SELECT DISTINCT \"clanMember\".\"id\" FROM \"clanMember\", \"clan\", \"reverificationFeature\" WHERE \"clan\".\"id\" = \"reverificationFeature\".\"clanId\"");
                ResultSet rs = pstm.executeQuery();
                while (rs.next()) {
                    clanMembers.add(rs.getInt(1));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            if (clanMembers.isEmpty()) return;
            for (int clanMemberId : clanMembers) {
                ClanMember clanMember = config.getClanManager().getClanMember(clanMemberId);
                if (clanMember == null) continue;
                scheduleReverification(clanMember);
            }
        } catch (SchedulerException exception) {
            log.error("Failed to start scheduler for reverification feature.", exception);
        }
    }

    /**
     * Schedules a clan member for reverification.
     * @param clanMember The clan member to schedule.
     */
    public void  scheduleReverification(ClanMember clanMember) {
        ReverificationFeature feature;
        try {
            feature = new ReverificationFeature(clanMember.getClan(), config);
        } catch (IllegalArgumentException exception) {
            log.debug("Failed to create reverification feature for clan member {}.", clanMember.getId());
            return;
        }
        JobDetail job = newJob(ReverificationJob.class)
                .withIdentity(String.valueOf(clanMember.getId()))
                .build();
        Date date = Date.from(clanMember.getVerificationDate().toInstant().plus(feature.getNumberOfDays(), ChronoUnit.DAYS));
        Trigger trigger = newTrigger()
                .withIdentity(String.valueOf(clanMember.getId()))
                .startAt(date)
                .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
        try {
            scheduler.getContext().put("config", config);
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Cancels the reverification of a clan member.
     * @param clanMember The clan member to cancel.
     */
    public void cancelSchedule(ClanMember clanMember) {
        try {
            if (!scheduler.checkExists(JobKey.jobKey(String.valueOf(clanMember.getId())))) return;
            scheduler.deleteJob(JobKey.jobKey(String.valueOf(clanMember.getId())));
        } catch (SchedulerException exception) {
            exception.printStackTrace();
        }
    }
}
