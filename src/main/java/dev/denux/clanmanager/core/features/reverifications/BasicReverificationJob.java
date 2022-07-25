package dev.denux.clanmanager.core.features.reverifications;

import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.entities.ClanMember;

import javax.annotation.Nonnull;

public interface BasicReverificationJob {

    default void executeJob(@Nonnull ClanManagerConfig config, @Nonnull ClanMember clanMember) {}
}
