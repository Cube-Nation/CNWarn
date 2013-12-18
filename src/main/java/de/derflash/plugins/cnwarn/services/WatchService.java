package de.derflash.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;

import com.avaje.ebean.EbeanServer;

import de.derflash.plugins.cnwarn.model.Watch;

public class WatchService {
    // external services
    private final EbeanServer dbConnection;

    public WatchService(EbeanServer dbConnection) {
        this.dbConnection = dbConnection;
    }

    public final boolean isPlayerInWatchList(String playerName) {
        Watch watchedPlayer = dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
        return !(watchedPlayer == null);
    }

    public final void addWatch(String playerName, String description, String staffName) {
        Watch watch = new Watch();
        watch.setPlayername(playerName);
        watch.setMessage(description);
        watch.setCreated(new Date());
        watch.setStaffname(staffName);
        dbConnection.save(watch);
    }

    public final Watch getWatchedPlayerById(int id) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().eq("id", id).findUnique();
    }

    public final Watch getWatchedPlayerByName(String playerName) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    public final void deletePlayerWatch(Watch watch) {
        dbConnection.delete(watch);
    }

    public List<Watch> getAllWatches() {
        return dbConnection.find(Watch.class).findList();
    }
}
