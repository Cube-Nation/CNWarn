package de.cubenation.plugins.cnwarn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.cubenation.plugins.cnwarn.model.Warn;
import de.cubenation.plugins.cnwarn.model.Watch;
import de.cubenation.plugins.cnwarn.model.exception.WarnNotFoundException;
import de.cubenation.plugins.cnwarn.model.exception.WarnsNotFoundException;
import de.cubenation.plugins.utils.testapi.AbstractDatabaseTest;

public class WarnServiceTest extends AbstractDatabaseTest {
    private WarnService warnService;
    private String testOnlinePlayer = "testOnlinePlayer";
    private String testOfflinePlayer = "testOfflinePlayer";
    private String testStaff = "testStaff";

    @Before
    @Override
    public void setUp() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Warn.class);
        list.add(Watch.class);

        super.setUp(list);

        final Player player = mock(Player.class);
        when(player.getName()).thenReturn(testOnlinePlayer);

        doAnswer(new Answer<Player>() {
            public Player answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String name = (String) args[0];

                if (name.equalsIgnoreCase(testOnlinePlayer)) {
                    return player;
                }

                return null;
            }
        }).when(Bukkit.getServer()).getPlayer(anyString());
        doAnswer(new Answer<Player>() {
            public Player answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String name = (String) args[0];

                if (name.equalsIgnoreCase(testOnlinePlayer)) {
                    return player;
                }

                return null;
            }
        }).when(Bukkit.getServer()).getPlayerExact(anyString());

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
        try {
            warnService.addWarn("", testStaff, "message", 1);
        } catch (IllegalArgumentException e) {
            assertEquals("warned player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.addWarn(testOnlinePlayer, "", "message", 1);
        } catch (IllegalArgumentException e) {
            assertEquals("staff member name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.addWarn(null, testStaff, "message", 1);
        } catch (IllegalArgumentException e) {
            assertEquals("warned player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.addWarn(testOnlinePlayer, null, "message", 1);
        } catch (IllegalArgumentException e) {
            assertEquals("staff member name cannot be null or empty", e.getMessage());
        }
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
    public void testDeleteWarnWrongId() throws WarnNotFoundException {
        try {
            warnService.deleteWarn(5);
        } catch (WarnNotFoundException e) {
            assertEquals("warn not found for id 5", e.getMessage());
            assertEquals(5, e.getWarnId());
        }
    }

    @Test
    public void testDeleteWarnOnline() throws WarnNotFoundException {
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
    public void testDeleteWarnOffline() throws WarnNotFoundException {
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
    public void testDeleteWarnsNull() throws WarnsNotFoundException {
        try {
            warnService.deleteWarns(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.deleteWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testDeleteWarnsEmtpy() throws WarnsNotFoundException {
        try {
            warnService.deleteWarns(testOnlinePlayer);
        } catch (WarnsNotFoundException e) {
            assertEquals("warns not found for player testOnlinePlayer", e.getMessage());
        }
    }

    @Test
    public void testDeleteWarnsOnline() throws WarnsNotFoundException {
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
    public void testDeleteWarnsOffline() throws WarnsNotFoundException {
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
    public void testAcceptWarnsNull() throws WarnsNotFoundException {
        try {
            warnService.acceptWarns(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.acceptWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testAcceptWarnsEmpty() throws WarnsNotFoundException {
        try {
            warnService.acceptWarns(testOnlinePlayer);
        } catch (WarnsNotFoundException e) {
            assertEquals("warns not found for player testOnlinePlayer", e.getMessage());
        }
    }

    @Test
    public void testAcceptWarnsOnline() throws WarnsNotFoundException {
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
    public void testAcceptWarnsOffline() throws WarnsNotFoundException {
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
        try {
            warnService.hasPlayerNotAcceptedWarns(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.hasPlayerNotAcceptedWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testHasPlayerNotAcceptedWarns() {
        assertFalse(warnService.hasPlayerNotAcceptedWarns(testOnlinePlayer));

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertTrue(warnService.hasPlayerNotAcceptedWarns(testOnlinePlayer));
    }

    @Test
    public void testIsPlayersWarnedNull() {
        try {
            warnService.isPlayersWarned(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.isPlayersWarned("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testIsPlayersWarned() {
        assertFalse(warnService.isPlayersWarned(testOnlinePlayer));

        assertTrue(warnService.addWarn(testOnlinePlayer, testStaff, "message", 1));

        assertTrue(warnService.isPlayersWarned(testOnlinePlayer));
    }

    @Test
    public void testSearchPlayerWithWarnsNull() {
        try {
            warnService.searchPlayerWithWarns(null);
        } catch (IllegalArgumentException e) {
            assertEquals("search pattern cannot be null or empty", e.getMessage());
        }
        try {
            warnService.searchPlayerWithWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("search pattern cannot be null or empty", e.getMessage());
        }
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
        try {
            warnService.getWarnList(null);
        } catch (IllegalArgumentException e) {
            assertEquals("search pattern cannot be null or empty", e.getMessage());
        }

        try {
            warnService.getWarnList("");
        } catch (IllegalArgumentException e) {
            assertEquals("search pattern cannot be null or empty", e.getMessage());
        }
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
        try {
            warnService.cacheNotAcceptedWarns(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.cacheNotAcceptedWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
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
        try {
            warnService.removeCachedNotAcceptedWarns(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.removeCachedNotAcceptedWarns("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
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
        try {
            warnService.hasPlayerNotAcceptedWarnsCached("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.hasPlayerNotAcceptedWarnsCached(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
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
        try {
            warnService.hasPlayedBefore("");
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            warnService.hasPlayedBefore(null);
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testHasPlayedBeforeFallback() {
        assertTrue(warnService.hasPlayedBefore(testOnlinePlayer));
        assertFalse(warnService.hasPlayedBefore(testOfflinePlayer));
    }

    @Test
    public void testCalculateExpirationDateNull() {
        try {
            warnService.calculateExpirationDate(null);
        } catch (IllegalArgumentException e) {
            assertEquals("warn cannot be null", e.getMessage());
        }

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
