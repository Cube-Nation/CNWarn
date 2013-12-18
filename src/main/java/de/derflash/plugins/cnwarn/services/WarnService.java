package de.derflash.plugins.cnwarn.services;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;

import de.cubenation.plugins.utils.ArrayConvert;
import de.cubenation.plugins.utils.BukkitUtils;
import de.derflash.plugins.cnwarn.model.Warn;

public class WarnService {
    private int expirationDays = 30;
    private EbeanServer dbConnection;

    // Hashset all (!online!) players with not accepted warnings
    private HashSet<String> notAcceptedWarnedPlayersCache = new HashSet<String>();

    private SqlQuery preparedSqlSumRating;
    private SqlQuery preparedSqlOfflinePlayer;

    public WarnService(EbeanServer dbConnection) {
        this.dbConnection = dbConnection;

        preparedSqlSumRating = dbConnection
                .createSqlQuery("select sum(rating) as sumrating from cn_warns where lower(playername) = lower(:playerName) limit 1");
        preparedSqlOfflinePlayer = dbConnection.createSqlQuery("select * from `lb-players` where lower(playername) = lower(:playerName)");
    }

    public void clearOld() {
        dbConnection.createSqlUpdate("update `cn_warns` set rating = 0 where to_days(now()) - to_days(`accepted`) > " + Integer.toString(expirationDays))
                .execute();
    }

    public Integer getWarnCount(String playerName) {
        return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount();
    }

    public Integer getRatingSum(String playerName) {
        preparedSqlSumRating.setParameter("playerName", playerName);

        return preparedSqlSumRating.findUnique().getInteger("sumrating");
    }

    public void warnPlayer(String warnedPlayer, String staffMemberName, String message, Integer rating) {
        Warn newWarn = new Warn();
        newWarn.setPlayername(warnedPlayer);
        newWarn.setStaffname(staffMemberName);
        newWarn.setMessage(message);
        newWarn.setRating(rating);
        newWarn.setCreated(new Date());
        dbConnection.save(newWarn);

        if (BukkitUtils.isPlayerOnline(warnedPlayer)) {
            notAcceptedWarnedPlayersCache.add(warnedPlayer.toLowerCase());
        }
    }

    public boolean deleteWarning(Integer id) {
        String playerName = getPlayerNameFromId(id);
        if (playerName != null) {
            dbConnection.delete(Warn.class, id);

            notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());

            return true;
        }

        return false;
    }

    private String getPlayerNameFromId(Integer id) {
        Warn warn = dbConnection.find(Warn.class, id);
        if (warn != null) {
            return warn.getPlayername();
        }
        return null;
    }

    public void deleteWarnings(String playerName) {
        Set<Warn> warns = dbConnection.find(Warn.class).where().ieq("playername", playerName).findSet();
        dbConnection.delete(warns);

        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());
    }

    public void acceptWarnings(String playerName) {
        Set<Warn> unAccWarns = dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
        for (Warn warn : unAccWarns) {
            warn.setAccepted(new Date());
        }
        dbConnection.save(unAccWarns);

        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());
    }

    public boolean hasPlayerNotAcceptedWarns(String playerName) {
        return dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
    }

    public boolean isPlayersWarned(String playerName) {
        return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
    }

    public Collection<String> searchPlayerWithWarns(String playerName) {
        List<Warn> found = dbConnection.find(Warn.class).where().like("playername", "%" + playerName + "%").setMaxRows(8).setDistinct(true).findList();

        ArrayConvert<Warn> wc = new ArrayConvert<Warn>() {
            @Override
            protected String convertToString(Warn obj) {
                return obj.getPlayername();
            }
        };

        return wc.toCollection(found);
    }

    public List<Warn> getWarnList(String playerName) {
        return dbConnection.find(Warn.class).where().like("playername", "%" + playerName + "%").findList();
    }

    public void cacheNotAcceptedWarns(String playerName) {
        if (BukkitUtils.isPlayerOnline(playerName)) {
            notAcceptedWarnedPlayersCache.add(playerName.toLowerCase());
        }
    }

    public void removeCachedNotAcceptedWarns(String playerName) {
        notAcceptedWarnedPlayersCache.remove(playerName.toLowerCase());
    }

    public boolean hasPlayerNotAcceptedWarnsCached(String playerName) {
        return notAcceptedWarnedPlayersCache.contains(playerName.toLowerCase());
    }

    public boolean hasPlayedBefore(String playerName) {
        preparedSqlOfflinePlayer.setParameter("playerName", playerName);

        return (preparedSqlOfflinePlayer.findUnique() != null);
    }

    /**
     * Calculate expiration date for warning against settings.
     * 
     * @param warn
     * @return Return the date or null, if warn or accepted date is null
     * 
     * @since 1.2
     */
    public Date getExpirationDate(Warn warn) {
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
    public int getExpirationDays() {
        return expirationDays;
    }

    /**
     * Set the settings expiration day.
     * 
     * @param expirationDays
     *            Must be greater than 0 otherwise, statment will ignored.
     * 
     * @since 1.2
     */
    public void setExpirationDays(int expirationDays) {
        if (expirationDays < 0) {
            return;
        }

        this.expirationDays = expirationDays;
    }
}
