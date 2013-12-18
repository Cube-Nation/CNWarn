package de.derflash.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;

import javax.persistence.OptimisticLockException;

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

    public final boolean addWatch(String playerName, String description, String staffName) {
        if (playerName == null || playerName.isEmpty() || staffName == null || staffName.isEmpty()) {
            return false;
        }

        Watch watch = new Watch();
        watch.setPlayerName(playerName);
        watch.setMessage(description);
        watch.setCreated(new Date());
        watch.setStaffName(staffName);

        try {
            dbConnection.save(watch);
        } catch (OptimisticLockException e) {
            return false;
        }

        return true;
    }

    public final Watch getWatchedPlayerById(int id) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().idEq(id).findUnique();
    }

    public final Watch getWatchedPlayerByName(String playerName) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    public final boolean deletePlayerWatch(Watch watch) {
        if (watch == null) {
            return false;
        }

        try {
            dbConnection.delete(watch);
        } catch (OptimisticLockException e) {
            return false;
        }

        return true;
    }

    public List<Watch> getAllWatches() {
        return dbConnection.find(Watch.class).findList();
    }
}
