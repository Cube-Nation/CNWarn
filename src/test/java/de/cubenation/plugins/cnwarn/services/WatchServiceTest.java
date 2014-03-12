package de.cubenation.plugins.cnwarn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import de.cubenation.plugins.cnwarn.model.Warn;
import de.cubenation.plugins.cnwarn.model.Watch;
import de.cubenation.plugins.utils.testapi.AbstractDatabaseTest;

public class WatchServiceTest extends AbstractDatabaseTest {
    private WatchService watchService;
    private String testPlayer = "testPlayer";
    private String testStaff = "testStaff";

    @Before
    @Override
    public void setUp() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Warn.class);
        list.add(Watch.class);

        super.setUp(list);

        watchService = new WatchService(dbConnection, Logger.getLogger("WatchServiceTest"));
        assertNotNull(watchService);
    }

    @Test
    public void testIsPlayerInWatchListNull() {
        try {
            watchService.isPlayerInWatchList("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }

        try {
            watchService.isPlayerInWatchList(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testIsPlayerInWatchList() {
        assertFalse(watchService.isPlayerInWatchList(testPlayer));

        Watch watch = new Watch();
        watch.setPlayerName(testPlayer);
        watch.setCreated(new Date());
        watch.setStaffName(testStaff);
        dbConnection.save(watch);

        assertTrue(watchService.isPlayerInWatchList(testPlayer));
    }

    @Test
    public void testAddWatchNull() {
        try {
            watchService.addWatch("", "test", testStaff);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            watchService.addWatch(null, "test", testStaff);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            watchService.addWatch(testPlayer, "test", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("staff name cannot be null or empty", e.getMessage());
        }
        try {
            watchService.addWatch(testPlayer, "test", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("staff name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testAddWatch() {
        assertEquals(0, dbConnection.find(Watch.class).findRowCount());

        assertTrue(watchService.addWatch(testPlayer, "test", testStaff));

        assertEquals(1, dbConnection.find(Watch.class).findRowCount());

        Watch watchedPlayer = dbConnection.find(Watch.class).where().eq("playerName", testPlayer).findUnique();
        assertNotNull(watchedPlayer);
        assertNotNull(watchedPlayer.getCreated());
        assertNotNull(watchedPlayer.getId());
        assertEquals("test", watchedPlayer.getMessage());
        assertEquals(testPlayer, watchedPlayer.getPlayerName());
        assertEquals(testStaff, watchedPlayer.getStaffName());
    }

    @Test
    public void testGetWatchedPlayerByIdWrongId() {
        assertNull(watchService.getWatchedPlayerById(5));
    }

    @Test
    public void testGetWatchedPlayerById() {
        Watch watch = new Watch();
        watch.setPlayerName(testPlayer);
        watch.setCreated(new Date());
        watch.setStaffName(testStaff);
        dbConnection.save(watch);

        Watch watchedPlayerById = watchService.getWatchedPlayerById(watch.getId());
        assertNotNull(watchedPlayerById);
        assertEquals(watch.getId(), watchedPlayerById.getId());
    }

    @Test
    public void testGetWatchedPlayerByNameNull() {
        try {
            watchService.getWatchedPlayerByName("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
        try {
            watchService.getWatchedPlayerByName(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("player name cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testGetWatchedPlayerByName() {
        assertNull(watchService.getWatchedPlayerByName(testPlayer));

        Watch watch = new Watch();
        watch.setPlayerName(testPlayer);
        watch.setCreated(new Date());
        watch.setStaffName(testStaff);
        dbConnection.save(watch);

        Watch watchedPlayerByName = watchService.getWatchedPlayerByName(testPlayer);
        assertNotNull(watchedPlayerByName);
        assertEquals(watch.getId(), watchedPlayerByName.getId());
    }

    @Test
    public void testDeletePlayerWatchNull() {
        try {
            watchService.deletePlayerWatch(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("watch cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testDeletePlayerWatch() {
        Watch watch = new Watch();
        watch.setPlayerName(testPlayer);
        watch.setCreated(new Date());
        watch.setStaffName(testStaff);
        dbConnection.save(watch);

        assertEquals(1, dbConnection.find(Watch.class).findRowCount());

        watchService.deletePlayerWatch(watch);

        assertEquals(0, dbConnection.find(Watch.class).findRowCount());
    }

    @Test
    public void testGetAllWatches() {
        List<Watch> empty = watchService.getAllWatches();
        assertNotNull(empty);
        assertEquals(0, empty.size());

        Watch watch = new Watch();
        watch.setPlayerName(testPlayer);
        watch.setCreated(new Date());
        watch.setStaffName(testStaff);
        dbConnection.save(watch);

        List<Watch> one = watchService.getAllWatches();
        assertNotNull(one);
        assertEquals(1, one.size());

        assertEquals(watch.getId(), one.get(0).getId());

    }
}
