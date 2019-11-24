package cz.libors.iqrest;

import org.junit.Test;

import java.time.LocalDate;

import static cz.libors.iqrest.DayNameUtil.checkMenuSaveRelevant;
import static org.junit.Assert.*;

public class DayNameUtilTest {

    @Test
    public void relevantDownloadDate() {
        LocalDate now = LocalDate.of(2019, 2, 6); //wednesday

        assertFalse(checkMenuSaveRelevant("9.2.2019", now)); // weekend
        assertTrue(checkMenuSaveRelevant("7.2.2019", now)); // weekday

        assertFalse(checkMenuSaveRelevant("1.2.2019", now)); // last week
        assertFalse(checkMenuSaveRelevant("13.2.2019", now)); // next week

        now = LocalDate.of(2019, 2, 9); // saturday

        assertTrue(checkMenuSaveRelevant("13.2.2019", now)); // next week
        assertFalse(checkMenuSaveRelevant("20.2.2019", now)); // 2 weeks ahead
    }

}