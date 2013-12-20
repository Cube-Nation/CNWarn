package de.derflash.plugins.cnwarn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.Test;

import de.cubenation.plugins.utils.testapi.AbstractDatabaseTest;
import de.cubenation.plugins.utils.testapi.TestPlayer;
import de.cubenation.plugins.utils.testapi.TestServer;
import de.derflash.plugins.cnwarn.CnWarn;
import de.derflash.plugins.cnwarn.model.Warn;

public class WarnServiceTest extends AbstractDatabaseTest {
    private WarnService warnService;
    private String testOnlinePlayer = "testOnlinePlayer";
    private String testOfflinePlayer = "testOfflinePlayer";
    private String testStaff = "testStaff";

    @Before
    @Override
    public void setUp() {
        CnWarn plugin = new CnWarn();
        super.setUp(plugin);

        ((TestServer) Bukkit.getServer()).addOnlinePlayer(new TestPlayer(testOnlinePlayer));

        warnService = new WarnService(dbConnection, Logger.getLogger("WarnServiceTest"));
        assertNotNull(warnService);
    }

    @Test
    public void testClearExpired() {
        Calendar cal = new GregorianCalendar();

        Date creationDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date acceptedDateEquals = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date acceptedDateBefore = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date acceptedDateAfter = cal.getTime();

        Warn warnEquals = new Warn();
        warnEquals.setPlayerName(testOfflinePlayer);
        warnEquals.setStaffName(testStaff);
        warnEquals.setCreated(creationDate);
        warnEquals.setAccepted(acceptedDateEquals);
        warnEquals.setRating(1);

        Warn warnBefore = new Warn();
        warnBefore.setPlayerName(testOfflinePlayer);
        warnBefore.setStaffName(testStaff);
        warnBefore.setCreated(creationDate);
        warnBefore.setAccepted(acceptedDateBefore);
        warnBefore.setRating(1);

        Warn warnAfter = new Warn();
        warnAfter.setPlayerName(testOfflinePlayer);
        warnAfter.setStaffName(testStaff);
        warnAfter.setCreated(creationDate);
        warnAfter.setAccepted(acceptedDateAfter);
        warnAfter.setRating(1);

        assertEquals(0, dbConnection.find(Warn.class).where().gt("rating", 0).findList().size());

        // nothing to update
        warnService.clearExpired();

        assertEquals(0, dbConnection.find(Warn.class).where().gt("rating", 0).findList().size());

        dbConnection.save(warnEquals);
        dbConnection.save(warnBefore);
        dbConnection.save(warnAfter);

        assertEquals(3, dbConnection.find(Warn.class).where().gt("rating", 0).findList().size());

        // update older
        warnService.clearExpired();

        assertEquals(2, dbConnection.find(Warn.class).where().gt("rating", 0).findList().size());

        // nothing new to update
        warnService.clearExpired();

        assertEquals(2, dbConnection.find(Warn.class).where().gt("rating", 0).findList().size());
    }

    @Test
    public void testGetWarnCount() {
        assertEquals(0, warnService.getWarnCount(testOnlinePlayer));
        assertEquals(0, warnService.getWarnCount(testOfflinePlayer));

        Warn warn = new Warn();
        warn.setPlayerName(testOfflinePlayer);
        warn.setStaffName(testStaff);
        warn.setCreated(new Date());
        warn.setRating(1);
        dbConnection.save(warn);

        assertEquals(0, warnService.getWarnCount(testOnlinePlayer));
        assertEquals(1, warnService.getWarnCount(testOfflinePlayer));

        Warn warnSecond = new Warn();
        warnSecond.setPlayerName(testOfflinePlayer);
        warnSecond.setStaffName(testStaff);
        warnSecond.setCreated(new Date());
        warnSecond.setRating(1);
        dbConnection.save(warnSecond);

        assertEquals(0, warnService.getWarnCount(testOnlinePlayer));
        assertEquals(2, warnService.getWarnCount(testOfflinePlayer));
    }

