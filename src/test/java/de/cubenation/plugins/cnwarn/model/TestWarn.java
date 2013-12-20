package de.cubenation.plugins.cnwarn.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import de.cubenation.plugins.cnwarn.CnWarn;
import de.cubenation.plugins.cnwarn.model.Warn;
import de.cubenation.plugins.utils.testapi.AbstractDatabaseTest;

public class TestWarn extends AbstractDatabaseTest {
    @Before
    public void setUp() {
        super.setUp(new CnWarn());
    }

    @Test
    public void testExpired() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.YEAR, 2013);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date createdDate = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, 2);
        Date acceptedDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date beforeDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date afterDate = cal.getTime();

        Warn warn = new Warn();
        warn.setCreated(createdDate);
        warn.setAccepted(acceptedDate);
        warn.setMessage("test");
        warn.setPlayerName("testplayer");
        warn.setRating(1);
        warn.setStaffName("teststaff");

        dbConnection.save(warn);

        Warn findWarn = dbConnection.find(Warn.class).where().gt("accepted", beforeDate).findUnique();
        assertNotNull(findWarn);

        findWarn = dbConnection.find(Warn.class).where().lt("accepted", beforeDate).findUnique();
        assertNull(findWarn);

        findWarn = dbConnection.find(Warn.class).where().lt("accepted", afterDate).findUnique();
        assertNotNull(findWarn);

        findWarn = dbConnection.find(Warn.class).where().gt("accepted", afterDate).findUnique();
        assertNull(findWarn);
    }
}
