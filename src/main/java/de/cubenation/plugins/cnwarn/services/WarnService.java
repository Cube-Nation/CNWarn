package de.cubenation.plugins.cnwarn.services;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.Validate;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import de.cubenation.plugins.cnwarn.model.Warn;
import de.cubenation.plugins.cnwarn.model.exception.WarnNotFoundException;
import de.cubenation.plugins.cnwarn.model.exception.WarnsNotFoundException;
import de.cubenation.plugins.utils.ArrayConvert;
import de.cubenation.plugins.utils.BukkitUtils;

/**
 * With this service, player warnings can be managed. Players warnings have a
 * expiration time that is configurable, default 30 days.
 * 
 * @since 1.1
 */
public class WarnService {
    // external services
    private final EbeanServer conn;
    private final Logger log;

    // Hashset all (!online!) players with not accepted warnings
    private final HashSet<String> notAcceptedWarnedPlayerCache = new HashSet<String>();

    private int expirationDays = 30;

    private final SqlQuery sqlSumRating;
    private final SqlQuery sqlSearchWarnedPlayer;
    private final SqlQuery sqlLogBlockPlayer;

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
    public WarnService(EbeanServer conn, Logger log) {
        this.conn = conn;
        this.log = log;

        sqlSumRating = conn.createSqlQuery("select sum(`rating`) as sumrating from `cn_warns` where lower(`playername`) = lower(:playerName) limit 1");
        sqlSearchWarnedPlayer = conn
                .createSqlQuery("select distinct `playername` as playername from `cn_warns` where `playername` like :playerName order by `playername` limit 8");
        sqlLogBlockPlayer = conn.createSqlQuery("select * from `lb-players` where lower(`playername`) = lower(:playerName)");
    }

    /**
     * Set for expired warns rating value to zero.
     * 
     * @return count of warns that was cleared. On error -1 will be returned.
     * 
     * @since 1.1
     */
    public final int clearExpired() {
        Calendar cal = new GregorianCalendar();
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.add(Calendar.DAY_OF_MONTH, -expirationDays);

        List<Warn> findList = conn.find(Warn.class).where().gt("rating", 0).lt("accepted", cal.getTime()).findList();
        for (Warn find : findList) {
            find.setRating(0);
        }

        try {
            conn.save(findList.iterator());
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on expired warns", e);

            return -1;
        }

        return findList.size();
    }

    /**
     * Return the count of warns for a player.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return count of warns
     * 
     * @since 1.1
     */
    public final int getWarnCount(String playerName) {
        return conn.find(Warn.class).where().ieq("playername", playerName).findRowCount();
    }

    /**
     * Return the total sum ratings from all player warns.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return sum of warn ratings
     * 
     * @since 1.1
     */
    public final int getRatingSum(String playerName) {
        sqlSumRating.setParameter("playerName", playerName);

        Integer retInt = sqlSumRating.findUnique().getInteger("sumrating");
        return retInt == null ? 0 : retInt;
    }

    /**
     * Add a warn for a player. If player is only add cached not acceped warn
     * too.
     * 
     * @param warnedPlayerName
     *            player name that is warned
     * @param staffMemberName
     *            player name that warned the player
     * @param message
     *            warn reason
     * @param rating
     *            the heaviness of the warn
     * @return True, if warn was added.
     * 
     * @since 1.1
     */
    public final boolean addWarn(String warnedPlayerName, String staffMemberName, String message, Integer rating) {
        Validate.notEmpty(warnedPlayerName, "warned player name cannot be null or empty");
        Validate.notEmpty(staffMemberName, "staff member name cannot be null or empty");

        Warn newWarn = new Warn();
        newWarn.setPlayerName(warnedPlayerName);
        newWarn.setStaffName(staffMemberName);
        newWarn.setMessage(message);
        newWarn.setRating(rating);
        newWarn.setCreated(new Date());

        try {
            conn.save(newWarn);
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on save warn", e);

            return false;
        }

        cacheNotAcceptedWarns(warnedPlayerName);

        return true;
    }

    /**
     * Delete warn by id. Remove cached not accepted warns for the player.
     * 
     * @param id
     *            warn id
     * @return True, if the warn was deleted successful. False, if the warn
     *         could not be deleted.
     * @throws WarnNotFoundException
     *             if warn could not found in database
     * 
     * @since 1.1
     */
    public final boolean deleteWarn(int id) throws WarnNotFoundException {
        Warn warn = conn.find(Warn.class, id);
        if (warn == null) {
            throw new WarnNotFoundException(id);
        }

        try {
            conn.delete(warn);
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on delete warn", e);

            return false;
        }

        removeCachedNotAcceptedWarns(warn.getPlayerName());

        return true;
    }

    /**
     * Delete all warns for a player. Remove cached not accepted warns for the
     * player.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if warns was deleted successful.
     * @throws WarnsNotFoundException
     *             if no warns found for player
     * 
     * @since 1.1
     */
    public final boolean deleteWarns(String playerName) throws WarnsNotFoundException {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        Set<Warn> warns = conn.find(Warn.class).where().ieq("playername", playerName).findSet();
        if (warns.size() == 0) {
            throw new WarnsNotFoundException(playerName);
        }

        try {
            conn.delete(warns.iterator());
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on delete warns", e);

            return false;
        }

        removeCachedNotAcceptedWarns(playerName);

        return true;
    }

