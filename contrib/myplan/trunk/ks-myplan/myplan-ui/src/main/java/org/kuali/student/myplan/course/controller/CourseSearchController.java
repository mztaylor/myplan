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

import javax.annotation.RegEx;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.xml.namespace.QName;

import edu.uw.kuali.student.myplan.util.TermInfoComparator;
import org.apache.log4j.Logger;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.r2.common.util.constants.CourseOfferingServiceConstants;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/course/**")
public class CourseSearchController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(CourseSearchController.class);

    private static final int MAX_HITS = 250;

    private transient LuService luService;

    private transient AtpService atpService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient Person person;

    private transient PersonImpl personImpl;

    public PersonImpl getPersonImpl() {
        return personImpl;
    }

    public void setPersonImpl(PersonImpl personImpl) {
        this.personImpl = personImpl;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Autowired
    private TermInfoComparator atpTypeComparator;

    @Autowired
    private CourseSearchStrategy searcher = new CourseSearchStrategy();

    private CampusSearch campusSearch = new CampusSearch();

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new CourseSearchForm();
    }

    @RequestMapping(value = "/course/{courseCd}", method = RequestMethod.GET)
    public String get(@PathVariable("courseCd") String courseCd, @ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                      HttpServletRequest request, HttpServletResponse response) {
        String number = "";
        String subject = "";
        String courseId = "";
        courseCd = courseCd.toUpperCase();
        StringBuffer campus = new StringBuffer();
        List<KeyValue> campusKeys = campusSearch.getKeyValues();
        for (KeyValue k : campusKeys) {
            campus.append(k.getKey().toString());
            campus.append(",");
        }
        String[] splitStr = courseCd.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        if (splitStr.length == 2) {
            number = splitStr[1];
            subject = splitStr[0];
        } else {
            StringBuffer splitBuff = new StringBuffer();
            for (int i = 0; i < splitStr.length; i++) {
                splitBuff.append(splitStr[i]);
            }
            return "redirect:/myplan/course?methodToCall=searchForCourses&viewId=CourseSearch-FormView&searchQuery=" + splitBuff + "&searchTerm=any&campusSelect=" + campus;

        }
        HashMap<String, String> divisionMap = fetchCourseDivisions();

        ArrayList<String> divisions = new ArrayList<String>();
        extractDivisions(divisionMap, subject, divisions);
        if (divisions.size() > 0) {
            subject = divisions.get(0);
        }
        SearchRequest searchRequest = new SearchRequest("myplan.course.getcluid");
        SearchResult searchResult = null;
        try {
            searchRequest.addParam("number", number);
            searchRequest.addParam("subject", subject.trim());

            searchResult = getLuService().search(searchRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (SearchResultRow row : searchResult.getRows()) {
            courseId = getCellValue(row, "lu.resultColumn.cluId");
        }
        if (courseId.equalsIgnoreCase("")) {
            return "redirect:/myplan/course?methodToCall=searchForCourses&viewId=CourseSearch-FormView&searchQuery=" + courseCd + "&searchTerm=any&campusSelect=" + campus;

        }


        return "redirect:/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + courseId;


    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                            HttpServletRequest request, HttpServletResponse response) {

        super.start(form, result, request, response);
        CourseSearchForm searchForm = (CourseSearchForm) form;
        form.setViewId("CourseSearch-FormView");
        form.setView(super.getViewService().getViewById("CourseSearch-FormView"));

        return getUIFModelAndView(form);
    }

    @RequestMapping(value = "/course/", method = RequestMethod.GET)
    public String doGet(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                        HttpServletRequest request, HttpServletResponse response) {

        return "redirect:/myplan/course";
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {

        super.start(form, result, request, response);
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
        public boolean equals(Object other) {
            return courseID.equals(((Hit) other).courseID);
        }

        @Override
        public int hashCode() {
            return courseID.hashCode();
        }
    }

    public static class HitComparator implements Comparator<Hit> {
        @Override
        public int compare(Hit x, Hit y) {
            if (x == null) return -1;
            if (y == null) return 1;
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

    public String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }

    public HashMap<String, Credit> getCreditMap() {
        HashMap<String, Credit> creditMap = new HashMap<String, Credit>();

        try {
            SearchRequest searchRequest = new SearchRequest("myplan.course.info.credits.details");
            List<SearchParam> params = new ArrayList<SearchParam>();
            searchRequest.setParams(params);
            SearchResult searchResult = getLuService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = getCellValue(row, "credit.id");
                String type = getCellValue(row, "credit.type");
                String min = getCellValue(row, "credit.min");
                String max = getCellValue(row, "credit.max");
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

    public Credit getCreditByID(String id) {
        Credit credit;
        HashMap<String, Credit> creditMap = getCreditMap();
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
            logger.info("Start Of Method searchForCourses in CourseSearchController:"+System.currentTimeMillis());
                List<SearchRequest> requests = searcher.queryToRequests(form);

            List<Hit> hits = processSearchRequests(requests);
            logger.info("No of actual records pulled in:"+hits.size());
            List<CourseSearchItem> courseList = new ArrayList<CourseSearchItem>();

            Set<String> savedCourseSet = getSavedCourseSet();
            for (Hit hit : hits) {
                CourseSearchItem course = getCourseInfo(hit.courseID);
                if (isCourseOffered(form, course)) {
                    loadScheduledTerms(course);
                    loadTermsOffered(course);
                    loadGenEduReqs(course);
                    if (savedCourseSet.contains(course.getCourseId())) {
                        course.setStatus(CourseSearchItem.PlanState.SAVED);
                    }

                    courseList.add(course);

                    if (courseList.size() >= MAX_HITS) {
                        break;
                    }
                }
            }

            populateFacets(form, courseList);

            logger.info("Start of setting courseSearch Results to form:"+System.currentTimeMillis());
            //  Add the search results to the response.
            form.setCourseSearchResults(courseList);
            logger.info("End of setting courseSearch Results to form:"+System.currentTimeMillis());
            logger.info("End Of Method searchForCourses in CourseSearchController:"+System.currentTimeMillis());

            return getUIFModelAndView(form, CourseSearchConstants.COURSE_SEARCH_RESULT_PAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void hitCourseID(Map<String, Hit> courseMap, String id) {
        Hit hit = null;
        if (courseMap.containsKey(id)) {
            hit = courseMap.get(id);
            hit.count++;
        } else {
            hit = new Hit(id);
            courseMap.put(id, hit);
        }
    }

    public ArrayList<Hit> processSearchRequests(List<SearchRequest> requests) throws MissingParameterException {
        logger.info("Start of processSearchRequests of CourseSearchController:"+System.currentTimeMillis());
        HashMap<String, Hit> courseMap = new HashMap<String, Hit>();

        ArrayList<Hit> hits = new ArrayList<Hit>();
        ArrayList<Hit> tempHits = new ArrayList<Hit>();
        for (SearchRequest request : requests) {
            SearchResult searchResult = getLuService().search(request);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = getCellValue(row, "lu.resultColumn.cluId");
               /* hitCourseID(courseMap, id);*/
                Hit hit= new Hit(id);
                tempHits.add(hit);

            }
            tempHits.removeAll(hits);
            hits.addAll(tempHits);
            tempHits.clear();
        }

