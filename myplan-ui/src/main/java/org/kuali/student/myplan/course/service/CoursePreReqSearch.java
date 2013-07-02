package org.kuali.student.myplan.course.service;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.util.OrgHelper;

import javax.xml.namespace.QName;
import java.util.*;

public class CoursePreReqSearch {

    private transient LuService luService;

    private CourseHelper courseHelper;

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    public void setLuService(LuService luService) {
        this.luService = luService;
    }


    /**
     * @param subject eg "A A", "CHEM", aka division
     * @return
     */
    public List<String> getCoursePreReqBySubject(String subject) {
        try {

            ArrayList<String> courseList = new ArrayList<String>();
            SearchRequest req = new SearchRequest("myplan.course.prereqsearch.subject");
            req.addParam("subject", subject);
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                String cluid = OrgHelper.getCellValue(row, "lu.resultColumn.cluId");
                courseList.add(cluid);
            }
            return courseList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCoursePreReqBySubjectAndRange(String subject, String range) {
        try {
            if (range == null) {
                throw new NullPointerException("range");
            }
            if (range.length() != 3) {
                throw new IllegalArgumentException("range must be 3 chars");
            }
            range = range.toUpperCase().replace("X", "_");

            ArrayList<String> courseList = new ArrayList<String>();
            SearchRequest req = new SearchRequest("myplan.course.prereqsearch.range");
            req.addParam("subject", subject);
            req.addParam("range", range);
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                String cluid = OrgHelper.getCellValue(row, "lu.resultColumn.cluId");
                courseList.add(cluid);
            }
            return courseList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCoursePreReqWithExclusions(String subject, String range, Set<String> excludeList) {
        try {
            if (range == null) {
                throw new NullPointerException("range");
            }
            if (range.length() != 3) {
                throw new IllegalArgumentException("range must be 3 chars");
            }
            range = range.toUpperCase().replace("X", "_");

            ArrayList<String> courseList = new ArrayList<String>();
            SearchRequest req = new SearchRequest("myplan.course.prereqsearch.exclusions");
            req.addParam("subject", subject);
            req.addParam("range", range);
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                String cluid = OrgHelper.getCellValue(row, "lu.resultColumn.cluId");
                String code = OrgHelper.getCellValue(row, "lu.resultColumn.luOptionalCode");
                if (!excludeList.contains(code)) {
                    courseList.add(cluid);
                }
            }
            return courseList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
