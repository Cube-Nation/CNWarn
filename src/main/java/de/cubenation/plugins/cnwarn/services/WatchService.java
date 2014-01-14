package de.cubenation.plugins.cnwarn.services;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.Validate;

import com.avaje.ebean.EbeanServer;

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
     * @return True, if the player is under observation, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean isPlayerInWatchList(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

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
     * @return True, if saving the watch was successful, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean addWatch(String playerName, String description, String staffName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");
        Validate.notEmpty(staffName, "staff name cannot be null or empty");

        Watch watch = new Watch();
        watch.setPlayerName(playerName);
        watch.setMessage(description);
        watch.setCreated(new Date());
        watch.setStaffName(staffName);

        try {
            conn.save(watch);
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on add watch", e);

            return false;
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
     * @return The found watch. If not found, null.
     * 
     * @since 1.1
     */
    public final Watch getWatchedPlayerByName(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");
        return conn.find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
    }

    /**
     * Deletes a player from watch list.
     * 
     * @param watch
     *            the watch to delete.
     * @return True, if the deletion was successful, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean deletePlayerWatch(Watch watch) {
        Validate.notNull(watch, "watch cannot be null or empty");

        try {
            conn.delete(watch);
        } catch (PersistenceException e) {
            log.log(Level.SEVERE, "error on delete watch", e);

            return false;
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
