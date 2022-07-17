package dev.denux.clanmanager;

import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;

import javax.annotation.Nonnull;

/**
 * The core of this manager.
 *
 * <pre>{@code
 * ClanManager clanManager = ClanManagerBuilder
 *             .setJDA(jda) // Your JDA instance
 *             .setJdbcUrl("jdbc:postgresql://localhost:5432/clanManager") // Your Jdbc URL to your SQL Database
 *             .build();
 * }</pre>
 *
 * @see ClanManagerBuilder
 */
public interface ClanManager {

    /**
     * @param id The id of the clan.
     * @return The {@link Clan} with the given id.
     * @throws IllegalArgumentException If the clan with the given id does not exist.
     */
    Clan getClan(int id);

    /**
     * @param code The code of the clan.
     * @return The {@link Clan} with the given code.
     */
    Clan getClanByVerificationCode(@Nonnull String code);

    /**
     * @param id The id of the clan member.
     * @return The {@link ClanMember} with the given id.
     * @throws IllegalArgumentException If the clan with the given id does not exist.
     */
    ClanMember getClanMember(int id);
}
