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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.field.MessageField;
import org.kuali.rice.krad.uif.util.ComponentFactory;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;
import org.kuali.student.myplan.course.dataobject.SavedCoursesService;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    public static class Hit {
        public String courseID;
        public int count = 0;

        public Hit(String courseID) {
            this.courseID = courseID;
            count = 1;
        }

        @Override
        public boolean equals( Object other ) {
            return courseID.equals( ((Hit)other).courseID );
        }

        @Override
        public int hashCode() {
            return courseID.hashCode();
        }
    }

    public static class HitComparator implements Comparator<Hit> {
        @Override
        public int compare(Hit x, Hit y) {
            if( x == null ) return -1;
            if( y == null ) return 1;
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

    public String getCellValue( SearchResultRow row, String key ) {
        for( SearchResultCell cell : row.getCells() ) {
            if( key.equals( cell.getKey() )) {
                return cell.getValue();
            }
        }
        throw new RuntimeException( "cell result '" + key + "' not found" );
    }

    public HashMap<String, Credit> getCreditMap() {
        HashMap<String, Credit> creditMap = new HashMap<String, Credit>();

        try {
            SearchRequest searchRequest = new SearchRequest("myplan.course.info.credits.details");
            List<SearchParam> params = new ArrayList<SearchParam>();
            searchRequest.setParams(params);
            SearchResult searchResult = getLuService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = getCellValue( row, "credit.id" );
                String type = getCellValue( row, "credit.type" );
                String min = getCellValue( row, "credit.min" );
                String max = getCellValue( row, "credit.max" );
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
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {
       try {
            List<SearchRequest> requests = searcher.queryToRequests(form);

            List<Hit> hits = processSearchRequests(requests);

            List<CourseSearchItem> courseList = new ArrayList<CourseSearchItem>();

            for (Hit hit : hits) {
                CourseSearchItem course = getCourseInfo( hit.courseID );
                if( isCourseOffered(form, course)) {
                    loadScheduledTerms(course);
                    loadTermsOffered(course);
                    loadGenEduReqs(course);

                    courseList.add(course);

                    if (courseList.size() >= MAX_HITS) {
                        break;
                    }
                }
            }

            populateFacets( form, courseList );

            //  Add the search results to the response.
            form.setCourseSearchResults(courseList);

            return getUIFModelAndView(form, CourseSearchConstants.COURSE_SEARCH_RESULT_PAGE);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void hitCourseID( Map<String, Hit> courseMap, String id ) {
        Hit hit = null;
        if( courseMap.containsKey( id )) {
            hit = courseMap.get( id );
            hit.count++;
        } else {
            hit = new Hit( id );
            courseMap.put( id, hit );
        }
    }

    public ArrayList<Hit> processSearchRequests(List<SearchRequest> requests) throws MissingParameterException {
        HashMap<String, Hit> courseMap = new HashMap<String, Hit>();

        for (SearchRequest request : requests) {
            SearchResult searchResult = getLuService().search(request);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = getCellValue( row, "lu.resultColumn.cluId" );
                hitCourseID( courseMap, id );
            }
        }

        ArrayList<Hit> hits = new ArrayList<Hit>( courseMap.values() );
        Collections.sort(hits, new HitComparator());
        return hits;
    }

    public boolean isCourseOffered(CourseSearchForm form, CourseSearchItem course) throws Exception {
        /*
         *  If the "any" item was chosen in the terms dop-down then continue processing.
         *  Otherwise, determine if the CourseSearchItem should be filtered out of the
         *  result set.
         */
        String term = form.getSearchTerm();

        if( CourseSearchForm.SEARCH_TERM_ANY_ITEM.equals( term )) return true;

        /*
          Use the course offering service to see if the course is being offered in the selected term.
          Note: In the UW implementation of the Course Offering service, course id is actually course code.
        */
        CourseOfferingService service = getCourseOfferingService();

        String subject = course.getSubject();
        List<String> codes = service.getCourseOfferingIdsByTermAndSubjectArea(term, subject, null);

        //  The course code is not in the list, so move on to the next item.
        String code = course.getCode();
        boolean result = codes.contains(code);
        return result;
    }

    //  Load scheduled terms.
    //  Fetch the available terms from the Academic Calendar Service.
    private void loadScheduledTerms(CourseSearchItem course) {
        try {
            AcademicCalendarService atpService = getAcademicCalendarService();

            List<TermInfo> terms = atpService.getCurrentTerms(CourseSearchConstants.PROCESS_KEY,
                    CourseSearchConstants.CONTEXT_INFO);

            CourseOfferingService offeringService = getCourseOfferingService();

            //  If the course is offered in the term then add the term info to the scheduled terms list.
            String code = course.getCode();

            for (TermInfo term : terms) {

                String key = term.getKey();
                String subject = course.getSubject();

                List<String> offerings = offeringService
                        .getCourseOfferingIdsByTermAndSubjectArea(key, subject, CourseSearchConstants.CONTEXT_INFO);
                if (offerings.contains(code)) {
                    course.addScheduledTerm( term.getName() );
                }
            }
        } catch (Exception e) {
            // TODO: Eating this error sucks
            logger.error("Web service call failed.", e);
        }
    }

    private void loadTermsOffered(CourseSearchItem course) throws MissingParameterException {
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.atp");
        request.addParam("courseID", courseId);

        List<AtpTypeInfo> termsOffered = new ArrayList<AtpTypeInfo>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String id = getCellValue( row, "atp.id" );

            // Don't add the terms that are not found
            AtpTypeInfo atpType = getATPType(id);
            if (null != atpType) {
                termsOffered.add(atpType);
            }
        }

        Collections.sort(termsOffered, getAtpTypeComparator());
        course.setTermInfoList(termsOffered);
    }

    private void loadGenEduReqs(CourseSearchItem course) throws MissingParameterException {
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.gened");
        request.addParam("courseID", courseId);
        List<String> reqs = new ArrayList<String>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String genEd = getCellValue( row, "gened.name");
            reqs.add(genEd);
        }
        String formatted = formatGenEduReq(reqs);
        course.setGenEduReq(formatted);
    }

    private CourseSearchItem getCourseInfo(String courseId) throws MissingParameterException {
        CourseSearchItem course = new CourseSearchItem();

        SearchRequest request = new SearchRequest( "myplan.course.info" );
        request.addParam("courseID", courseId);
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String name = getCellValue( row, "course.name" );
            String number = getCellValue( row, "course.number" );
            String subject = getCellValue( row, "course.subject" );
            String level = getCellValue( row, "course.level" );
            String creditsID = getCellValue( row, "course.credits" );
            String code = getCellValue( row, "course.code" );

            course.setCourseId(courseId);
            course.setSubject(subject);
            course.setNumber(number);
            course.setLevel(level);
            course.setCourseName(name);
            course.setCode(code);

            Credit credit = getCreditByID( creditsID );
            course.setCreditMin(credit.min);
            course.setCreditMax(credit.max);
            course.setCreditType(credit.type);
            course.setCredit(credit.display);
            break;
        }
        return course;
    }

    public void populateFacets( CourseSearchForm form, List<CourseSearchItem> courses ) {
        //  Initialize facets.
        CurriculumFacet curriculumFacet = new CurriculumFacet();
        CreditsFacet creditsFacet = new CreditsFacet();
        CourseLevelFacet courseLevelFacet = new CourseLevelFacet();
        GenEduReqFacet genEduReqFacet = new GenEduReqFacet();
        TermsFacet termsFacet = new TermsFacet();

        //  Update facet info and code the item.
        for( CourseSearchItem course : courses ) {
            curriculumFacet.process(course);
            courseLevelFacet.process(course);
            genEduReqFacet.process(course);
            creditsFacet.process(course);
            termsFacet.process(course);
        }

        //  Add the facet data to the response.
        form.setCurriculumFacetItems(curriculumFacet.getFacetItems());
        form.setCreditsFacetItems(creditsFacet.getFacetItems());
        form.setGenEduReqFacetItems(genEduReqFacet.getFacetItems());
        form.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
        form.setTermsFacetItems(termsFacet.getFacetItems());
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
            this.courseOfferingService = (CourseOfferingService)
                GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
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

    private SavedCoursesService savedCoursesService;

    public SavedCoursesService getSavedCoursesService() {
        if( savedCoursesService == null ) {
            savedCoursesService = new SavedCoursesService();
        }
        return savedCoursesService;
    }

    public void setSavedCoursesService( SavedCoursesService savedCourses ) {
        this.savedCoursesService = savedCourses;
    }
}