//        ArrayList<Hit> hits = new ArrayList<Hit>(courseMap.values());
//        Collections.sort(hits, new HitComparator());
        logger.info("End of processSearchRequests of CourseSearchController:"+System.currentTimeMillis());
        return hits;
    }

    public boolean isCourseOffered(CourseSearchForm form, CourseSearchItem course) throws Exception {
        /*
         *  If the "any" item was chosen in the terms dop-down then continue processing.
         *  Otherwise, determine if the CourseSearchItem should be filtered out of the
         *  result set.
         */
        String term = form.getSearchTerm();

        if (CourseSearchForm.SEARCH_TERM_ANY_ITEM.equals(term)) return true;

        /*
          Use the course offering service to see if the course is being offered in the selected term.
          Note: In the UW implementation of the Course Offering service, course id is actually course code.
        */
        CourseOfferingService service = getCourseOfferingService();

        String subject = course.getSubject();
        List<String> codes = service
                .getCourseOfferingIdsByTermAndSubjectArea(term, subject, CourseSearchConstants.CONTEXT_INFO);

        //  The course code is not in the list, so move on to the next item.
        String code = course.getCode();
        boolean result = codes.contains(code);
        return result;
    }

    //  Load scheduled terms.
    //  Fetch the available terms from the Academic Calendar Service.
    private void loadScheduledTerms(CourseSearchItem course) {
        try {
            logger.info("Start of method loadScheduledTerms of CourseSearchController:"+System.currentTimeMillis());
            AcademicCalendarService atpService = getAcademicCalendarService();

            List<TermInfo> terms = atpService
                    .getCurrentTerms(CourseSearchConstants.PROCESS_KEY, CourseSearchConstants.CONTEXT_INFO);

            CourseOfferingService offeringService = getCourseOfferingService();

            //  If the course is offered in the term then add the term info to the scheduled terms list.
            String code = course.getCode();

            for (TermInfo term : terms) {

                String key = term.getId();
                String subject = course.getSubject();

                List<String> offerings = offeringService
                        .getCourseOfferingIdsByTermAndSubjectArea(key, subject, CourseSearchConstants.CONTEXT_INFO);
                if (offerings.contains(code)) {
                    course.addScheduledTerm(term.getName());
                }
            }
        logger.info("End of method loadScheduledTerms of CourseSearchController:"+System.currentTimeMillis());
        } catch (Exception e) {
            // TODO: Eating this error sucks
            logger.error("Web service call failed.", e);
        }
    }

    private void loadTermsOffered(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadTermsOffered of CourseSearchController:"+System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.atp");
        request.addParam("courseID", courseId);

        List<AtpTypeInfo> termsOffered = new ArrayList<AtpTypeInfo>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String id = getCellValue(row, "atp.id");

            // Don't add the terms that are not found
            AtpTypeInfo atpType = getATPType(id);
            if (null != atpType) {
                termsOffered.add(atpType);
            }
        }

        Collections.sort(termsOffered, getAtpTypeComparator());
        course.setTermInfoList(termsOffered);
        logger.info("End of method loadTermsOffered of CourseSearchController:"+System.currentTimeMillis());
    }

    private void loadGenEduReqs(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadGenEduReqs of CourseSearchController:"+System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.gened");
        request.addParam("courseID", courseId);
        List<String> reqs = new ArrayList<String>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String genEd = getCellValue(row, "gened.name");
            reqs.add(genEd);
        }
        String formatted = formatGenEduReq(reqs);
        course.setGenEduReq(formatted);
        logger.info("End of method loadGenEduReqs of CourseSearchController:"+System.currentTimeMillis());
    }

    private transient AcademicPlanService academicPlanService;

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(AcademicPlanServiceConstants.NAMESPACE,
                            AcademicPlanServiceConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public Person getUser() {
        if (person == null) {
            person = GlobalVariables.getUserSession().getPerson();
        }
        return person;
    }

    private Set<String> getSavedCourseSet() throws Exception {
        logger.info("Start of method getSavedCourseSet of CourseSearchController:"+System.currentTimeMillis());
        AcademicPlanService academicPlanService = getAcademicPlanService();

        Person user = getUser();

        ContextInfo context = new ContextInfo();
        String studentID = user.getPrincipalId();

        String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;

        Set<String> savedCourseSet = new HashSet<String>();

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentID, planTypeKey, context);
        for (LearningPlan learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);

            for (PlanItem planItem : planItemList) {
                String courseID = planItem.getRefObjectId();
                savedCourseSet.add(courseID);
            }
        }
        logger.info("End of method getSavedCourseSet of CourseSearchController:"+System.currentTimeMillis());
        return savedCourseSet;
    }

    private CourseSearchItem getCourseInfo(String courseId) throws MissingParameterException {
        logger.info("Start of method getCourseInfo of CourseSearchController:"+System.currentTimeMillis());
        CourseSearchItem course = new CourseSearchItem();

        SearchRequest request = new SearchRequest("myplan.course.info");
        request.addParam("courseID", courseId);
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String name = getCellValue(row, "course.name");
            String number = getCellValue(row, "course.number");
            String subject = getCellValue(row, "course.subject");
            String level = getCellValue(row, "course.level");
            String creditsID = getCellValue(row, "course.credits");
            String code = getCellValue(row, "course.code");

            course.setCourseId(courseId);
            course.setSubject(subject);
            course.setNumber(number);
            course.setLevel(level);
            course.setCourseName(name);
            course.setCode(code);

            Credit credit = getCreditByID(creditsID);
            course.setCreditMin(credit.min);
            course.setCreditMax(credit.max);
            course.setCreditType(credit.type);
            course.setCredit(credit.display);
            break;
        }
        logger.info("End of method getCourseInfo of CourseSearchController:"+System.currentTimeMillis());
        return course;
    }

    public void populateFacets(CourseSearchForm form, List<CourseSearchItem> courses) {
        logger.info("Start of method populateFacets of CourseSearchController:"+System.currentTimeMillis());
        //  Initialize facets.
        CurriculumFacet curriculumFacet = new CurriculumFacet();
        CreditsFacet creditsFacet = new CreditsFacet();
        CourseLevelFacet courseLevelFacet = new CourseLevelFacet();
        GenEduReqFacet genEduReqFacet = new GenEduReqFacet();
        TermsFacet termsFacet = new TermsFacet();

        //  Update facet info and code the item.
        for (CourseSearchItem course : courses) {
            curriculumFacet.process(course);
            courseLevelFacet.process(course);
            genEduReqFacet.process(course);
            creditsFacet.process(course);
            termsFacet.process(course);
        }

        //  Add the facet data to the response.
        logger.info("Start of populating curriculumFacet  of CourseSearchController:"+System.currentTimeMillis());
        form.setCurriculumFacetItems(curriculumFacet.getFacetItems());
        logger.info("End of populating curriculumFacet  of CourseSearchController:"+System.currentTimeMillis());
        logger.info("Start of populating creditsFacet  of CourseSearchController:"+System.currentTimeMillis());
        form.setCreditsFacetItems(creditsFacet.getFacetItems());
        logger.info("End of populating creditsFacet  of CourseSearchController:"+System.currentTimeMillis());
        logger.info("Start of populating genEduReqFacet  of CourseSearchController:"+System.currentTimeMillis());
        form.setGenEduReqFacetItems(genEduReqFacet.getFacetItems());
        logger.info("End of populating genEduReqFacet  of CourseSearchController:"+System.currentTimeMillis());
        logger.info("Start of populating courseLevelFacet  of CourseSearchController:"+System.currentTimeMillis());
        form.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
        logger.info("End of populating courseLevelFacet  of CourseSearchController:"+System.currentTimeMillis());
        logger.info("Start of populating termsFacet  of CourseSearchController:"+System.currentTimeMillis());
        form.setTermsFacetItems(termsFacet.getFacetItems());
        logger.info("End of populating termsFacet  of CourseSearchController:"+System.currentTimeMillis());
        logger.info("End of method populateFacets of CourseSearchController:"+System.currentTimeMillis());
    }

    public HashMap<String, String> fetchCourseDivisions() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            SearchRequest request = new SearchRequest("myplan.distinct.clu.divisions");

            SearchResult result = getLuService().search(request);

            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    String division = cell.getValue();
                    // Store both trimmed and original, because source data
                    // is sometimes space padded.
                    String key = division.trim().replaceAll("\\s+", "");
                    map.put(key, division);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return map;
    }

    public String extractDivisions(HashMap<String, String> divisionMap, String query, List<String> divisions) {
        boolean match = true;
        while (match) {
            match = false;
            // Retokenize after each division found is removed
            // Remove extra spaces to normalize input
            query = query.trim().replaceAll("\\s+", " ");
            List<QueryTokenizer.Token> tokens = QueryTokenizer.tokenize(query);
            List<String> list = QueryTokenizer.toStringList(tokens);
            List<String> pairs = TokenPairs.toPairs(list);
            TokenPairs.sortedLongestFirst(pairs);

            Iterator<String> i = pairs.iterator();
            while (match == false && i.hasNext()) {
                String pair = i.next();

                String key = pair.replace(" ", "");
                if (divisionMap.containsKey(key)) {
                    String division = divisionMap.get(key);
                    divisions.add(division);
                    query = query.replace(pair, "");
                    match = true;
                }
            }
        }
        return query;
    }


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
            this.courseOfferingService = (CourseOfferingService)
                    GlobalResourceLoader.getService(new QName(CourseOfferingServiceConstants.NAMESPACE, CourseOfferingServiceConstants.SERVICE_NAME_LOCAL_PART));
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

    public CampusSearch getCampusSearch() {
        return campusSearch;
    }

    public void setCampusSearch(CampusSearch campusSearch) {
        this.campusSearch = campusSearch;
    }
}


