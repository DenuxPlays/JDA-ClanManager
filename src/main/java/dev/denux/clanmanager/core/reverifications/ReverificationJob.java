package dev.denux.clanmanager.core.reverifications;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.utils.CMChecks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

public class ReverificationJob extends BasicReverificationJob implements Job {
    private static final Logger log = JDALogger.getLog(ReverificationFeature.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ClanManagerConfig config = (ClanManagerConfig) context.get("config");
        ClanMember clanMember = config.getClanManager().getClanMember(Integer.parseInt(context.getJobDetail().getKey().getName()));
        try {
            new CMChecks(config).isReverificationEnabled(clanMember.getClan());
        } catch (IllegalArgumentException exception) {
            log.warn("Reverification for clan member {} is disabled.", clanMember.getId());
            return;
        }
        try {
            config.getReverificationJobImpl().getConstructor().newInstance().executeJob(config, clanMember);
        } catch (ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void executeJob(@NotNull ClanManagerConfig config, @NotNull ClanMember clanMember) {
        clanMember.retrieveDiscordMember().thenAccept(m ->
            m.getUser().openPrivateChannel().queue(c -> c.sendMessage(String.format("Pleas e verify yourself again for the clan: `%s`", clanMember.getClan().getId())).queue())
        );
        clanMember.getClan().deleteClanMember(clanMember);
    }
}
