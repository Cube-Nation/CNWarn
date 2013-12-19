package de.derflash.plugins.cnwarn.services;

import java.util.ArrayList;
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

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import de.cubenation.plugins.utils.ArrayConvert;
import de.cubenation.plugins.utils.BukkitUtils;
import de.derflash.plugins.cnwarn.model.Warn;

/**
 * With this service, player warnings can be managed. Players warnings have a
 * expiration time that is configurable, default 30 days.
 * 
 * @since 1.1
 */
public class WarnService {
    // external services
    private final EbeanServer dbConnection;
    private final Logger logger;

    // Hashset all (!online!) players with not accepted warnings
    private final HashSet<String> notAcceptedWarnedPlayersCache = new HashSet<String>();

    private int expirationDays = 30;

    private final SqlQuery preparedSqlSumRating;
    private final SqlQuery preparedSqlSearchWarnedPlayer;
    private final SqlQuery preparedSqlOfflinePlayer;

    /**
     * 
     * @param dbConnection
     * @param logger
     * 
     * @since 1.1
     */
    public WarnService(EbeanServer dbConnection, Logger logger) {
        this.dbConnection = dbConnection;
        this.logger = logger;

        preparedSqlSumRating = dbConnection
                .createSqlQuery("select sum(`rating`) as sumrating from `cn_warns` where lower(`playername`) = lower(:playerName) limit 1");
        preparedSqlSearchWarnedPlayer = dbConnection
                .createSqlQuery("select distinct `playername` as playername from `cn_warns` where `playername` like :playerName order by `playername` limit 8");
        preparedSqlOfflinePlayer = dbConnection.createSqlQuery("select * from `lb-players` where lower(`playername`) = lower(:playerName)");
    }

    /**
     * Set for expired warns rating value to zero.
     * 
     * @since 1.1
     */
    public final void clearExpired() {
        Calendar cal = new GregorianCalendar();
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.add(Calendar.DAY_OF_MONTH, -expirationDays);

        List<Warn> findList = dbConnection.find(Warn.class).where().gt("rating", 0).lt("accepted", cal.getTime()).findList();
        for (Warn find : findList) {
            find.setRating(0);
        }
        dbConnection.save(findList);
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final int getWarnCount(String playerName) {
        return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount();
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final int getRatingSum(String playerName) {
        preparedSqlSumRating.setParameter("playerName", playerName);

        Integer retInt = preparedSqlSumRating.findUnique().getInteger("sumrating");
        return retInt == null ? 0 : retInt;
    }

    /**
     * 
     * @param warnedPlayerName
     * @param staffMemberName
     * @param message
     * @param rating
     * @return
     * 
     * @since 1.1
     */
    public final boolean addWarn(String warnedPlayerName, String staffMemberName, String message, Integer rating) {
        if (warnedPlayerName == null || staffMemberName == null || warnedPlayerName.isEmpty() || staffMemberName.isEmpty()) {
            return false;
        }

        Warn newWarn = new Warn();
        newWarn.setPlayerName(warnedPlayerName);
        newWarn.setStaffName(staffMemberName);
        newWarn.setMessage(message);
        newWarn.setRating(rating);
        newWarn.setCreated(new Date());

        try {
            dbConnection.save(newWarn);
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "error on save data", e);
            return false;
        }

        if (BukkitUtils.isPlayerOnline(warnedPlayerName)) {
            notAcceptedWarnedPlayersCache.add(warnedPlayerName.toLowerCase());
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
    public final boolean deleteWarn(Integer id) {
        Warn warn = dbConnection.find(Warn.class, id);
        if (warn == null) {
            return false;
        }

        try {
            dbConnection.delete(warn);
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "error on delete", e);
            return false;
        }

        notAcceptedWarnedPlayersCache.remove(warn.getPlayerName().toLowerCase());

        return true;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final boolean deleteWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        Set<Warn> warns = dbConnection.find(Warn.class).where().ieq("playername", playerName).findSet();
        if (warns.size() == 0) {
            return false;
        }

        try {
            int deletedRows = dbConnection.delete(warns);
            if (deletedRows == 0) {
                return false;
            }
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "error on delete data", e);
            return false;
        }

        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());

        return true;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since
     */
    public final boolean acceptWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        Set<Warn> unAccWarns = dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
        if (unAccWarns.size() == 0) {
            return false;
        }

        for (Warn warn : unAccWarns) {
            warn.setAccepted(new Date());
        }

        try {
            int savedRows = dbConnection.save(unAccWarns);
            if (savedRows == 0) {
                return false;
            }
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "error on save data", e);
            return false;
        }

        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());

        return true;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final boolean hasPlayerNotAcceptedWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        return dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final boolean isPlayersWarned(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final Collection<String> searchPlayerWithWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new ArrayList<String>();
        }

        preparedSqlSearchWarnedPlayer.setParameter("playerName", "%" + playerName + "%");
        List<SqlRow> found = preparedSqlSearchWarnedPlayer.findList();

        ArrayConvert<SqlRow> wc = new ArrayConvert<SqlRow>() {
            @Override
            protected String convertToString(SqlRow obj) {
                return obj.getString("playername");
            }
        };

        return wc.toCollection(found);
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final List<Warn> getWarnList(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new ArrayList<Warn>();
        }

        return dbConnection.find(Warn.class).where().like("playername", "%" + playerName + "%").findList();
    }

    /**
     * 
     * @param playerName
     * @since 1.1
     */
    public final void cacheNotAcceptedWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        if (BukkitUtils.isPlayerOnline(playerName)) {
            notAcceptedWarnedPlayersCache.add(playerName.toLowerCase());
        }
    }

    /**
     * 
     * @param playerName
     * 
     * @since 1.1
     */
    public final void removeCachedNotAcceptedWarns(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());
    }

    /**
     * 
     * @param playerName
     * @return
     * 
     * @since 1.1
     */
    public final boolean hasPlayerNotAcceptedWarnsCached(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        return notAcceptedWarnedPlayersCache.contains(playerName.toLowerCase());
    }

    /**
     * Checks if the player was sometime before joined the server. Detailed it
     * will checked the LogBlock-player table for that, on the same database
     * connection.
     * 
     * @param playerName
     * @return True, if the player had was online before, otherwise false. If
     *         the playerName is null or empty false will be returned.
     * 
     * @since 1.1
     * @see <a
     *      href="http://dev.bukkit.org/bukkit-plugins/logblock/">LogBlock</a>
     */
    public final boolean hasPlayedBefore(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        preparedSqlOfflinePlayer.setParameter("playerName", playerName);

        try {
            return (preparedSqlOfflinePlayer.findUnique() != null);
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "error on query LogBlock Table", e);
            return false;
        }
    }

    /**
     * Calculate expiration date for warning against settings.
     * 
     * @param warn
     * @return Return the date or null, if warn or accepted date is null
     * 
     * @since 1.2
     */
    public final Date calculateExpirationDate(Warn warn) {
        if (warn == null || warn.getAccepted() == null) {
            return null;
        }

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
     * @return False, if the expirationDays smaller than 0, otherwise true
     * 
     * @since 1.2
     */
    public final boolean setExpirationDays(int expirationDays) {
        if (expirationDays < 0) {
            return false;
        }

        this.expirationDays = expirationDays;

        return true;
    }
}
