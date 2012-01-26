package org.kuali.student.myplan.course;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Test Class for Course Search Controller
 * User: kmuthu
 * Date: 12/20/11
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})
public class CourseSearchControllerTest {

    @Autowired
    private CourseSearchController searchController;

    public CourseSearchController getSearchController() {
        return searchController;
    }

    public void setSearchController(CourseSearchController searchController) {
        this.searchController = searchController;
    }

    @Test
    public void testHitComparator() {
        CourseSearchController.HitComparator comparator = new CourseSearchController.HitComparator();

        CourseSearchController.Hit hit1 = new CourseSearchController.Hit( "a" );
        hit1.count++;

        CourseSearchController.Hit hit2 = new CourseSearchController.Hit( "b" );
        hit2.count++;
        hit2.count++;
        hit2.count++;

        assertTrue( comparator.compare( hit1, hit2 ) > 0 );
        assertTrue( comparator.compare( hit2, hit1 ) < 0 );
        assertTrue(comparator.compare(hit1, hit1) == 0);
        assertTrue(comparator.compare(hit1, null) > 0);
        assertTrue(comparator.compare(null, hit2) < 0);
    }

    @Test
    public void testGetCellValue() {
        CourseSearchController controller = getSearchController();
        SearchResultRow row = new SearchResultRow();
        row.addCell( "key", "value" );

        assertEquals( "value", controller.getCellValue( row, "key" ));

        try {
            controller.getCellValue( row, "fail" );
            fail( "should have throw exception");
        }
        catch( Exception e ) {}
    }

    @Test
    public void testGetCreditMap() {
        CourseSearchController controller = getSearchController();
        HashMap<String, CourseSearchController.Credit> map = controller.getCreditMap();
        assertFalse( map.isEmpty() );
    }

    @Test
    public void testGetCreditByID() {
        CourseSearchController controller = getSearchController();

        CourseSearchController.Credit nothing = controller.getCreditByID("nothing");
        assertNull(nothing);

        CourseSearchController.Credit something = controller.getCreditByID( "kuali.creditType.credit.degree.1-4" );
        assertNotNull(something);
    }

    // FIXME: @Test
    public void testSearchForCoursesExactMatch() {

        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery("CHEM 453");
        form.setCampusSeattle(true);
        form.setCampusBothell(true);
        form.setCampusTacoma(true);

        searchController.searchForCourses(form, null, null, null);

        List<CourseSearchItem> results = form.getCourseSearchResults();
        assertEquals(1, results.size());
        CourseSearchItem course = results.get(0);
        assertEquals("CHEM   453", course.getCode());
        assertEquals("CHEM", course.getSubject());
        assertEquals("453", course.getNumber());
        assertEquals("400", course.getLevel());
        assertEquals("3", course.getCredit());
    }

    // FIXME: @Test
    public void testSearchForCoursesSubjectArea() {

        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery("HDCE");
        form.setCampusSeattle(true);
        form.setCampusBothell(true);
        form.setCampusTacoma(true);

        searchController.searchForCourses(form, null, null, null);

        List<CourseSearchItem> results = form.getCourseSearchResults();
        assertTrue( results.size() > 0 );
    }

    //FIXME: @Test
    public void testSearchForCoursesSubjectAreaLevel() {

        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery("ENGL 1xx");
        form.setCampusSeattle(true);
        form.setCampusBothell(true);
        form.setCampusTacoma(true);

        searchController.searchForCourses(form, null, null, null);

        List<CourseSearchItem> results = form.getCourseSearchResults();
        assertTrue( results.size() > 0 );
    }

    //FIXME: @Test
    public void testIsCourseOffered() {

        CourseSearchForm form = new CourseSearchForm();
        CourseSearchItem course = new CourseSearchItem();
        CourseSearchController controller = getSearchController();

        try {
            form.setSearchTerm(CourseSearchForm.SEARCH_TERM_ANY_ITEM);

            assertTrue(controller.isCourseOffered(form, course));

            form.setSearchTerm("fake");
            course.setCode( "CHEM" );
            assertTrue(controller.isCourseOffered(form, course));

            course.setCode( "FAKE" );
            assertFalse(controller.isCourseOffered(form, course));
        } catch (Exception e) {
            fail( "failed!" );
        }
    }
}
