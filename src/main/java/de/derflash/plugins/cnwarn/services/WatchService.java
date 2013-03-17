package de.derflash.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;

import com.avaje.ebean.EbeanServer;

import de.derflash.plugins.cnwarn.model.Watch;

public class WatchService {
    private EbeanServer dbConnection;

    public WatchService(EbeanServer dbConnection) {
        this.dbConnection = dbConnection;
    }

    public boolean isPlayerInWatchList(String playerName) {
        Watch watchedPlayer = dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
        return !(watchedPlayer == null);
    }

    public void addWatch(String playerName, String description, String staffName) {
        Watch watch = new Watch();
        watch.setPlayername(playerName);
        watch.setMessage(description);
        watch.setCreated(new Date());
        watch.setStaffname(staffName);
        dbConnection.save(watch);
    }

    public Watch getWatchedPlayerById(int id) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().eq("id", id).findUnique();
    }

    public Watch getWatchedPlayerByName(String playerName) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    public void deletePlayerWatch(Watch watch) {
        dbConnection.delete(watch);
    }

    public List<Watch> getAllWatches() {
        return dbConnection.find(Watch.class).findList();
    }
}
