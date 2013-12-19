package de.derflash.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;

import com.avaje.ebean.EbeanServer;

import de.derflash.plugins.cnwarn.model.Watch;

/**
 * 
 * @since 1.1
 */
public class WatchService {
    // external services
    private final EbeanServer dbConnection;
    private final Logger logger;

    /**
     * 
     * @param dbConnection
     * @param logger
     * 
     * @since 1.1
     */
    public WatchService(EbeanServer dbConnection, Logger logger) {
        this.dbConnection = dbConnection;
        this.logger = logger;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final boolean isPlayerInWatchList(String playerName) {
        Watch watchedPlayer = dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
        return !(watchedPlayer == null);
    }

    /**
     * 
     * @param playerName
     * @param description
     * @param staffName
     * @return
     * 
     * @since 1.1
     */
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
            logger.log(Level.SEVERE, "error on add watch", e);
            return false;
        }

        return true;
    }

    /**
     * 
     * @param id
     * @return
     * 
     * @since 1.1
     */
    public final Watch getWatchedPlayerById(int id) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().idEq(id).findUnique();
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final Watch getWatchedPlayerByName(String playerName) {
        return dbConnection.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    /**
     * 
     * @param watch
     * @return
     * 
     * @since 1.1
     */
    public final boolean deletePlayerWatch(Watch watch) {
        if (watch == null) {
            return false;
        }

        try {
            dbConnection.delete(watch);
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "error on delete watch", e);
            return false;
        }

        return true;
    }

    /**
     * 
     * @return
     * 
     * @since 1.1
     */
    public List<Watch> getAllWatches() {
        return dbConnection.find(Watch.class).findList();
    }
}