    @Test
    public void testGetRatingSum() {
        assertEquals(0, warnService.getRatingSum(testOnlinePlayer));
        assertEquals(0, warnService.getRatingSum(testOfflinePlayer));

        Warn warn = new Warn();
        warn.setPlayerName(testOfflinePlayer);
        warn.setStaffName(testStaff);
        warn.setCreated(new Date());
        warn.setRating(1);
        dbConnection.save(warn);

        assertEquals(0, warnService.getRatingSum(testOnlinePlayer));
        assertEquals(1, warnService.getRatingSum(testOfflinePlayer));

        Warn warnSecond = new Warn();
        warnSecond.setPlayerName(testOfflinePlayer);
        warnSecond.setStaffName(testStaff);
        warnSecond.setCreated(new Date());
        warnSecond.setRating(2);
        dbConnection.save(warnSecond);

        assertEquals(0, warnService.getRatingSum(testOnlinePlayer));
        assertEquals(3, warnService.getRatingSum(testOfflinePlayer));
    }

    @Test
    public void testAddWarnNull() {
        assertFalse(warnService.addWarn("", testStaff, "message", 1));
        assertFalse(warnService.addWarn(testOnlinePlayer, "", "message", 1));
        assertFalse(warnService.addWarn(null, testStaff, "message", 1));
        assertFalse(warnService.addWarn(testOnlinePlayer, null, "message", 1));
    }

