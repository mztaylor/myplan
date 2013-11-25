package edu.uw.kuali.student.myplan.tests.unit;

import edu.uw.kuali.student.myplan.util.TermInfoComparator;
import org.junit.Test;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TermInfoComparatorTest {

    @Test
    public void circularTermListExerciseTerms() {
        TypeInfo t1 = new TypeInfo();
        t1.setKey("kuali.uw.atp.type.Spring");

        TypeInfo t2 = new TypeInfo();
        t2.setKey("kuali.uw.atp.type.Winter");

        TypeInfo t3 = new TypeInfo();
        t3.setKey("kuali.uw.atp.type.Fall");

        TypeInfo t4 = new TypeInfo();
        t4.setKey("kuali.uw.atp.type.Summer");

        List<TypeInfo> sorted = new ArrayList<TypeInfo>();
        sorted.add(t3);
        sorted.add(t2);
        sorted.add(t1);
        sorted.add(t4);
        sorted.add(t4);

        List<TypeInfo> mixed = new ArrayList<TypeInfo>();
        mixed.add(t4);
        mixed.add(t1);
        mixed.add(t2);
        mixed.add(t3);
        mixed.add(t4);

        Collections.sort(mixed, new TermInfoComparator());

        assertEquals(mixed, sorted);
    }
}
