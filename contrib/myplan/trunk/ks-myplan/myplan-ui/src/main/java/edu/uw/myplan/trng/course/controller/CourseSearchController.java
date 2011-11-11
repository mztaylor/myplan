/*
 * Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.uw.myplan.trng.course.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import edu.uw.myplan.trng.course.util.*;
import org.apache.log4j.Logger;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.form.CourseSearchForm;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.exceptions.OperationFailedException;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.core.atp.dto.AtpSeasonalTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lrc.dto.ResultComponentInfo;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


@Controller
@RequestMapping(value = "/course")
public class CourseSearchController extends UifControllerBase {

    final Logger logger = Logger.getLogger(CourseSearchController.class);

    private transient LuService luService;

    private transient AtpService atpService;

    private transient CourseService courseService;

    private transient Map<String, String> atpCache;

    private enum QueryParamEnum {
        SUBJECT("lu.queryParam.luOptionalStudySubjectArea", "subjectArea"),
        CODE("lu.queryParam.luOptionalCode", "code"),
        TITLE("lu.queryParam.luOptionalLongName", "courseTitle");

        private final String fieldValue;
        private final String queryKey;

        QueryParamEnum(String qKey, String fValue) {
            this.queryKey = qKey;
            this.fieldValue = fValue;
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public String getQueryKey() {
            return queryKey;
        }
    }

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new CourseSearchForm();
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") CourseSearchForm courseSearchForm, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        return getUIFModelAndView(courseSearchForm);
    }

    @RequestMapping(params = "methodToCall=searchForCourses")
    public ModelAndView searchForCourses(@ModelAttribute("KualiForm") CourseSearchForm courseSearchForm, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) {

        List<CourseSearchItem> searchResults = new ArrayList<CourseSearchItem>();

        String courseId = null;
        SearchParam searchParam = new SearchParam();
        searchParam.setKey("queryText");
        searchParam.setValue(courseSearchForm.getSearchQuery());
        List<SearchParam> params = new ArrayList<SearchParam>();
        params.add( searchParam );
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setParams(params);
        searchRequest.setSearchKey("myplan.lu.search.current");

        //  Initialize facets.
        CurriculumFacet curriculumFacet = new CurriculumFacet();
        CreditsFacet creditsFacet = new CreditsFacet();
        CourseLevelFacet courseLevelFacet = new CourseLevelFacet();
        GenEduReqFacet genEduReqFacet = new GenEduReqFacet();
        TimeScheduleFacet timeScheduleFacet = new TimeScheduleFacet();

        try {
            SearchResult searchResult = getLuService().search(searchRequest);
            //  Increment through search results and populate the CourseSearchItem objects.
            if (searchResult.getRows().size() > 0) {
                for (SearchResultRow srrow : searchResult.getRows()) {
                    List<SearchResultCell> srCells = srrow.getCells();
                    if (srCells != null && srCells.size() > 0) {
                        for (SearchResultCell srcell : srCells) {
                            if (srcell.getKey().equals("lu.resultColumn.cluId")) {
                                courseId = srcell.getValue();
                                CourseInfo course = getCourseService().getCourse(courseId);

                                CourseSearchItem courseSearchItem = new CourseSearchItem();
                                courseSearchItem.setCode(course.getCode());
                                courseSearchItem.setCourseName(course.getCourseTitle());
                                courseSearchItem.setScheduledTime(formatScheduledTime(course));
                                courseSearchItem.setCredit(formatCredits(course));
                                courseSearchItem.setGenEduReq(formatGenEduReq(course));
                                courseSearchItem.setLevel(course.getLevel());
                                // item.setStatus(TBD);

                                //  Update facet info and code the item.
                                curriculumFacet.process(course, courseSearchItem);
                                courseLevelFacet.process(course, courseSearchItem);
                                genEduReqFacet.process(course, courseSearchItem);
                                creditsFacet.process(course, courseSearchItem);
                                timeScheduleFacet.process(course, courseSearchItem);

                                searchResults.add(courseSearchItem);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //  Add the facet data to the response.
        courseSearchForm.setCurriculumFacetItems(curriculumFacet.getFacetItems());
        courseSearchForm.setCreditsFacetItems(creditsFacet.getFacetItems());
        courseSearchForm.setGenEduReqFacetItems(genEduReqFacet.getFacetItems());
        courseSearchForm.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
        courseSearchForm.setTimeScheduleFacetItems(timeScheduleFacet.getFacetItems());

        //  Add the search results to the response.
        courseSearchForm.setCourseSearchResults(searchResults);

        return getUIFModelAndView(courseSearchForm, courseSearchForm.getViewId(), CourseSearchConstants.COURSE_SEARCH_RESULT_PAGE);
    }

    private String formatCredits(CourseInfo courseInfo) {
        String credits = "--";

        List<ResultComponentInfo> options = courseInfo.getCreditOptions();
        if (options.size() == 0) {
            logger.warn("Credit options list was empty.");
            return credits;
        }
        /* At UW this list should only contain one item. */
        if (options.size() > 1) {
            logger.warn("Credit option list contained more than one value.");
        }
        ResultComponentInfo rci = options.get(0);

        /**
         *  Credit values are provided in three formats: FIXED, LIST (Multiple), and RANGE (Variable). Determine the
         *  format and parse it into a String representation.
         */
        String type = rci.getType();
        if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED)) {
            credits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE);
            credits = trimCredits(credits);
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE)) {
            StringBuilder cTmp = new StringBuilder();
            for (String c : rci.getResultValues()) {
                if (cTmp.length() != 0) {
                    cTmp.append(" ,");
                }
                cTmp.append(trimCredits(c));
            }
            credits = cTmp.toString();
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE)) {
            String minCredits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MIN_CREDIT_VALUE);
            String maxCredits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MAX_CREDIT_VALUE);
            credits = minCredits + "-" + maxCredits;
        }
        return credits;
    }

    /**
     *  Drop the decimal point and and trailing zero from credits.
     * @return The supplied value minus the trailing ".0"
     */
    private String trimCredits(String credits) {
        if (credits.endsWith(".0")) {
            credits = credits.substring(0, credits.indexOf("."));
        }
        return credits;
    }

    private String formatScheduledTime(CourseInfo courseInfo) {
        List<String> terms = courseInfo.getTermsOffered();
        Collections.sort(terms);
        StringBuilder termsTmp = new StringBuilder();
        for (String term : terms) {
            if (termsTmp.length() != 0) {
                termsTmp.append(", ");
            }
            termsTmp.append(getTerm(term));
        }
        return termsTmp.toString();
    }

    /**
     * Gets a "term" from the AtpSeasonalTypes cache.
     * @param term The key of the AtpSeasonalTypeInfo
     * @return A String representation of the AtpSeasonalTypeInfo.
     */
    private String getTerm(String term) {
        if (atpCache == null) {
            initializeAtpSeasonalTypesCache();
        }
        String t = atpCache.get(term);
        if (t == null) {
            logger.error("Term [" + term + "] was not found in the ATP cache. Attempting to re-initialize the the cache");
            initializeAtpSeasonalTypesCache();
            t = "?";
        }
        return t;
    }

    /**
     * Initializes ATP term cache.
     * AtpSeasonalTypes rarely change, so fetch them all and store them in a Map.
     */
    private void initializeAtpSeasonalTypesCache() {
        atpCache = new HashMap<String, String>();
        List<AtpSeasonalTypeInfo> atpSeasonalTypeInfos;
        try {
            atpSeasonalTypeInfos = getAtpService().getAtpSeasonalTypes();
        } catch (OperationFailedException e) {
            logger.error("ATP seasonal types lookup failed.", e);
            return;
        }
        for (AtpSeasonalTypeInfo ti : atpSeasonalTypeInfos) {
            atpCache.put(ti.getId(), ti.getName().substring(0,2).toUpperCase());
        }
    }

    private String formatGenEduReq(CourseInfo courseInfo) {
        return "--";
    }

    //Note: here I am using r1 LuService implementation!!!
    protected LuService getLuService() {
        if (luService == null) {
            luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    /**
     * Provides an instance of the AtpService client.
     */
    protected AtpService getAtpService() {
        if (atpService == null) {
            // TODO: Namespace should not be hard-coded.
            atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return this.atpService;
    }

    protected CourseService getCourseService() {
        if (courseService == null) {
            courseService = (CourseService) GlobalResourceLoader.getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }
}