    /**
     * Accept all non accepted warns for the player. Remove cached not accepted
     * warns for the player.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if the warns acceptance successful save. False, if saving
     *         is failed.
     * @throws WarnsNotFoundException
     *             if no warn exists for acceptance
     * 
     * @since 1.1
     */
    public final boolean acceptWarns(String playerName) throws WarnsNotFoundException {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        Set<Warn> unAccWarns = conn.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
        if (unAccWarns.size() == 0) {
            throw new WarnsNotFoundException(playerName);
        }

        for (Warn warn : unAccWarns) {
            warn.setAccepted(new Date());
        }

        try {
            conn.save(unAccWarns.iterator());
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on accept warn", e);

            return false;
        }

        removeCachedNotAcceptedWarns(playerName);

        return true;
    }

    /**
     * Check if the player has not accepted warns (accepted date is not set)
     * against database.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if not accepted warns exists in database, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean hasPlayerNotAcceptedWarns(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        return conn.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
    }

    /**
     * Check if the player has not accepted warns (accepted date is not set)
     * against database.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if not accepted warns exists in database, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean isPlayersWarned(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        return conn.find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
    }

    /**
     * Search player names with warns.
     * 
     * @param searchPattern
     *            not case-sensitive player name, wildcards '%' can be used.
     * @return List of player names that was found for the searchPattern and has
     *         warns.
     * 
     * @since 1.1
     */
    public final Collection<String> searchPlayerWithWarns(String searchPattern) {
        Validate.notEmpty(searchPattern, "search pattern cannot be null or empty");

        sqlSearchWarnedPlayer.setParameter("playerName", "%" + searchPattern + "%");
        List<SqlRow> found = sqlSearchWarnedPlayer.findList();

        ArrayConvert<SqlRow> wc = new ArrayConvert<SqlRow>() {
            @Override
            protected String convertToString(SqlRow obj) {
                return obj.getString("playername");
            }
        };

        return wc.toCollection(found);
    }

    /**
     * Search player warns.
     * 
     * @param searchPattern
     *            not case-sensitive player name, wildcards '%' can be used.
     * @return List of player names that was found for the searchPattern and has
     *         warns.
     * 
     * @since 1.1
     */
    public final List<Warn> getWarnList(String searchPattern) {
        Validate.notEmpty(searchPattern, "search pattern cannot be null or empty");

        return conn.find(Warn.class).where().like("playername", "%" + searchPattern + "%").findList();
    }

    /**
     * Checks, if the player has not accpeted warns against cache.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if the player is online, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean cacheNotAcceptedWarns(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        if (BukkitUtils.isPlayerOnline(playerName)) {
            notAcceptedWarnedPlayerCache.add(playerName.toLowerCase());

            return true;
        }

        return false;
    }

    /**
     * Remove player from not accepted warns cache.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if successful removed.
     * 
     * @since 1.1
     */
    public final boolean removeCachedNotAcceptedWarns(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        notAcceptedWarnedPlayerCache.remove(playerName.toLowerCase());

        return true;
    }

    /**
     * Checks, if the player has not accpeted warns against cache.
     * 
     * @param playerName
     *            not case-sensitive player name
     * @return True, if the player has not accpeted warns, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean hasPlayerNotAcceptedWarnsCached(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        return notAcceptedWarnedPlayerCache.contains(playerName.toLowerCase());
    }

    /**
     * Checks if the player was sometime before joined the server. Detailed it
     * will checked the LogBlock-player table for that, on the same database
     * connection.
     * 
     * If LogBlock-table not exists, fallback looks to player online status.
     * 
     * @param playerName
     * @return True, if the player had was online before, otherwise false.
     * 
     * @since 1.1
     * @see <a
     *      href="http://dev.bukkit.org/bukkit-plugins/logblock/">LogBlock</a>
     */
    public final boolean hasPlayedBefore(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        sqlLogBlockPlayer.setParameter("playerName", playerName);

        try {
            return (sqlLogBlockPlayer.findUnique() != null);
        } catch (PersistenceException e) {
            log.log(Level.SEVERE, "error on query LogBlock Table", e);

            // fallback if player is online
            return BukkitUtils.isPlayerOnline(playerName);
        }
    }

    /**
     * Calculate expiration date for warning against settings.
     * 
     * @param warn
     * @return Return the date or null, if accepted date is null.
     * 
     * @since 1.2
     */
    public final Date calculateExpirationDate(Warn warn) {
        Validate.notNull(warn, "warn cannot be null");

        GregorianCalendar acceptedDate = new GregorianCalendar();
        acceptedDate.setTime(warn.getAccepted());
        acceptedDate.add(Calendar.DAY_OF_MONTH, expirationDays);

        return acceptedDate.getTime();
    }

    /**
     * Return the settings expiration day.
     * 
     * @return Returns expiration days
     * 
     * @since 1.2
     */
    public final int getExpirationDays() {
        return expirationDays;
    }

    /**
     * Set the settings expiration day.
     * 
     * @param expirationDays
     *            Must be greater than 0 otherwise, statment will ignored.
     * @return False, if the expirationDays smaller or equals than 0, otherwise
     *         true
     * 
     * @since 1.2
     */
    public final boolean setExpirationDays(int expirationDays) {
        if (expirationDays <= 0) {
            return false;
        }

        this.expirationDays = expirationDays;

        return true;
    }
}
