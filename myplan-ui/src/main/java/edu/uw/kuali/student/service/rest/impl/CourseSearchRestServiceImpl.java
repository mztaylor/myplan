package edu.uw.kuali.student.service.rest.impl;

import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * REST service to return course search results
 *
 * @Author kmuthu
 * Date: 3/30/12
 */
public class CourseSearchRestServiceImpl extends ServerResource {

    @Get
    public String getResource() {
        String queryText = getQuery().getValues("queryText");


        CourseSearchController controller = new CourseSearchController();
        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery(queryText);
        form.setCampusSelect("0");

        List<CourseSearchItem> courses = null; // controller.searchForCourses(form);


        StringBuilder sb = new StringBuilder();

        sb.append("{ \"aaData\": [\n");

        int i = 0;
        for (CourseSearchItem course : courses) {
            i++;
            sb.append("[\"" + course.getCode() + "\",");
            sb.append("\"<a id=\\\"629_line0\\\" href=\\\"inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + course.getCourseId() + "\\\" target=\\\"_self\\\" title=\\\"" + course.getCourseName() + "\\\"      style=\\\"width: 175px;\\\" class=\\\"myplan-text-ellipsis\\\"  >" + course.getCourseName() + "</a>\",");
            sb.append("\"3\",");
            sb.append("\"<dl><dd class=\\\"projected\\\"><dl><dd>AU</dd></dl></dd></dl>\",\"NW\",\"    In List\",\";Unknown;;Projected AU;\",\"   \",\";;NW;;\",\";3;\",\";100;\",\";CHEM;\"]");
            if(i < courses.size()) {
                sb.append(",\n");
            }

        }

        sb.append("\n] }");


        return sb.toString();

    }
}
