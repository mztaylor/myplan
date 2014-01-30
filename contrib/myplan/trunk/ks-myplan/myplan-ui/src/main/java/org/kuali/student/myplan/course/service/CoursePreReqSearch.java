package org.kuali.student.myplan.course.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.util.SearchHelper;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CoursePreReqSearch {

    private transient CluService luService;


    private CourseHelper courseHelper;

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    protected CluService getLuService() {
        if (this.luService == null) {
            this.luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return this.luService;
    }

    public void setLuService(CluService luService) {
        this.luService = luService;
    }


    /**
     * @param subject eg "A A", "CHEM", aka division
     * @return
     */
    public List<String> getCoursePreReqBySubject(String subject) {
        try {

            ArrayList<String> courseList = new ArrayList<String>();
            SearchRequestInfo req = new SearchRequestInfo("myplan.course.prereqsearch.subject");
            req.addParam("subject", subject);
            SearchResultInfo result = getLuService().search(req, CourseSearchConstants.CONTEXT_INFO);
            for (SearchResultRow row : result.getRows()) {
                String cluid = SearchHelper.getCellValue(row, "lu.resultColumn.cluId");
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
            SearchRequestInfo req = new SearchRequestInfo("myplan.course.prereqsearch.range");
            req.addParam("subject", subject);
            req.addParam("range", range);
            SearchResultInfo result = getLuService().search(req, CourseSearchConstants.CONTEXT_INFO);
            for (SearchResultRow row : result.getRows()) {
                String cluid = SearchHelper.getCellValue(row, "lu.resultColumn.cluId");
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
            SearchRequestInfo req = new SearchRequestInfo("myplan.course.prereqsearch.exclusions");
            req.addParam("subject", subject);
            req.addParam("range", range);
            SearchResultInfo result = getLuService().search(req, CourseSearchConstants.CONTEXT_INFO);
            for (SearchResultRow row : result.getRows()) {
                String cluid = SearchHelper.getCellValue(row, "lu.resultColumn.cluId");
                String code = SearchHelper.getCellValue(row, "lu.resultColumn.luOptionalCode");
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
