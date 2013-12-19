package de.derflash.plugins.cnwarn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    public void testDeleteWarns() {
        warnService.deleteWarns(testOnlinePlayer);
    }

    @Test
    public void testAcceptWarns() {
        warnService.acceptWarns(testOnlinePlayer);
    }

    @Test
    public void testHasPlayerNotAcceptedWarns() {
        warnService.hasPlayerNotAcceptedWarns(testOnlinePlayer);
    }

    @Test
    public void testIsPlayersWarned() {
        warnService.isPlayersWarned(testOnlinePlayer);
    }

    @Test
    public void testSearchPlayerWithWarns() {
        warnService.searchPlayerWithWarns(testOnlinePlayer);
    }

    @Test
    public void testGetWarnList() {
        warnService.getWarnList(testOnlinePlayer);
    }

    @Test
    public void testCacheNotAcceptedWarns() {
        warnService.cacheNotAcceptedWarns(testOnlinePlayer);
    }

    @Test
    public void testRemoveCachedNotAcceptedWarns() {
        warnService.removeCachedNotAcceptedWarns(testOnlinePlayer);
    }

    @Test
    public void testHasPlayerNotAcceptedWarnsCached() {
        warnService.hasPlayerNotAcceptedWarnsCached(testOnlinePlayer);
    }

    @Test
    public void testHasPlayedBefore() {
        warnService.hasPlayedBefore(testOnlinePlayer);
    }

    @Test
    public void testCalculateExpirationDate() {
        warnService.calculateExpirationDate(null);
    }

    @Test
    public void testGetExpirationDays() {
        warnService.getExpirationDays();
    }

    @Test
    public void testSetExpirationDays() {
        warnService.setExpirationDays(30);
    }
}
