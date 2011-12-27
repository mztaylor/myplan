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
package org.kuali.student.myplan.course.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import edu.uw.kuali.student.myplan.util.TermInfoComparator;
import org.apache.log4j.Logger;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@RequestMapping(value = "/course")
public class CourseSearchController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(CourseSearchController.class);

    private static final int MAX_HITS = 250;

    private transient LuService luService;

    private transient AtpService atpService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient Map<String, String> atpCache;

    @Autowired
    private TermInfoComparator atpTypeComparator;

    @Autowired
    private CourseSearchStrategy searcher = new CourseSearchStrategy();

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new CourseSearchForm();
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        return getUIFModelAndView(form);
    }

    public class Hit {
        String courseID;
        int count = 0;

        public Hit(String courseID) {
            this.courseID = courseID;
            count = 1;
        }
    }

    public class HitComparator implements Comparator<Hit> {
        @Override
        public int compare(Hit x, Hit y) {
            return y.count - x.count;
        }
    }


    public class Credit {
        String id;
        String display;
        float min;
        float max;
        CourseSearchItem.CreditType type;
    }

    int maxHits = 250;

    public HashMap<String, Credit> getCreditMap()
    {
        HashMap<String, Credit> creditMap = new HashMap<String, Credit>();

        try {
            SearchRequest searchRequest = new SearchRequest("myplan.course.info.credits.details");
            List<SearchParam> params = new ArrayList<SearchParam>();
            searchRequest.setParams(params);
            SearchResult searchResult = getLuService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {
                Iterator<SearchResultCell> i = row.getCells().iterator();
                String id = i.next().getValue();
                String type = i.next().getValue();
                String min = i.next().getValue();
                String max = i.next().getValue();
                Credit credit = new Credit();
                credit.id = id;
                credit.min = Float.valueOf(min);
                credit.max = Float.valueOf(max);
                if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE.equals(type)) {
                    credit.display = min + ", " + max;
                    credit.type = CourseSearchItem.CreditType.multiple;
                } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE.equals(type)) {
                    credit.display = min + "-" + max;
                    credit.type = CourseSearchItem.CreditType.range;
                } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED.equals(type)) {
                    credit.display = min;
                    credit.type = CourseSearchItem.CreditType.fixed;
                }

                creditMap.put(id, credit);
            }
            return creditMap;
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public Credit getCreditByID( String id ) {
        Credit credit;
        HashMap<String, Credit> creditMap  = getCreditMap();
        if (getCreditMap().containsKey(id)) {
            credit = creditMap.get(id);
        } else {
            credit = creditMap.get("u");
        }
        return credit;
    }

    @RequestMapping(params = "methodToCall=searchForCourses")
    public ModelAndView searchForCourses(@ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) {


        //  Initialize facets.
        CurriculumFacet curriculumFacet = new CurriculumFacet();
        CreditsFacet creditsFacet = new CreditsFacet();
        CourseLevelFacet courseLevelFacet = new CourseLevelFacet();
        GenEduReqFacet genEduReqFacet = new GenEduReqFacet();
        TermsFacet termsFacet = new TermsFacet();
        ScheduledTermsFacet scheduledTermsFacet = new ScheduledTermsFacet();

        try {
            List<SearchRequest> requests = searcher.queryToRequests(form);

            HashMap<String, Hit> courseMap = new HashMap<String, Hit>();

            for (SearchRequest searchRequest : requests) {
                SearchResult searchResult = getLuService().search(searchRequest);
                for (SearchResultRow row : searchResult.getRows()) {
                    for (SearchResultCell cell : row.getCells()) {
                        if ("lu.resultColumn.cluId".equals(cell.getKey())) {
                            String courseId = cell.getValue();
                            Hit hit = null;
                            if (courseMap.containsKey(courseId)) {
                                hit = courseMap.get(courseId);
                                hit.count++;
                            } else {
                                hit = new Hit(courseId);
                                courseMap.put(courseId, hit);
                            }
                        }
                    }
                }
            }


            ArrayList<CourseSearchItem> searchResults = new ArrayList<CourseSearchItem>();

            ArrayList<Hit> hits = new ArrayList<Hit>( courseMap.values());
            Collections.sort(hits, new HitComparator());

            for (Hit hit : hits) {
                String courseId = hit.courseID;

                {
                    CourseSearchItem course = new CourseSearchItem();
                    {
                        SearchRequest searchRequest = new SearchRequest( "myplan.course.info" );
                        searchRequest.addParam("courseID", courseId);

                        SearchResult searchResult = getLuService().search(searchRequest);
                        for (SearchResultRow row : searchResult.getRows()) {
                            Iterator<SearchResultCell> i = row.getCells().iterator();
                            String name = i.next().getValue();
                            String number = i.next().getValue();
                            String subject = i.next().getValue();
                            String level = i.next().getValue();
                            String id = i.next().getValue();
                            String cd = i.next().getValue();

                            course.setCourseId(courseId);
                            course.setSubject(subject);
                            course.setNumber(number);
                            course.setLevel(level);
                            course.setCourseName(name);
                            course.setCode(cd);

                            Credit credit = null;
                            credit = getCreditByID( id );
                            course.setCreditMin(credit.min);
                            course.setCreditMax(credit.max);
                            course.setCreditType(credit.type);
                            course.setCredit(credit.display);
                        }
                    }

                    /*
                     *  If the "any" item was chosen in the terms dop-down then continue processing.
                     *  Otherwise, determine if the CourseSearchItem should be filtered out of the
                     *  result set.
                     */
                    if ( ! form.getSearchTerm().equals(CourseSearchForm.SEARCH_TERM_ANY_ITEM)) {
                        /*
                          Use the course offering service to see if the course is being offered in the selected term.
                          Note: In the UW implementation of the Course Offering service, course id is actually course code.
                        */
                        List<String> courseCodes = getCourseOfferingService()
                            .getCourseOfferingIdsByTermAndSubjectArea(form.getSearchTerm(), course.getSubject(), null);

                        if ( ! courseCodes.contains(course.getCode())) {
                            //  The course code is not in the list, so move on to the next item.
                            continue;
                        }
                    }

                    //  Load scheduled terms.
                    {
                        List<String> scheduledTerms = new ArrayList<String>();

                        //  Fetch the available terms from the Academic Calendar Service.
                        List<TermInfo> termInfos = null;
                        try {
                            termInfos = getAcademicCalendarService().getCurrentTerms(CourseSearchConstants.PROCESS_KEY,
                                CourseSearchConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Web service call failed.", e);
                        }
                        //  If the course is offered in the term then add the term info to the scheduled terms list.
                        if (termInfos != null) {
                            for (TermInfo ti : termInfos) {
                                List<String> offerings = getCourseOfferingService()
                                    .getCourseOfferingIdsByTermAndSubjectArea(ti.getKey(), course.getSubject(), null);
                                if (offerings.contains(course.getCode())) {
                                    scheduledTerms.add(ti.getName());
                                }
                            }
                        }
                        course.setScheduledTerms(scheduledTerms);
                    }

                    // Load Terms Offered.
                    {
                        SearchRequest searchRequest = new SearchRequest("myplan.course.info.atp");
                        searchRequest.addParam("courseID", courseId);
                        List<AtpTypeInfo> termsOffered = new ArrayList<AtpTypeInfo>();
                        SearchResult searchResult = getLuService().search(searchRequest);
                        for (SearchResultRow row : searchResult.getRows()) {
                            for (SearchResultCell cell : row.getCells()) {
                                String term = cell.getValue();

                                // Don't add the terms that are not found
                                AtpTypeInfo atpType = getATPType(term);
                                if (null != atpType) {
                                    termsOffered.add(atpType);
                                }
                            }
                        }

                        Collections.sort(termsOffered, getAtpTypeComparator() );
                        course.setTermInfoList(termsOffered);
                    }

                    // Load Gen Ed Requirements
                    {
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setSearchKey("myplan.course.info.gened");
                        List<SearchParam> params = new ArrayList<SearchParam>();
                        params.add(new SearchParam("courseID", courseId));
                        searchRequest.setParams(params);
                        List<String> genEdReqs = new ArrayList<String>();
                        SearchResult searchResult = getLuService().search(searchRequest);
                        for (SearchResultRow row : searchResult.getRows()) {
                            for (SearchResultCell cell : row.getCells()) {
                                String genEd = cell.getValue();
                                genEdReqs.add(genEd);
                            }
                        }
                        String formatted = formatGenEduReq(genEdReqs);
                        course.setGenEduReq(formatted);
                    }

                    //  Update facet info and code the item.
                    curriculumFacet.process(course);
                    courseLevelFacet.process(course);
                    genEduReqFacet.process(course);
                    creditsFacet.process(course);
                    termsFacet.process(course);
                    scheduledTermsFacet.process(course);

                    searchResults.add(course);
                }

                if(searchResults.size() >= MAX_HITS) {
                    break;
                }
            }

            //  Add the facet data to the response.
            form.setCurriculumFacetItems(curriculumFacet.getFacetItems());
            form.setCreditsFacetItems(creditsFacet.getFacetItems());
            form.setGenEduReqFacetItems(genEduReqFacet.getFacetItems());
            form.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
            form.setTermsFacetItems(termsFacet.getFacetItems());
            form.setScheduledTermsFacetItems(scheduledTermsFacet.getFacetItems());

            //  Add the search results to the response.
            form.setCourseSearchResults(searchResults);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getUIFModelAndView(form, CourseSearchConstants.COURSE_SEARCH_RESULT_PAGE);
    }

    //TODO: Change this to using the Enumeration Service to get the Gen Edd Display Value
    private String formatGenEduReq(List<String> genEduRequirements) {

        //  Make the order predictable.
        Collections.sort(genEduRequirements);
        StringBuilder genEdsOut = new StringBuilder();
        for (String req : genEduRequirements) {
            if (genEdsOut.length() != 0) {
                genEdsOut.append(", ");
            }
            req = req.replace(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX, "");
            genEdsOut.append(req);
        }
        return genEdsOut.toString();
    }

    private AtpTypeInfo getATPType(String key) {
        try {
            return getAtpService().getAtpType(key);
        } catch (Exception e) {
            logger.error("Could not find ATP Type: " + key);
            return null;
        }
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    protected AtpService getAtpService() {
        if (this.atpService == null) {
            // TODO: Namespace should not be hard-coded.
            this.atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return this.atpService;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
           this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

    public void setLuService(LuService luService) {
        this.luService = luService;
    }

    public void setAtpService(AtpService atpService) {
        this.atpService = atpService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    public TermInfoComparator getAtpTypeComparator() {
        return atpTypeComparator;
    }

    public void setAtpTypeComparator(TermInfoComparator atpTypeComparator) {
        this.atpTypeComparator = atpTypeComparator;
    }

    public CourseSearchStrategy getSearcher() {
        return searcher;
    }

    public void setSearcher(CourseSearchStrategy searcher) {
        this.searcher = searcher;
    }
}