    @Test
    public void testAddWarnOnline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));
        assertEquals(1, dbConnection.find(Warn.class).findList().size());

        Warn findUnique = dbConnection.find(Warn.class).findUnique();
        assertNotNull(findUnique);

        assertNull(findUnique.getAccepted());
        assertNotNull(findUnique.getCreated());
        assertNotNull(findUnique.getId());
        assertNotNull(findUnique.getMessage());
        assertEquals("message", findUnique.getMessage());
        assertNotNull(findUnique.getPlayerName());
        assertEquals(testOnlinePlayer, findUnique.getPlayerName());
        assertEquals(1, findUnique.getRating());
        assertNotNull(findUnique.getStaffName());
        assertEquals(testStaff, findUnique.getStaffName());

        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testAddWarnOffline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOfflinePlayer, testStaff, "message", 1));
        assertEquals(1, dbConnection.find(Warn.class).findList().size());

        Warn findUnique = dbConnection.find(Warn.class).findUnique();
        assertNotNull(findUnique);

        assertNull(findUnique.getAccepted());
        assertNotNull(findUnique.getCreated());
        assertNotNull(findUnique.getId());
        assertNotNull(findUnique.getMessage());
        assertEquals("message", findUnique.getMessage());
        assertNotNull(findUnique.getPlayerName());
        assertEquals(testOfflinePlayer, findUnique.getPlayerName());
        assertEquals(1, findUnique.getRating());
        assertNotNull(findUnique.getStaffName());
        assertEquals(testStaff, findUnique.getStaffName());

        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testDeleteWarnNull() {
        assertFalse(warnService.deleteWarn(null));
    }

    @Test
    public void testDeleteWarnWrongId() {
        assertFalse(warnService.deleteWarn(5));
    }

    @Test
    public void testDeleteWarnOnline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        Warn findUnique = dbConnection.find(Warn.class).findUnique();

        assertTrue(warnService.deleteWarn(findUnique.getId()));

        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testDeleteWarnOffline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOfflinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        Warn findUnique = dbConnection.find(Warn.class).findUnique();

        assertTrue(warnService.deleteWarn(findUnique.getId()));

        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testDeleteWarnsNull() {
        assertFalse(warnService.deleteWarns(null));
        assertFalse(warnService.deleteWarns(""));
    }

    @Test
    public void testDeleteWarnsEmtpy() {
        assertFalse(warnService.deleteWarns(testOnlinePlayer));
    }

    @Test
    public void testDeleteWarnsOnline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.deleteWarns(testOnlinePlayer));

        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testDeleteWarnsOffline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOfflinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.deleteWarns(testOfflinePlayer));

        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testAcceptWarnsNull() {
        assertFalse(warnService.acceptWarns(null));
        assertFalse(warnService.acceptWarns(""));
    }

    @Test
    public void testAcceptWarnsEmpty() {
        assertFalse(warnService.acceptWarns(testOnlinePlayer));
    }

    @Test
    public void testAcceptWarnsOnline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.acceptWarns(testOnlinePlayer));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOnlinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testAcceptWarnsOffline() {
        assertEquals(0, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.addWarn(testOfflinePlayer, testStaff, "message", 1));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(1, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());

        assertTrue(warnService.acceptWarns(testOfflinePlayer));

        assertEquals(1, dbConnection.find(Warn.class).findList().size());
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOfflinePlayer));
        assertEquals(0, dbConnection.find(Warn.class).where().eq("playername", testOfflinePlayer).isNull("accepted").findRowCount());
    }

    @Test
    public void testHasPlayerNotAcceptedWarnsNull() {
        assertFalse(warnService.hasPlayerNotAcceptedWarns(null));
        assertFalse(warnService.hasPlayerNotAcceptedWarns(""));
    }

    @Test
    public void testHasPlayerNotAcceptedWarns() {
        assertFalse(warnService.hasPlayerNotAcceptedWarns(testOnlinePlayer));

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertTrue(warnService.hasPlayerNotAcceptedWarns(testOnlinePlayer));
    }

    @Test
    public void testIsPlayersWarnedNull() {
        assertFalse(warnService.isPlayersWarned(null));
        assertFalse(warnService.isPlayersWarned(""));
    }

    @Test
    public void testIsPlayersWarned() {
        assertFalse(warnService.isPlayersWarned(testOnlinePlayer));

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertTrue(warnService.isPlayersWarned(testOnlinePlayer));
    }

    @Test
    public void testSearchPlayerWithWarnsNull() {
        Collection<String> searchWarnsNull = warnService.searchPlayerWithWarns(null);
        assertNotNull(searchWarnsNull);
        assertEquals(0, searchWarnsNull.size());
        Collection<String> searchWarnsEmpty = warnService.searchPlayerWithWarns("");
        assertNotNull(searchWarnsEmpty);
        assertEquals(0, searchWarnsEmpty.size());
    }

    @Test
    public void testSearchPlayerWithWarns() {
        Collection<String> searchWarnsExact = warnService.searchPlayerWithWarns(testOnlinePlayer);
        assertNotNull(searchWarnsExact);
        assertEquals(0, searchWarnsExact.size());
        Collection<String> searchWarnsLike = warnService.searchPlayerWithWarns(testOnlinePlayer.substring(1, testOnlinePlayer.length() - 2));
        assertNotNull(searchWarnsLike);
        assertEquals(0, searchWarnsLike.size());

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        searchWarnsExact = warnService.searchPlayerWithWarns(testOnlinePlayer);
        assertNotNull(searchWarnsExact);
        assertEquals(1, searchWarnsExact.size());
        assertTrue(searchWarnsExact.contains(testOnlinePlayer));
        searchWarnsLike = warnService.searchPlayerWithWarns(testOnlinePlayer.substring(1, testOnlinePlayer.length() - 2));
        assertNotNull(searchWarnsExact);
        assertEquals(1, searchWarnsLike.size());
        assertTrue(searchWarnsLike.contains(testOnlinePlayer));
    }

    @Test
    public void testGetWarnListNull() {
        List<Warn> listWarnsNull = warnService.getWarnList(null);
        assertNotNull(listWarnsNull);
        assertEquals(0, listWarnsNull.size());
        List<Warn> listWarnsEmpty = warnService.getWarnList("");
        assertNotNull(listWarnsEmpty);
        assertEquals(0, listWarnsEmpty.size());
    }

    @Test
    public void testGetWarnList() {
        List<Warn> listWarns = warnService.getWarnList(testOnlinePlayer);
        assertNotNull(listWarns);
        assertEquals(0, listWarns.size());

        Warn warnFirst = new Warn();
        warnFirst.setPlayerName(testOnlinePlayer);
        warnFirst.setStaffName(testStaff);
        warnFirst.setCreated(new Date());
        warnFirst.setRating(2);
        warnFirst.setMessage("message");
        dbConnection.save(warnFirst);

        listWarns = warnService.getWarnList(testOnlinePlayer);
        assertNotNull(listWarns);
        assertEquals(1, listWarns.size());

        Warn warn = listWarns.get(0);

        assertNull(warn.getAccepted());
        assertNotNull(warn.getCreated());
        assertNotNull(warn.getId());
        assertNotNull(warn.getMessage());
        assertEquals("message", warn.getMessage());
        assertNotNull(warn.getPlayerName());
        assertEquals(testOnlinePlayer, warn.getPlayerName());
        assertEquals(2, warn.getRating());
        assertNotNull(warn.getStaffName());
        assertEquals(testStaff, warn.getStaffName());

        Warn warnSecond = new Warn();
        warnSecond.setPlayerName(testOnlinePlayer);
        warnSecond.setStaffName(testStaff);
        warnSecond.setCreated(new Date());
        warnSecond.setRating(2);
        warnSecond.setMessage("message");
        dbConnection.save(warnSecond);

        listWarns = warnService.getWarnList(testOnlinePlayer);
        assertNotNull(listWarns);
        assertEquals(2, listWarns.size());
    }

    @Test
    public void testCacheNotAcceptedWarnsNull() {
        assertFalse(warnService.cacheNotAcceptedWarns(null));
        assertFalse(warnService.cacheNotAcceptedWarns(""));
    }

    @Test
    public void testCacheNotAcceptedWarnsOnline() {
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));

        assertTrue(warnService.cacheNotAcceptedWarns(testOnlinePlayer));

        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
    }

    @Test
    public void testCacheNotAcceptedWarnsOffline() {
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));

        assertFalse(warnService.cacheNotAcceptedWarns(testOfflinePlayer));

        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
    }

    @Test
    public void testRemoveCachedNotAcceptedWarnsNull() {
        assertFalse(warnService.removeCachedNotAcceptedWarns(null));
        assertFalse(warnService.removeCachedNotAcceptedWarns(""));
    }

    @Test
    public void testRemoveCachedNotAcceptedWarns() {
        assertTrue(warnService.cacheNotAcceptedWarns(testOnlinePlayer));
        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));

        assertTrue(warnService.removeCachedNotAcceptedWarns(testOnlinePlayer));

        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
    }

    @Test
    public void testHasPlayerNotAcceptedWarnsCachedNull() {
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(""));
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(null));
    }

    @Test
    public void testHasPlayerNotAcceptedWarnsCached() {
        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));

        assertTrue(warnService.cacheNotAcceptedWarns(testOnlinePlayer));

        assertTrue(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));

        assertTrue(warnService.removeCachedNotAcceptedWarns(testOnlinePlayer));

        assertFalse(warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer));
    }

    @Test
    public void testHasPlayedBeforeNull() {
        assertFalse(warnService.hasPlayedBefore(""));
        assertFalse(warnService.hasPlayedBefore(null));
    }

    @Test
    public void testHasPlayedBeforeFallback() {
        assertTrue(warnService.hasPlayedBefore(testOnlinePlayer));
        assertFalse(warnService.hasPlayedBefore(testOfflinePlayer));
    }

    @Test
    public void testCalculateExpirationDateNull() {
        assertNull(warnService.calculateExpirationDate(null));

        Warn warn = new Warn();
        assertNull(warnService.calculateExpirationDate(warn));
    }

    @Test
    public void testCalculateExpirationDate() {
        Calendar cal = new GregorianCalendar();

        Date acceptedDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date expiredDate = cal.getTime();

        Warn warn = new Warn();
        warn.setAccepted(acceptedDate);

        Date calculateExpirationDate = warnService.calculateExpirationDate(warn);
        assertNotNull(calculateExpirationDate);
        assertEquals(expiredDate, calculateExpirationDate);
    }

    @Test
    public void testGetExpirationDays() {
        assertEquals(30, warnService.getExpirationDays());

        assertTrue(warnService.setExpirationDays(1));

        assertEquals(1, warnService.getExpirationDays());
    }

    @Test
    public void testSetExpirationDays() {
        assertEquals(30, warnService.getExpirationDays());

        assertTrue(warnService.setExpirationDays(1));
        assertEquals(1, warnService.getExpirationDays());

        assertFalse(warnService.setExpirationDays(0));
        assertEquals(1, warnService.getExpirationDays());

        assertFalse(warnService.setExpirationDays(-2));
        assertEquals(1, warnService.getExpirationDays());
    }
}
