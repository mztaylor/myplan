package org.kuali.student.myplan.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class CourseLinkBuilderTest {

    private String rawText, cookedText;

    public CourseLinkBuilderTest(String rawText, String cookedText) {
        this.rawText = rawText;
        this.cookedText = cookedText;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                //  Simple course code expansion.
                { "20 credits, including ECON 200 and ECON 201:", "20 credits, including <a href=\"/student/plan&param=p1\" title=\"Title Text\">ECON 200</a> and <a href=\"/student/plan&param=p1\" title=\"Title Text\">ECON 201</a>:" },
                { "1) You must complete ECON 200:", "1) You must complete <a href=\"/student/plan&param=p1\" title=\"Title Text\">ECON 200</a>:" },

        };
        return Arrays.asList(data);
    }

    @Test
    public void testLinkBuilder() {
        assertEquals(CourseLinkBuilder.insertLinks(rawText, CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS), cookedText);
    }
}
