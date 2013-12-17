package de.derflash.plugins.cnwarn.services;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;

import de.cubenation.plugins.utils.ArrayConvert;
import de.derflash.plugins.cnwarn.model.Warn;

public class WarnService {
    private int expirationDays = 30;
    private EbeanServer dbConnection;

    // Hashset all (!online!) players with not accepted warnings
    private HashSet<Player> notAccepted = new HashSet<Player>();

    private SqlQuery preparedSqlSumRating;
    private SqlQuery preparedSqlOfflinePlayer;

    public WarnService(EbeanServer dbConnection) {
        this.dbConnection = dbConnection;

        preparedSqlSumRating = dbConnection.createSqlQuery("select sum(rating) as sumrating from cn_warns where lower(playername) = lower(:playerName) limit 1");
        preparedSqlOfflinePlayer = dbConnection.createSqlQuery("select * from `lb-players` where lower(playername) = lower(:playerName)");
    }

    public void clearOld() {
        dbConnection.createSqlUpdate("update `cn_warns` set rating = 0 where to_days(now()) - to_days(`accepted`) > " + expirationDays).execute();
    }

    public Integer getWarnCount(String playerName) {
        return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount();
    }

    public Integer getRatingSum(String playerName) {
        preparedSqlSumRating.setParameter("playerName", playerName);

        return preparedSqlSumRating.findUnique().getInteger("sumrating");
    }

    public void warnPlayer(String warnedPlayer, Player staffMember, String message, Integer rating) {
        Warn newWarn = new Warn();
        newWarn.setPlayername(warnedPlayer);
        newWarn.setStaffname(staffMember.getName());
        newWarn.setMessage(message);
        newWarn.setRating(rating);
        newWarn.setCreated(new Date());
        dbConnection.save(newWarn);

        // if player is online
        Player player = Bukkit.getServer().getPlayer(warnedPlayer);
        if (player != null) {
            notAccepted.add(player);
        }
    }

    public boolean deleteWarning(Integer id, Player staffplayer) {
        String playerName = getPlayerNameFromId(id);
        if (playerName != null) {
            dbConnection.delete(Warn.class, id);

            Player onlinePlayer = Bukkit.getServer().getPlayer(playerName);
            if (onlinePlayer != null) {
                notAccepted.remove(onlinePlayer);
            }

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

    public void deleteWarnings(String playerName, Player staffplayer) {
        Set<Warn> warns = dbConnection.find(Warn.class).where().ieq("playername", playerName).findSet();
        dbConnection.delete(warns);

        Player onlinePlayer = Bukkit.getServer().getPlayer(playerName);
        if (onlinePlayer != null) {
            notAccepted.remove(onlinePlayer);
        }
    }

    public void acceptWarnings(Player player) {
        if (player == null) {
            return;
        }

        Set<Warn> unAccWarns = dbConnection.find(Warn.class).where().ieq("playername", player.getName()).isNull("accepted").findSet();
        for (Warn warn : unAccWarns) {
            warn.setAccepted(new Date());
        }
        dbConnection.save(unAccWarns);

        notAccepted.remove(player);
    }

    public boolean hasUnacceptedWarnings(Player player) {
        if (player == null) {
            return false;
        }

        return dbConnection.find(Warn.class).where().ieq("playername", player.getName()).isNull("accepted").findRowCount() > 0;
    }

    public boolean hasPlayersWarings(String playerName) {
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

    public void addNotAccepted(Player player) {
        if (player != null) {
            notAccepted.add(player);
        }
    }

    public void removeNotAccepted(Player player) {
        if (player != null) {
            notAccepted.remove(player);
        }
    }

    public boolean containsNotAccepted(Player player) {
        if (player == null) {
            return false;
        }

        return notAccepted.contains(player);
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
