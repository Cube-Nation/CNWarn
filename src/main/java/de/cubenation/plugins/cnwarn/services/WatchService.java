package de.cubenation.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

import de.cubenation.plugins.cnwarn.model.Watch;

/**
 * With this service, player watches can be managed.
 * 
 * @since 1.1
 */
public class WatchService {
    // external services
    private final EbeanServer conn;
    private final Logger log;

    /**
     * Initial with external services.
     * 
     * @param conn
     *            EbeanServer for database connection
     * @param log
     *            Logger for unexpected errors
     * 
     * @since 1.1
     */
    public WatchService(EbeanServer conn, Logger log) {
        this.conn = conn;
        this.log = log;
    }

    /**
     * Checks if the player is under observation.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if the player is under observation. Otherwise False. Also
     *         false, if the playerName is null or empty.
     * 
     * @since 1.1
     */
    public final boolean isPlayerInWatchList(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        Watch watchedPlayer = conn.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
        return !(watchedPlayer == null);
    }

    /**
     * Adds a player to the watch list.
     * 
     * @param playerName
     *            player name that is under observation
     * @param description
     *            why is the player under observation
     * @param staffName
     *            player name that watched the player
     * @return True, if saving the watch was successful. Otherwise false. Also
     *         false, if the playerName or staffName is empty or null.
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

        Transaction transaction = conn.beginTransaction();
        try {
            conn.save(watch, transaction);
            transaction.commit();
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on add watch", e);
            transaction.rollback();

            return false;
        } finally {
            transaction.end();
        }

        return true;
    }

    /**
     * Find watch for a player by id.
     * 
     * @param id
     *            watch id
     * @return The found watch. If not found, null.
     * 
     * @since 1.1
     */
    public final Watch getWatchedPlayerById(int id) {
        return conn.find(Watch.class).setMaxRows(1).where().idEq(id).findUnique();
    }

    /**
     * Find watch for a player by name.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return The found watch. If not found, null. Also null if playerName is
     *         empty or null.
     * 
     * @since 1.1
     */
    public final Watch getWatchedPlayerByName(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return null;
        }
        return conn.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    /**
     * Deletes a player from watch list.
     * 
     * @param watch
     *            the watch to delete.
     * @return True, if the deletion was successful. Otherwise false. Also
     *         false, if the watch is null.
     * 
     * @since 1.1
     */
    public final boolean deletePlayerWatch(Watch watch) {
        if (watch == null) {
            return false;
        }

        Transaction transaction = conn.beginTransaction();
        try {
            conn.delete(watch, transaction);
            transaction.commit();
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on delete watch", e);
            transaction.rollback();

            return false;
        } finally {
            transaction.end();
        }

        return true;
    }

    /**
     * Return all available watches.
     * 
     * @return List with watches
     * 
     * @since 1.1
     */
    public final List<Watch> getAllWatches() {
        return conn.find(Watch.class).findList();
    }
}
