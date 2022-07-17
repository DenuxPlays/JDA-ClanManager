package dev.denux.clanmanager.internal;

import dev.denux.clanmanager.ClanManager;
import dev.denux.clanmanager.entities.Clan;
import dev.denux.clanmanager.entities.ClanMember;
import dev.denux.clanmanager.core.ClanManagerConfig;
import dev.denux.clanmanager.internal.entities.ClanImpl;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanManagerImpl implements ClanManager {

    private final ClanManagerConfig config;

    public ClanManagerImpl(ClanManagerConfig config) {
        this.config = config;
        config.setClanManager(this);
    }

    @Override
    public Clan getClan(int id) {
        return new ClanImpl(config, id);
    }

    @Override
    public Clan getClanByVerificationCode(@NotNull String code) {
        try(Connection con = config.getDataSource().getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT \"id\" FROM \"clan\" WHERE \"verificationCode\" = ?");
            ResultSet rs = pstm.executeQuery();
            if(!rs.next()) {
                throw new IllegalArgumentException("Clan with the given code does not exist.");
            }
            return getClan(rs.getInt("id"));
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public ClanMember getClanMember(int id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
