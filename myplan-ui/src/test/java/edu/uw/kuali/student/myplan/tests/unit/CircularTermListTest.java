package edu.uw.kuali.student.myplan.tests.unit;

import edu.uw.kuali.student.myplan.util.CircularTermList;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CircularTermListTest {

    @Test
    public void circularTermListExerciseTerms() {
        CircularTermList ctl = new CircularTermList("autumn", 2000);
        assertEquals(ctl.getQuarterName(), "Autumn");
        assertEquals(ctl.getYear(), 2000);

        ctl.incrementQuarter();
        assertEquals(ctl.getQuarterName(), "Winter");
        assertEquals(ctl.getYear(), 2001);

        ctl.incrementQuarter();
        assertEquals(ctl.getQuarterName(), "Spring");
        assertEquals(ctl.getYear(), 2001);

        ctl.incrementQuarter();
        assertEquals(ctl.getQuarterName(), "Summer");
        assertEquals(ctl.getYear(), 2001);

        ctl.incrementQuarter();
        assertEquals(ctl.getQuarterName(), "Autumn");
        assertEquals(ctl.getYear(), 2001);

        ctl.incrementQuarter();
        assertEquals(ctl.getQuarterName(), "Winter");
        assertEquals(ctl.getYear(), 2002);
    }

    @Test (expected = IllegalArgumentException.class)
    public void circularTermListUnknownQuarter() {
        new CircularTermList("unknown", 2000);
    }
}
