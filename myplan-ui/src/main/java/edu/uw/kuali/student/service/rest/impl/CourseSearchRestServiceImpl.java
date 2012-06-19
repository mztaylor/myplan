package edu.uw.kuali.student.service.rest.impl;

import edu.uw.kuali.student.myplan.util.TermInfoComparator;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.web.format.FormatException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.controller.CourseSearchStrategy;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * REST service to return course search results
 *
 * @Author kmuthu
 * Date: 3/30/12
 */
public class CourseSearchRestServiceImpl extends ServerResource {
    private final Logger logger = Logger.getLogger(CourseSearchRestServiceImpl.class);


    //  Java to JSON outputter.
    private transient ObjectMapper mapper = new ObjectMapper();


    @Get
    public String getResource() {

        String courseSearchJson = null;
        CourseSearchController controller = new CourseSearchController();
        List<CourseSearchItem> courses = new ArrayList<CourseSearchItem>();

        String user = null;
        if (getClientInfo().getPrincipals().size() > 0) {
            user = getClientInfo().getPrincipals().get(0).getName();
            logger.info("Course search using USER:" + user);
        } else {
            user = "admin";
        }


        /*Params from the Url*/
        String queryText = getRequest().getAttributes().get("queryText").toString();
        if (queryText.contains("%20")) {
            queryText = queryText.replace("%20", " ");
        }
        String termParam = getRequest().getAttributes().get("termParam").toString();
        String campusParam = getRequest().getAttributes().get("campusParam").toString();


        /*populating the form with the params*/
        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery(queryText);
        form.setCampusSelect(campusParam);
        form.setSearchTerm(termParam);

        /*populating the CourseSearchItem list*/

        controller.setAtpTypeComparator(new TermInfoComparator());
        courses =controller.courseSearch(form, user);

        /*Building the Json String*/
        StringBuilder jsonString = new StringBuilder();
        jsonString = jsonString.append("{ \"aaData\":[");
        int count = 0;
        for (CourseSearchItem item : courses) {
            String scheduledAndOfferedTerms = null;
            String status="";
            try {
                scheduledAndOfferedTerms = mapper.writeValueAsString(item.getScheduledAndOfferedTerms());
            } catch (IOException e) {
                throw new RuntimeException("Could not write the value using mapper", e);
            }
            if(item.getStatus().getLabel().length()>0){
                status = "<span id=\\\"" + item.getCourseId() + "_status\\\">"+item.getStatus().getLabel()+"</span>";
            } else {
                status = "<span id=\\\"" + item.getCourseId() + "_status\\\"><input type=\\\"image\\\" title=\\\"Bookmark This Course\\\" src=\\\"/student/ks-myplan/images/btnAdd.png\\\" alt=\\\"Save to Your Courses List\\\" class=\\\"uif-field uif-imageField\\\" onclick=\\\"myPlanAjaxPlanItemMove('"+item.getCourseId()+"', 'courseId', 'addSavedCourse', event);\\\" /></span>";
            }
            String courseName ="";
            if(item.getCourseName()!=null){
                    courseName=item.getCourseName().replace("\"","'");
            }

            jsonString = jsonString.append("[\"").append(item.getCode()).
                    append("\",\"").append(" <a href=\\").
                    append("\"inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=").
                    append(item.getCourseId()).append("\\").append("\" target=\\").append("\"_self\\").
                    append("\" title=\\").append("\"").append(courseName).append("\\").append("\"").
                    append(" style=\\").append("\"width: 171px;\\").append("\" class=\\").
                    append("\"myplan-text-ellipsis\\").append("\"  >").append(courseName).append("</a>\"").append(",\"").
                    append(item.getCredit()).append("\",").append(scheduledAndOfferedTerms).append(",\"").
                    append(item.getGenEduReq()).append("\",\"").append(status).
                    append("\",\"").append(item.getTermsFacetKeys()).
                    append("\",\"").append(item.getGenEduReqFacetKeys()).
                    append("\",\"").append(item.getCreditsFacetKeys()).
                    append("\",\"").append(item.getCourseLevelFacetKeys()).
                    append("\",\"").append(item.getCurriculumFacetKeys()).
                    append("\"]").append(", ");
        }
        String jsonStr=null;
        if(!jsonString.toString().equalsIgnoreCase("{ \"aaData\":[")){
            jsonStr = jsonString.substring(0, jsonString.lastIndexOf(","));
        }                  else {
            jsonStr=jsonString.toString();
        }
        jsonStr = jsonStr + "]" + "}";

        return jsonStr;

    }


}
