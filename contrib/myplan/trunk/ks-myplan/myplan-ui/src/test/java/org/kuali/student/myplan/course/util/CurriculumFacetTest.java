package org.kuali.student.myplan.course.util;

import org.junit.Test;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CurriculumFacetTest {

    @Test
    public void testGetFacetItems() throws Exception {
        CurriculumFacet facet = new CurriculumFacet();
        CourseSearchItem course1 = new CourseSearchItem();
        course1.setSubject("ABC");
        facet.process( course1 );
        CourseSearchItem course2 = new CourseSearchItem();
        course2.setSubject("XYZ");
        facet.process( course2 );

        List<FacetItem> list = facet.getFacetItems();

        assertTrue( list.size() == 2 );
        assertEquals( list.get( 0 ).getDisplayName(), "ABC" );
        assertEquals( list.get( 0 ).getKey(), ";ABC;" );
        assertEquals( list.get( 1 ).getDisplayName(), "XYZ" );
        assertEquals( list.get( 1 ).getKey(), ";XYZ;" );
    }

    @Test
    public void testProcess() throws Exception {

        CurriculumFacet facet = new CurriculumFacet();
        CourseSearchItem course = new CourseSearchItem();
        course.setSubject( "ABC" );
        facet.process( course );

        Set<String> keys = course.getCurriculumFacetKeys();

        assertFalse(keys.isEmpty());
        assertEquals(1, keys.size());
        assertTrue( keys.contains( ";ABC;" ));
    }
}
