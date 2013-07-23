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

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.TermInfoComparator;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.SearchParam;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.EnumerationHelper;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.r2.common.util.constants.CourseOfferingServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;

@Controller
@RequestMapping(value = "/course/**")
public class CourseSearchController extends UifControllerBase {


    public static final String QUERY_TEXT = "queryText";
    public static final String CAMPUS_PARAM = "campusParam";
    public static final String TERM_PARAM = "termParam";
    private final Logger logger = Logger.getLogger(CourseSearchController.class);

    public static final String COURSE_SEARCH_URL = "/student/myplan/course?#searchQuery=%s&searchTerm=any&campusSelect=%s";

    public static final String COURSE_DETAILS_URL = "/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=%s";

    private static final int MAX_HITS = 1000;

    private transient LuService luService;

    private transient AtpService atpService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AcademicPlanService academicPlanService;


    @Autowired
    private TermInfoComparator atpTypeComparator;

    @Autowired
    private CourseSearchStrategy searcher = new CourseSearchStrategy();


    private CourseHelper courseHelper;

    private CampusSearch campusSearch = new CampusSearch();

    //  Java to JSON outputter.
    private transient ObjectMapper mapper = new ObjectMapper();


    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new CourseSearchForm();
    }

    @RequestMapping(value = "/course/{courseCd}", method = RequestMethod.GET)
    public String get(@PathVariable("courseCd") String courseCd, @ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {
        String number = "";
        String subject = "";
        String courseId = "";
        StringBuffer campus = new StringBuffer();
        List<KeyValue> campusKeys = campusSearch.getKeyValues();
        for (KeyValue k : campusKeys) {
            campus.append(k.getKey().toString());
            campus.append(",");
        }
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(courseCd);
        if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
            number = courseCode.getNumber();
            subject = courseCode.getSubject();
        } else {
            response.sendRedirect(String.format(COURSE_SEARCH_URL, courseCd, campus));
            return null;

        }
        HashMap<String, String> divisionMap = getCourseHelper().fetchCourseDivisions();

        ArrayList<String> divisions = new ArrayList<String>();
        getCourseHelper().extractDivisions(divisionMap, subject, divisions, false);
        if (divisions.size() > 0) {
            subject = divisions.get(0);
        }
        courseId = getCourseHelper().getCourseId(subject.trim(), number);
        if (!StringUtils.hasText(courseId)) {
            response.sendRedirect(String.format(COURSE_SEARCH_URL, courseCd, campus));
            return null;

        }
        response.sendRedirect(String.format(COURSE_DETAILS_URL, courseId));
        return null;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                            HttpServletRequest request, HttpServletResponse response) {

        super.start(form, result, request, response);
        form.setViewId(CourseSearchConstants.COURSE_SEARCH_FORM_VIEW);
        form.setView(super.getViewService().getViewById(CourseSearchConstants.COURSE_SEARCH_FORM_VIEW));

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

    public HashMap<String, Credit> getCreditMap() {
        HashMap<String, Credit> creditMap = new HashMap<String, Credit>();

        try {
            SearchRequest searchRequest = new SearchRequest(CourseSearchConstants.SEARCH_REQUEST_CREDITS_DETAILS);
            List<SearchParam> params = new ArrayList<SearchParam>();
            searchRequest.setParams(params);
            SearchResult searchResult = getLuService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = OrgHelper.getCellValue(row, "credit.id");
                String type = OrgHelper.getCellValue(row, "credit.type");
                String min = OrgHelper.getCellValue(row, "credit.min");
                String max = OrgHelper.getCellValue(row, "credit.max");
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

    public List<CourseSearchItem> courseSearch(CourseSearchForm form, String studentId) {

        String maxCountProp = ConfigContext.getCurrentContextConfig().getProperty(CourseSearchConstants.MYPLAN_SEARCH_RESULTS_MAX);
        int maxCount = (StringUtils.hasText(maxCountProp)) ? Integer.valueOf(maxCountProp) : MAX_HITS;
        try {
            List<SearchRequest> requests = searcher.queryToRequests(form);
            List<Hit> hits = processSearchRequests(requests);
            List<CourseSearchItem> courseList = new ArrayList<CourseSearchItem>();
            Map<String, CourseSearchItem.PlanState> courseStatusMap = getCourseStatusMap(studentId);

            Set<String> subjectArea = new HashSet<String>();

            for (Hit hit : hits) {
                CourseSearchItem course = getCourseInfo(hit.courseID);
                //       loadScheduledTerms(course);
                subjectArea.add(course.getSubject());
                loadTermsOffered(course);
                loadGenEduReqs(course);
                if (courseStatusMap.containsKey(course.getCourseVersionIndependentId())) {
                    course.setStatus(courseStatusMap.get(course.getCourseVersionIndependentId()));
                }
                courseList.add(course);
                if (courseList.size() >= maxCount) {
                    break;
                }
            }

            loadScheduledTerms(courseList, subjectArea);
            if (!CourseSearchForm.SEARCH_TERM_ANY_ITEM.equals(form.getSearchTerm())) {
                List<CourseSearchItem> filteredCourses = new ArrayList<CourseSearchItem>();
                filteredCourses = filterCoursesByTerm(courseList, form.getSearchTerm());
                populateFacets(form, filteredCourses);
                logger.error(String.format("SEARCH: %s  : %s CAMPUS : %s : %s", form.getSearchQuery(), form.getSearchTerm(), form.getCampusSelect(), String.valueOf(hits.size())));
                return filteredCourses;
            }

            populateFacets(form, courseList);
            logger.error(String.format("SEARCH: %s  : %s CAMPUS : %s : %s", form.getSearchQuery(), form.getSearchTerm(), form.getCampusSelect(), String.valueOf(hits.size())));
            return courseList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @RequestMapping(value = "/course/search")
    public void getJsonResponse(HttpServletResponse response, HttpServletRequest request) {
        /*Params from the Url*/
        String queryText = request.getParameter(QUERY_TEXT);
        queryText = queryText.replace("%20", " ");

        String campusParamStr = request.getParameter(CAMPUS_PARAM);
        List<String> campusParams = Arrays.asList(campusParamStr.split(CourseSearchConstants.CAMPUS_PARAM_REGEX));
        String termParam = request.getParameter(TERM_PARAM);

        /*populating the form with the params*/
        CourseSearchForm form = new CourseSearchForm();
        form.setSearchQuery(queryText);
        form.setCampusSelect(campusParams);
        form.setSearchTerm(termParam);


        /*populating the CourseSearchItem list*/
        String user = UserSessionHelper.getStudentRegId();
        List<CourseSearchItem> courses = courseSearch(form, user);

        /*Building the Json String*/
        StringBuilder jsonString = new StringBuilder();
        jsonString = jsonString.append("{ \"aaData\":[");
        boolean first = true;

        for (CourseSearchItem item : courses) {
            String scheduledAndOfferedTerms = null;
            try {
                scheduledAndOfferedTerms = mapper.writeValueAsString(item.getScheduledAndOfferedTerms());
            } catch (IOException e) {
                throw new RuntimeException("Could not write the value using mapper", e);
            }

            String status = "";
            String courseId = item.getCourseId();
            String label = item.getStatus().getLabel();
            if (label.length() > 0) {
                status = "<span id=\\\"" + courseId + "_status\\\" class=\\\"" + label.toLowerCase() + "\\\">" + label + "</span>";
            } else if (UserSessionHelper.isAdviser()) {
                status = "<span id=\\\"" + courseId + "_status\\\">" + CourseSearchItem.EMPTY_RESULT_VALUE_KEY + "</span>";
            } else {
                status = "<span id=\\\"" + courseId + "_status\\\"><input type=\\\"image\\\" title=\\\"Bookmark or Add to Plan\\\" src=\\\"/student/ks-myplan/images/pixel.gif\\\" alt=\\\"Bookmark or Add to Plan\\\" class=\\\"uif-field uif-imageField myplan-add\\\" data-courseid= \\\"" + courseId + "\\\"onclick=\\\"openMenu('" + courseId + "_add','add_course_items',null,event,null,'myplan-container-75',{tail:{align:'middle'},align:'middle',position:'right'},false);\\\" /></span>";
            }

            String courseName = "";
            if (item.getCourseName() != null) {
                courseName = item.getCourseName().replace('\"', '\'');
            }

            if (first) {
                first = false;
            } else {
                jsonString.append(", ");
            }

            jsonString.append("[\"").append(item.getCode()).
                    append("\",\" <a href=\\\"inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=").
                    append(courseId).append("\\\" target=\\\"_self\\\" title=\\\"").append(courseName).append("\\\" class=\\\"myplan-text-ellipsis\\\">").
                    append(courseName).append("</a>\",\"").
                    append(item.getCredit()).append("\",").append(scheduledAndOfferedTerms).append(",\"").
                    append(item.getGenEduReq()).append("\",\"").append(status).
                    append("\",\"").append(item.getTermsFacetKeys()).
                    append("\",\"").append(item.getGenEduReqFacetKeys()).
                    append("\",\"").append(item.getCreditsFacetKeys()).
                    append("\",\"").append(item.getCourseLevelFacetKeys()).
                    append("\",\"").append(item.getCurriculumFacetKeys()).
                    append("\"]");
        }
        jsonString.append("]}");
        try {
            response.setHeader("content-type", "application/json");
            response.setHeader("Cache-Control", "No-cache");
            response.setHeader("Cache-Control", "No-store");
            response.setHeader("Cache-Control", "max-age=0");
            response.getWriter().println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @RequestMapping(params = "methodToCall=searchForCourses")
    public ModelAndView searchForCourses(@ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        return getUIFModelAndView(form, CourseSearchConstants.COURSE_SEARCH_RESULT_PAGE);
    }

    public List<String> getResults(SearchRequest request, String division, String code) {
        try {
            List<String> results = new ArrayList<String>();
            if (division == null && code == null) {
                return results;
            }
            if (code != null) {
                request.addParam("code", code);

            }
            if (division != null) {
                request.addParam("division", division);
            }

            request.addParam("lastScheduledTerm", AtpHelper.getLastScheduledAtpId());

            SearchResult searchResult = getLuService().search(request);
            if (searchResult != null) {
                for (SearchResultRow row : searchResult.getRows()) {
                    results.add(OrgHelper.getCellValue(row, "courseCode"));
                }
            }

            return results;
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
        logger.info("Start of processSearchRequests of CourseSearchController:" + System.currentTimeMillis());

        ArrayList<Hit> hits = new ArrayList<Hit>();
        ArrayList<Hit> tempHits = new ArrayList<Hit>();
        for (SearchRequest request : requests) {
            SearchResult searchResult = getLuService().search(request);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = OrgHelper.getCellValue(row, "lu.resultColumn.cluId");
                /* hitCourseID(courseMap, id);*/
                Hit hit = new Hit(id);
                tempHits.add(hit);

            }
            tempHits.removeAll(hits);
            hits.addAll(tempHits);
            tempHits.clear();
        }

        logger.info("End of processSearchRequests of CourseSearchController:" + System.currentTimeMillis());
        return hits;
    }

    public List<CourseSearchItem> filterCoursesByTerm(List<CourseSearchItem> courses, String term) throws Exception {
        List<CourseSearchItem> filteredCourses = new ArrayList<CourseSearchItem>();
        AtpHelper.YearTerm yearTerm = AtpHelper.atpToYearTerm(term);
        for (CourseSearchItem item : courses) {
            List<String> scheduledTerms = item.getScheduledTermsList();
            if (scheduledTerms != null && scheduledTerms.contains(yearTerm.toLabel())) {
                filteredCourses.add(item);
            }
        }
        return filteredCourses;
    }
    //  Load scheduled terms.
    //  Fetch the available terms from the Academic Calendar Service.

    private void loadScheduledTerms(List<CourseSearchItem> courses, Set<String> subjectSet) {
        try {

            logger.info("Start of method loadScheduledTerms of CourseSearchController:" + System.currentTimeMillis());
            AcademicCalendarService atpService = getAcademicCalendarService();

            List<TermInfo> terms = atpService.searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED)), CourseSearchConstants.CONTEXT_INFO);

            CourseOfferingService offeringService = getCourseOfferingService();

            // For each term load all course offerings by subjectArea
            for (TermInfo term : terms) {
                Set<String> courseOfferingByTermSet = new HashSet<String>();

                for (String subjectArea : subjectSet) {
                    List<String> offeringIds = getCourseOfferingService().getCourseOfferingIdsByTermAndSubjectArea(term.getId(), subjectArea, CourseSearchConstants.CONTEXT_INFO);
                    courseOfferingByTermSet.addAll(offeringIds);
                }

                // Check to see if the course is offered
                for (CourseSearchItem item : courses) {
                    if (getCourseHelper().isCourseInOfferingIds(item.getSubject(), item.getNumber(), courseOfferingByTermSet)) {
                        item.addScheduledTerm(term.getName());
                    }
                }

            }
            logger.info("End of method loadScheduledTerms of CourseSearchController:" + System.currentTimeMillis());
        } catch (Exception e) {
            // TODO: Eating this error sucks
            logger.error("Web service call failed.", e);
        }
    }

    private void loadTermsOffered(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadTermsOffered of CourseSearchController:" + System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.atp");
        request.addParam("courseID", courseId);

        List<AtpTypeInfo> termsOffered = new ArrayList<AtpTypeInfo>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String id = OrgHelper.getCellValue(row, "atp.id");

            // Don't add the terms that are not found
            AtpTypeInfo atpType = getATPType(id);
            if (null != atpType) {
                termsOffered.add(atpType);
            }
        }

        Collections.sort(termsOffered, getAtpTypeComparator());
        course.setTermInfoList(termsOffered);
        logger.info("End of method loadTermsOffered of CourseSearchController:" + System.currentTimeMillis());
    }

    private void loadGenEduReqs(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadGenEduReqs of CourseSearchController:" + System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequest request = new SearchRequest("myplan.course.info.gened");
        request.addParam("courseID", courseId);
        List<String> reqs = new ArrayList<String>();
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String genEd = OrgHelper.getCellValue(row, "gened.name");
            reqs.add(genEd);
        }
        String formatted = formatGenEduReq(reqs);
        course.setGenEduReq(formatted);
        logger.info("End of method loadGenEduReqs of CourseSearchController:" + System.currentTimeMillis());
    }

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

    private Map<String, CourseSearchItem.PlanState> getCourseStatusMap(String studentID) throws Exception {
        logger.info("Start of method getCourseStatusMap of CourseSearchController:" + System.currentTimeMillis());
        AcademicPlanService academicPlanService = getAcademicPlanService();


        ContextInfo context = new ContextInfo();


        String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;

        Map<String, CourseSearchItem.PlanState> savedCourseSet = new HashMap<String, CourseSearchItem.PlanState>();

        /*
         *  For each plan item in each plan set the state based on the type.
         */
        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentID, planTypeKey, context);
        for (LearningPlan learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);
            for (PlanItem planItem : planItemList) {
                String courseID = planItem.getRefObjectId();
                CourseSearchItem.PlanState state;
                if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                    state = CourseSearchItem.PlanState.SAVED;
                } else if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)
                        || planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
                    state = CourseSearchItem.PlanState.IN_PLAN;
                } else {
                    throw new RuntimeException("Unknown plan item type.");
                }
                savedCourseSet.put(courseID, state);
            }
        }
        logger.info("End of method getCourseStatusMap of CourseSearchController:" + System.currentTimeMillis());
        return savedCourseSet;
    }

    private CourseSearchItem getCourseInfo(String courseId) throws MissingParameterException {
        logger.info("Start of method getCourseInfo of CourseSearchController:" + System.currentTimeMillis());
        CourseSearchItem course = new CourseSearchItem();

        SearchRequest request = new SearchRequest("myplan.course.info");
        request.addParam("courseID", courseId);
        SearchResult result = getLuService().search(request);
        for (SearchResultRow row : result.getRows()) {
            String name = OrgHelper.getCellValue(row, "course.name");
            String number = OrgHelper.getCellValue(row, "course.number");
            String subject = OrgHelper.getCellValue(row, "course.subject");
            String level = OrgHelper.getCellValue(row, "course.level");
            String creditsID = OrgHelper.getCellValue(row, "course.credits");
            String code = OrgHelper.getCellValue(row, "course.code");
            String versionIndId = OrgHelper.getCellValue(row, "course.verIndId");
            course.setCourseVersionIndependentId(versionIndId);

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
        logger.info("End of method getCourseInfo of CourseSearchController:" + System.currentTimeMillis());
        return course;
    }

    public void populateFacets(CourseSearchForm form, List<CourseSearchItem> courses) {
        logger.info("Start of method populateFacets of CourseSearchController:" + System.currentTimeMillis());
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
        /*Removing Duplicate entries from genEduReqFacet*/
        List<FacetItem> genEduReqFacetItems = new ArrayList<FacetItem>();
        for (FacetItem facetItem : genEduReqFacet.getFacetItems()) {
            boolean itemExists = false;
            for (FacetItem facetItem1 : genEduReqFacetItems) {
                if (facetItem1.getKey().equalsIgnoreCase(facetItem.getKey())) {
                    itemExists = true;
                }
            }
            if (!itemExists) {
                genEduReqFacetItems.add(facetItem);
            }
        }


        /*//  Add the facet data to the response.
 logger.info("Start of populating curriculumFacet  of CourseSearchController:" + System.currentTimeMillis());
 form.setCurriculumFacetItems(curriculumFacet.getFacetItems());
 logger.info("End of populating curriculumFacet  of CourseSearchController:" + System.currentTimeMillis());
 logger.info("Start of populating creditsFacet  of CourseSearchController:" + System.currentTimeMillis());
 form.setCreditsFacetItems(creditsFacet.getFacetItems());
 logger.info("End of populating creditsFacet  of CourseSearchController:" + System.currentTimeMillis());
 logger.info("Start of populating genEduReqFacet  of CourseSearchController:" + System.currentTimeMillis());
 form.setGenEduReqFacetItems(genEduReqFacetItems);
 logger.info("End of populating genEduReqFacet  of CourseSearchController:" + System.currentTimeMillis());
 logger.info("Start of populating courseLevelFacet  of CourseSearchController:" + System.currentTimeMillis());
 form.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
 logger.info("End of populating courseLevelFacet  of CourseSearchController:" + System.currentTimeMillis());
 logger.info("Start of populating termsFacet  of CourseSearchController:" + System.currentTimeMillis());
 form.setTermsFacetItems(termsFacet.getFacetItems());
 logger.info("End of populating termsFacet  of CourseSearchController:" + System.currentTimeMillis());
 logger.info("End of method populateFacets of CourseSearchController:" + System.currentTimeMillis());*/
    }


    private String formatGenEduReq(List<String> genEduRequirements) {

        //  Make the order predictable.
        Collections.sort(genEduRequirements);
        StringBuilder genEdsOut = new StringBuilder();
        for (String req : genEduRequirements) {
            if (genEdsOut.length() != 0) {
                genEdsOut.append(", ");
            }
            req = EnumerationHelper.getEnumAbbrValForCodeByType(req, PlanConstants.GEN_EDU_ENUM_KEY);
            /*Doing this to fix a bug in IE8 which is trimming off the I&S as I*/
            if (req.contains("&")) {
                req = req.replace("&", "&amp;");
            }
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

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
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


