package org.kuali.student.myplan.course;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

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
    public void testSearchLevel() {

        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery("CHEM 453");
        form.setCampusSeattle(true);
        form.setCampusBothell(true);
        form.setCampusTacoma(true);

        searchController.searchForCourses(form, null, null, null);

        assertEquals(1, form.getCourseSearchResults().size());
        assertEquals("CHEM   453", form.getCourseSearchResults().get(0).getCode());
    }
}
