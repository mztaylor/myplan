/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.student.myplan.quickAdd.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.EnrollmentStatusHelperImpl;
import org.kuali.student.myplan.plan.util.EnrollmentStatusHelperImpl.CourseCode;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;


public class QuickAddSuggestHelperService {

    public static final Logger logger = Logger.getLogger(QuickAddSuggestHelperService.class);

    public static CourseSearchController searchController = new CourseSearchController();

    public static transient boolean isAcademicCalendarServiceUp = true;

    public static transient boolean isAcademicRecordServiceUp = true;

    public static transient boolean isCourseOfferingServiceUp = true;

    public static transient CourseOfferingService courseOfferingService;

    public static transient AcademicCalendarService academicCalendarService;

    public static transient AcademicPlanService academicPlanService;

    public static transient AcademicRecordService academicRecordService;

    private static transient LuService luService;

    public static boolean isAcademicCalendarServiceUp() {
        return isAcademicCalendarServiceUp;
    }

    public static void setAcademicCalendarServiceUp(boolean academicCalendarServiceUp) {
        isAcademicCalendarServiceUp = academicCalendarServiceUp;
    }

    public static boolean isAcademicRecordServiceUp() {
        return isAcademicRecordServiceUp;
    }

    public static void setAcademicRecordServiceUp(boolean academicRecordServiceUp) {
        isAcademicRecordServiceUp = academicRecordServiceUp;
    }

    public static boolean isCourseOfferingServiceUp() {
        return isCourseOfferingServiceUp;
    }

    public static void setCourseOfferingServiceUp(boolean courseOfferingServiceUp) {
        isCourseOfferingServiceUp = courseOfferingServiceUp;
    }

    protected static CourseOfferingService getCourseOfferingService() {
        if (QuickAddSuggestHelperService.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            QuickAddSuggestHelperService.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return QuickAddSuggestHelperService.courseOfferingService;
    }

    protected static AcademicCalendarService getAcademicCalendarService() {
        if (QuickAddSuggestHelperService.academicCalendarService == null) {
            QuickAddSuggestHelperService.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return QuickAddSuggestHelperService.academicCalendarService;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    public static List<String> getSuggestions(String courseCd) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (!Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_COURSE_OFFERING_SERVICE_UP).toString())
                || !Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_CALENDER_SERVICE_UP).toString())
                || !Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_RECORD_SERVICE_UP).toString())) {
            AtpHelper.addServiceError("courseCd");
            QuickAddSuggestHelperService.setAcademicCalendarServiceUp(Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_CALENDER_SERVICE_UP).toString()));
            QuickAddSuggestHelperService.setAcademicRecordServiceUp(Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_RECORD_SERVICE_UP).toString()));
            QuickAddSuggestHelperService.setCourseOfferingServiceUp(Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_COURSE_OFFERING_SERVICE_UP).toString()));
        }
        List<String> results = new ArrayList<String>();
        if (courseCd.length() >= 2) {
            if (StringUtils.hasText(courseCd)) {
                SearchRequest searchRequest = null;
                SearchResult searchResult = null;
                HashMap<String, String> divisionMap = searchController.fetchCourseDivisions();
                /*Params from the Url*/
                String searchText = org.apache.commons.lang.StringUtils.upperCase(courseCd);
                String number = null;
                String subject = null;
                CourseCode courseCode = EnrollmentStatusHelperImpl.getCourseDivisionAndNumber(searchText);
                if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
                    number = courseCode.getNumber();
                    ArrayList<String> divisions = new ArrayList<String>();
                    subject = searchController.extractDivisions(divisionMap, courseCode.getSubject(), divisions, true);
                    if (divisions.size() > 0) {
                        subject = divisions.get(0);
                        searchRequest = new SearchRequest("myplan.clu.divisionAndCode");
                        results = searchController.getResults(searchRequest, subject, number);
                    }
                } else if (courseCode.getSubject() != null && !org.apache.commons.lang.StringUtils.isNumeric(courseCode.getSubject())) {
                    ArrayList<String> divisions = new ArrayList<String>();
                    subject = searchController.extractDivisions(divisionMap, courseCode.getSubject(), divisions, true);
                    if (divisions.size() > 0) {
                        subject = divisions.get(0);
                        searchRequest = new SearchRequest("myplan.clu.division");
                        results = searchController.getResults(searchRequest, subject, number);
                    } else {
                        searchRequest = new SearchRequest("myplan.clu.division");
                        results = searchController.getResults(searchRequest, subject, number);
                    }
                }
                if (results == null || results.size() == 0) {
                    results.add("No courses found");
                }

                /*TODO: uncomment this once a way to pass in atpid is known from the krad*/
                /*if (results.size() > 0) {
                    results = additionalFiltering(results, atpId);
                }*/

            } else {
                results.add("No courses found");
            }
        } else {
            results.add("Search Term Should be at least 2 characters");
        }
        return results;
    }

    public static List<String> additionalFiltering(List<String> results, String atpId) {
        int year = Calendar.getInstance().get(Calendar.YEAR) - 10;
        int resultsSize = results.size();
        if (isCourseOfferingServiceUp()) {
            for (int i = 0; i < resultsSize; i++) {
                CourseCode courseCode = EnrollmentStatusHelperImpl.getCourseDivisionAndNumber(results.get(i));
                List<CourseOfferingInfo> courseOfferingInfo = null;
                boolean removed = false;

                try {

                    /*Filtering courses that are not offered in the given term*/
                    List<String> offerings = getCourseOfferingService()
                            .getCourseOfferingIdsByTermAndSubjectArea(atpId, courseCode.getSubject(), CourseSearchConstants.CONTEXT_INFO);
                    if (!offerings.contains(results.get(i))) {
                        results.remove(results.get(i));
                        resultsSize--;
                        removed = true;
                    }
                    /*Filtering courses that are not offered for more than 10 years*/
                    if (!removed) {
                        String values = String.format("%s, %s, %s", year, courseCode.getSubject(), courseCode.getNumber());
                        courseOfferingInfo = getCourseOfferingService()
                                .searchForCourseOfferings(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("values", values)), CourseSearchConstants.CONTEXT_INFO);
                        if (courseOfferingInfo == null) {
                            results.remove(results.get(i));
                            resultsSize--;
                        }
                    }

                } catch (Exception e) {
                    logger.error("Could not filter results as SWS call failed", e);
                }
            }
        }
        if (results.size() > 9) {
            List<String> trimmedList = new ArrayList<String>();
            for (String result : results) {
                trimmedList.add(result);
                if (trimmedList.size() == 10) {
                    break;
                }

            }
            results = new ArrayList<String>();
            results.addAll(trimmedList);
        }
        return results;
    }
}
