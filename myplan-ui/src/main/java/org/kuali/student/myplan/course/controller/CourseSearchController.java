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

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.rice.krad.web.form.UifFormManager;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.EnumerationHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.plan.util.SearchHelper;
import org.kuali.student.myplan.utils.GlobalConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.r2.core.atp.service.AtpService;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.class1.type.service.TypeService;
import org.kuali.student.r2.core.constants.TypeServiceConstants;
import org.kuali.student.r2.core.search.dto.SearchParamInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.core.search.service.SearchService;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;

@Controller
@RequestMapping(value = "/course/**")
public class CourseSearchController extends UifControllerBase {


    public static final String QUERY_TEXT = "searchQuery";
    public static final String CAMPUS_PARAM = "campusSelect";
    public static final String TERM_PARAM = "searchTerm";
    public static final String START_TIME_PARAM = "startTime";
    public static final String END_TIME_PARAM = "endTime";
    public static final String SELECTED_DAYS_PARAM = "selectedDays";
    public static final String FORM_KEY_PARAM = "formKey";
    public static final String MEETING_FACETS_PARAM = "meetingFacets";
    public static final String MEETING_DAYS = "meetingDays";
    public static final String MEETING_TIMES = "meetingTimes";
    public static final String MEETING_DAY_TIMES = "meetingDayTimes";
    public static final String MEETING_DAY_TIMES_TBA = "tba";
    private final Logger logger = Logger.getLogger(CourseSearchController.class);

    public static final String COURSE_SEARCH_URL = "/" + GlobalConstants.MYPLAN_APP_CODE +
            "/myplan/course?#searchQuery=%s&searchTerm=any&campusSelect=%s";

    private static final int MAX_HITS = 1000;

    private transient CluService luService;

    private transient AtpService atpService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AcademicPlanService academicPlanService;

    private transient TypeService typeService;

    private transient SearchService courseOfferingSearchService;

    private Comparator<TypeInfo> atpTypeComparator;


    private CourseSearchStrategy searcher = new CourseSearchStrategy();


    private UserSessionHelper userSessionHelper;


    private PlanHelper planHelper;


    private CourseHelper courseHelper;

    private CampusSearch campusSearch = new CampusSearch();

    //  Java to JSON outPutter.
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
        String url = String.format(CourseSearchConstants.COURSE_DETAILS_URL, courseId, urlEscape(String.format("%s %s", subject.trim(), number)));
        response.sendRedirect(url);
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
            SearchRequestInfo searchRequest = new SearchRequestInfo(CourseSearchConstants.SEARCH_REQUEST_CREDITS_DETAILS);
            List<SearchParamInfo> params = new ArrayList<SearchParamInfo>();
            searchRequest.setParams(params);
            SearchResultInfo searchResult = getLuService().search(searchRequest, CourseSearchConstants.CONTEXT_INFO);
            for (SearchResultRow row : searchResult.getRows()) {
                String id = SearchHelper.getCellValue(row, "credit.id");
                String type = SearchHelper.getCellValue(row, "credit.type");
                String min = SearchHelper.getCellValue(row, "credit.min");
                String max = SearchHelper.getCellValue(row, "credit.max");
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
            List<SearchRequestInfo> requests = searcher.queryToRequests(form);
            Set<String> divisionsQueried = getDivisionsFromSearchRequests(requests);
            Set<String> numbersQueried = getNumbersFromSearchRequests(requests);
            List<Hit> hits = processSearchRequests(requests);
            List<CourseSearchItem> courseList = new ArrayList<CourseSearchItem>();
            Map<String, CourseSearchItem.PlanState> courseStatusMap = getCourseStatusMap(studentId);

            Set<String> subjectArea = new HashSet<String>();

            for (Hit hit : hits) {
                List<CourseSearchItem> courseSearchItems = getCourseInfo(hit.courseID);
                for (CourseSearchItem course : courseSearchItems) {

                    if ((divisionsQueried.size() == 1 && !divisionsQueried.contains(course.getSubject())) || (numbersQueried.size() == 1 && !numbersQueried.contains(course.getNumber()))) {
                        continue;
                    }
                    //       loadScheduledTerms(course);
                    subjectArea.add(course.getSubject());
                    loadTermsOffered(course);
                    loadGenEduReqs(course);
                    String key = String.format("%s:%s:%s", course.getCourseVersionIndependentId(), course.getSubject().trim(), course.getNumber().trim());
                    if (courseStatusMap.containsKey(key)) {

                        course.setStatus(courseStatusMap.get(key));
                    }
                    courseList.add(course);
                }
                if (courseList.size() >= maxCount) {
                    break;
                }
            }

            loadScheduledTerms(courseList, subjectArea, form);
            if (!CourseSearchConstants.SEARCH_TERM_ANY_ITEM.equals(form.getSearchTerm())) {
                Collections.sort(courseList, new Comparator<CourseSearchItem>() {
                    @Override
                    public int compare(CourseSearchItem p1, CourseSearchItem p2) {
                        boolean v1 = p1.isSortToTop();
                        boolean v2 = p2.isSortToTop();
                        return v1 == v2 ? 0 : (v1 ? -1 : 1);
                    }
                });
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

    @RequestMapping(value = "/course/updateFacets")
    public void updateFacets(HttpServletResponse response, HttpServletRequest request) {
        String formKey = request.getParameter(FORM_KEY_PARAM);
        response.setHeader("content-type", "application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");

        HashMap<String, Object> meetingResults = new HashMap<String, Object>();
        String meetings = request.getParameter(MEETING_FACETS_PARAM);

        CourseSearchForm searchForm = null;
        if (StringUtils.hasText(formKey)) {
            try {
                meetingResults = new ObjectMapper().readValue(meetings, HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            searchForm = (CourseSearchForm) ((UifFormManager) request.getSession().getAttribute("formManager")).getSessionForm(formKey);
            Map<String, List<String>> meetingfacets = searchForm.getMeetingFacets();
            for (String meeting : meetingResults.keySet()) {
                List<String> values = new ArrayList<String>();
                if (StringUtils.hasText((String) meetingResults.get(meeting))) {
                    values = Arrays.asList(((String) meetingResults.get(meeting)).split(","));

                }
                meetingfacets.put(meeting, values);
            }
            searchForm.setMeetingFacets(meetingfacets);
        }


        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonString = Json.createGenerator(stringWriter);
        jsonString.writeStartObject();
        try {
            jsonString.write("STATUS", "SUCCESS");
            jsonString.writeEnd().flush();
            response.getWriter().println(stringWriter.toString());
        } catch (Exception e) {
            logger.error("Could not write the response", e);
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
        String formKey = request.getParameter(FORM_KEY_PARAM);
        String selectedDays = request.getParameter(SELECTED_DAYS_PARAM);
        String startTime = request.getParameter(START_TIME_PARAM);
        String endTime = request.getParameter(END_TIME_PARAM);
        CourseSearchForm form = new CourseSearchForm();
        if (StringUtils.hasText(formKey)) {
            form = (CourseSearchForm) request.getAttribute(UifConstants.REQUEST_FORM);
            form.setMeetingFacets(new HashMap<String, List<String>>());
            form.setSelectedDays(StringUtils.hasText(selectedDays) ? Arrays.asList(selectedDays.split(",")) : new ArrayList<String>());
            form.setStartTime(startTime);
            form.setEndTime(endTime);
        }

        /*populating the form with the params*/
        form.setSearchQuery(queryText);
        form.setCampusSelect(campusParams);
        form.setSearchTerm(termParam);

        form.setView(form.getPostedView());

        /*populating the CourseSearchItem list*/
        String user = getUserSessionHelper().getStudentId();
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
            String domId = item.getSubject().trim().replaceAll("[\\&|\\s]", "") + item.getNumber().trim() + "-" + courseId;

            String courseName = "";
            if (item.getCourseName() != null) {
                courseName = item.getCourseName().replace('\"', '\'');
            }

            if (label.length() > 0) {
                status = String.format("<span id=\\\"%s_status\\\" class=\\\"%s\\\">%s</span>", domId, label.toLowerCase(), label);
            } else if (getUserSessionHelper().isAdviser()) {
                status = String.format("<span id=\\\"%s_status\\\">%s</span>", domId, CourseSearchItem.EMPTY_RESULT_VALUE_KEY);
            } else {
                status = String.format("<span id=\\\"%s_status\\\">" +
                                "<input type=\\\"image\\\" title=\\\"Bookmark or Add to Plan %s %s\\\" src=\\\"../themes/ksap/images/pixel.gif\\\" " +
                                "alt=\\\"Bookmark or Add to Plan %s %s\\\" class=\\\"courseResults__itemAdd\\\" data-courseid= \\\"%s\\\" " +
                                "data-coursecd= \\\"%s\\\" data-subject= \\\"%s\\\" data-number= \\\"%s\\\" " +
                                "onclick=\\\"openMenu('%s_add','add_course_items',null,event,null,'popover__menu popover__menu--small',{tail:{align:'middle'},align:'middle',position:'right'},false);\\\" /></span>",
                        domId, item.getCode().trim(), courseName, item.getCode().trim(), courseName, courseId, item.getCode().trim(), item.getSubject().trim(), item.getNumber().trim(), courseId);
            }

            if (first) {
                first = false;
            } else {
                jsonString.append(", ");
            }

            jsonString.append("[\"").append(item.getCode()).
                    append("\",\" <a href=\\\"inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=").
                    append(courseId + "&courseCd=" + item.getCode().replace("&", "%26")).append("&" + PlanConstants.PARAM_SEARCH_FORM_KEY + "=" + formKey).append("&" + PlanConstants.PARAM_FILTER_ACTIVITIES + "=" + !CourseSearchConstants.SEARCH_TERM_ANY_ITEM.equals(termParam)).append("\\\" target=\\\"_self\\\" title=\\\"").append(courseName).append("\\\" class=\\\"ellipsisItem\\\">").
                    append(courseName).append("</a>\",\"").
                    append(item.getCredit()).append("\",").append(scheduledAndOfferedTerms).append(",\"").
                    append(item.getGenEduReq()).append("\",\"").append(status).
                    append("\",\"").append(item.getTermsFacetKeys()).
                    append("\",\"").append(item.getGenEduReqFacetKeys()).
                    append("\",\"").append(item.getCreditsFacetKeys()).
                    append("\",\"").append(item.getCourseLevelFacetKeys()).
                    append("\",\"").append(item.getCurriculumFacetKeys()).
                    append("\",\"").append(item.getMeetingDayFacetKeys()).
                    append("\",\"").append(item.getMeetingTimeFacetKeys()).
                    append("\",\"").append(item.getMeetingDayTimeFacetKeys()).
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

    public List<String> getResults(SearchRequestInfo request, String division, String code) {
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

            SearchResultInfo searchResult = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
            if (searchResult != null) {
                for (SearchResultRow row : searchResult.getRows()) {
                    results.add(SearchHelper.getCellValue(row, "courseCode"));
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

    /**
     * Divisions that are added as params in the search request are passed as a set for crossListing course verification
     *
     * @param requestInfos
     * @return
     */
    private Set<String> getDivisionsFromSearchRequests(List<SearchRequestInfo> requestInfos) {
        Set<String> divisions = new HashSet<String>();
        for (SearchRequestInfo searchRequestInfo : requestInfos) {
            if ("myplan.lu.search.title".equals(searchRequestInfo.getSearchKey()) || "myplan.lu.search.description".equals(searchRequestInfo.getSearchKey()) || "myplan.lu.search.fulltext".equals(searchRequestInfo.getSearchKey())) {
                return new HashSet<String>();
            }
            for (SearchParamInfo requestParam : searchRequestInfo.getParams()) {
                if ("division".equals(requestParam.getKey())) {
                    divisions.add(requestParam.getValues().get(0).trim());
                }
            }
        }
        return divisions;
    }

    /**
     * Numbers that are added as params in the search request are passed as a set for crossListing course verification
     *
     * @param requestInfos
     * @return
     */
    private Set<String> getNumbersFromSearchRequests(List<SearchRequestInfo> requestInfos) {
        Set<String> numbers = new HashSet<String>();
        for (SearchRequestInfo searchRequestInfo : requestInfos) {
            if ("myplan.lu.search.title".equals(searchRequestInfo.getSearchKey()) || "myplan.lu.search.description".equals(searchRequestInfo.getSearchKey()) || "myplan.lu.search.fulltext".equals(searchRequestInfo.getSearchKey())) {
                return new HashSet<String>();
            }
            for (SearchParamInfo requestParam : searchRequestInfo.getParams()) {
                if ("code".equals(requestParam.getKey())) {
                    numbers.add(requestParam.getValues().get(0).trim());
                }
            }
        }
        return numbers;
    }


    /**
     * Gets the CluIds from the given search requests
     *
     * @param requests
     * @return
     * @throws MissingParameterException
     */
    public ArrayList<Hit> processSearchRequests(List<SearchRequestInfo> requests) throws MissingParameterException {
        logger.info("Start of processSearchRequests of CourseSearchController:" + System.currentTimeMillis());

        ArrayList<Hit> hits = new ArrayList<Hit>();
        ArrayList<Hit> tempHits = new ArrayList<Hit>();
        List<String> courseIds = new ArrayList<String>();
        for (SearchRequestInfo request : requests) {
            SearchResultInfo searchResult = null;
            try {
                searchResult = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
            } catch (InvalidParameterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (OperationFailedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (PermissionDeniedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            for (SearchResultRow row : searchResult.getRows()) {
                String id = SearchHelper.getCellValue(row, "lu.resultColumn.cluId");
                if (!courseIds.contains(id)) {
                /* hitCourseID(courseMap, id);*/
                    courseIds.add(id);
                    Hit hit = new Hit(id);
                    tempHits.add(hit);

                }

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

    public class ActivitySearchItem {

        private Map<String, Set<String>> primaryToSecondaries;
        private Map<String, Set<String>> primaryToSecondaryTypes;
        private Map<String, Set<String>> primaryToSecondaryTypesAvailable;
        private Set<String> meetingTimes;

        public Set<String> getMeetingTimes() {
            if (meetingTimes == null) {
                meetingTimes = new LinkedHashSet<String>();
            }
            return meetingTimes;
        }

        public void setMeetingTimes(Set<String> meetingTimes) {
            this.meetingTimes = meetingTimes;
        }

        public Map<String, Set<String>> getPrimaryToSecondaries() {
            if (primaryToSecondaries == null) {
                primaryToSecondaries = new HashMap<String, Set<String>>();
            }
            return primaryToSecondaries;
        }

        public void setPrimaryToSecondaries(Map<String, Set<String>> primaryToSecondaries) {
            this.primaryToSecondaries = primaryToSecondaries;
        }

        public Map<String, Set<String>> getPrimaryToSecondaryTypes() {
            if (primaryToSecondaryTypes == null) {
                primaryToSecondaryTypes = new HashMap<String, Set<String>>();
            }
            return primaryToSecondaryTypes;
        }

        public void setPrimaryToSecondaryTypes(Map<String, Set<String>> primaryToSecondaryTypes) {
            this.primaryToSecondaryTypes = primaryToSecondaryTypes;
        }

        public Map<String, Set<String>> getPrimaryToSecondaryTypesAvailable() {
            if (primaryToSecondaryTypesAvailable == null) {
                primaryToSecondaryTypesAvailable = new HashMap<String, Set<String>>();
            }
            return primaryToSecondaryTypesAvailable;
        }

        public void setPrimaryToSecondaryTypesAvailable(Map<String, Set<String>> primaryToSecondaryTypesAvailable) {
            this.primaryToSecondaryTypesAvailable = primaryToSecondaryTypesAvailable;
        }
    }

    //  Load scheduled terms.
    //  Fetch the available terms from the Academic Calendar Service.

    private void loadScheduledTerms(List<CourseSearchItem> courses, Set<String> subjectSet, CourseSearchForm form) {
        try {

            logger.info("Start of method loadScheduledTerms of CourseSearchController:" + System.currentTimeMillis());
            AcademicCalendarService atpService = getAcademicCalendarService();

            List<TermInfo> terms = atpService.searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED)), CourseSearchConstants.CONTEXT_INFO);

            // For each term load all course offerings by subjectArea
            for (TermInfo term : terms) {
                Set<String> courseOfferingByTermSet = new HashSet<String>();
                Map<String, ActivitySearchItem> coursesToActivitySearchItem = new LinkedHashMap<String, ActivitySearchItem>();

                /*If the search term is ANY then we do regular search for course offeringIds otherwise we do a section search based on meeting times and days passed in the form.*/
                if (CourseSearchConstants.SEARCH_TERM_ANY_ITEM.equals(form.getSearchTerm()) || !term.getId().equals(form.getSearchTerm())) {
                    for (String subjectArea : subjectSet) {
                        List<String> offeringIds = getCourseOfferingService().getCourseOfferingIdsByTermAndSubjectArea(term.getId(), subjectArea, CourseSearchConstants.CONTEXT_INFO);
                        courseOfferingByTermSet.addAll(offeringIds);
                    }
                } else {

                    AtpHelper.YearTerm yt = AtpHelper.atpToYearTerm(form.getSearchTerm());
                    for (String subjectArea : subjectSet) {

                        SearchRequestInfo request = new SearchRequestInfo(CourseSearchConstants.COURSE_OFFERING_MEETING_TIME_SEARCH);
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_DAYS_PARAM, org.apache.commons.lang.StringUtils.join(form.getSelectedDays(), ":"));
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_START_TIME_PARAM, form.getStartTime());
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_END_TIME_PARAM, form.getEndTime());
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_TERM_PARAM, yt.getTermAsID());
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_YEAR_PARAM, yt.getYearAsString());
                        request.addParam(CourseSearchConstants.COURSE_OFFERING_SEARCH_SUBJECT_PARAM, subjectArea.trim());

                        SearchResultInfo result = null;
                        try {
                            result = getCourseOfferingSearchService().search(request, CourseSearchConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could not retrieve course offering by meeting times for subject: " + subjectArea + " for term: " + form.getSearchTerm(), e);
                        }

                        for (SearchResultRow row : result.getRows()) {
                            String id = SearchHelper.getCellValue(row, "section.id");
                            String meetingTimes = SearchHelper.getCellValue(row, "section.meeting.days.and.times");
                            String subject = SearchHelper.getCellValue(row, "section.curriculum.abbreviation");
                            String number = SearchHelper.getCellValue(row, "section.course.number");
                            boolean primary = Boolean.valueOf(SearchHelper.getCellValue(row, "section.primary"));
                            int secondaryCount = Integer.parseInt(SearchHelper.getCellValue(row, "section.secondary.count"));
                            String primaryId = SearchHelper.getCellValue(row, "section.primary.id");
                            String secondaryActivityTypes = SearchHelper.getCellValue(row, "section.secondary.types");
                            String activityType = SearchHelper.getCellValue(row, "section.type");
                            String code = getCourseHelper().joinStringsByDelimiter(' ', subject.trim(), number.trim());
                            ActivitySearchItem activitySearchItem = coursesToActivitySearchItem.get(code) != null ? coursesToActivitySearchItem.get(code) : new ActivitySearchItem();
                            if (primary) {
                                courseOfferingByTermSet.add(id);
                                activitySearchItem.getPrimaryToSecondaries().put(id, secondaryCount > 0 ? new LinkedHashSet<String>() : null);
                                if (secondaryCount > 0) {
                                    activitySearchItem.getPrimaryToSecondaryTypes().put(id, StringUtils.commaDelimitedListToSet(secondaryActivityTypes));
                                }
                            } else {
                                if (activitySearchItem.getPrimaryToSecondaries().get(primaryId) == null) {
                                    continue;
                                } else {
                                    activitySearchItem.getPrimaryToSecondaries().get(primaryId).add(id);
                                    Set<String> secondaryTypes = activitySearchItem.getPrimaryToSecondaryTypesAvailable().get(primaryId);
                                    if (CollectionUtils.isEmpty(secondaryTypes)) {
                                        secondaryTypes = new HashSet<String>();
                                    }
                                    secondaryTypes.add(activityType);
                                    activitySearchItem.getPrimaryToSecondaryTypesAvailable().put(primaryId, secondaryTypes);
                                }
                            }
                            activitySearchItem.getMeetingTimes().add(meetingTimes);
                            coursesToActivitySearchItem.put(code, activitySearchItem);
                        }

                    }
                }

                /*Filtering out the activities from courseOfferingByTermSet which do not have a complete course activity set.*/
                for (String key : coursesToActivitySearchItem.keySet()) {
                    ActivitySearchItem activitySearchItem = coursesToActivitySearchItem.get(key);
                    if (!CollectionUtils.isEmpty(activitySearchItem.getPrimaryToSecondaryTypes())) {
                        for (String primaryId : activitySearchItem.getPrimaryToSecondaryTypes().keySet()) {
                            if (activitySearchItem.getPrimaryToSecondaryTypesAvailable().get(primaryId) == null) {
                                courseOfferingByTermSet.remove(primaryId);
                            } else if (activitySearchItem.getPrimaryToSecondaryTypesAvailable().get(primaryId).size() != activitySearchItem.getPrimaryToSecondaryTypes().get(primaryId).size()) {
                                courseOfferingByTermSet.remove(primaryId);
                            }
                        }
                    }
                }


                // Check to see if the course is offered
                for (CourseSearchItem item : courses) {
                    if (getCourseHelper().isCourseInOfferingIds(item.getSubject(), item.getNumber(), courseOfferingByTermSet)) {
                        item.addScheduledTerm(term.getName());
                        if (!CourseSearchConstants.SEARCH_TERM_ANY_ITEM.equals(form.getSearchTerm()) && term.getId().equals(form.getSearchTerm())) {
                            String code = getCourseHelper().joinStringsByDelimiter(' ', item.getSubject().trim(), item.getNumber().trim());
                            ActivitySearchItem activitySearchItem = coursesToActivitySearchItem.get(code);
                            if (activitySearchItem != null) {
                                if (!CollectionUtils.isEmpty(activitySearchItem.getPrimaryToSecondaries())) {
                                    for (String primaryId : activitySearchItem.getPrimaryToSecondaries().keySet()) {
                                        if (!item.isSortToTop()) {
                                            item.setSortToTop(activitySearchItem.getPrimaryToSecondaries().get(primaryId) == null || activitySearchItem.getPrimaryToSecondaries().get(primaryId).size() > 0);
                                        } else {
                                            /*Since we only need to check if there is atLeast one match of primary and secondary for this particular course*/
                                            break;
                                        }
                                    }
                                    item.getMeetingDaysAndTimes().addAll(activitySearchItem.getMeetingTimes());
                                    item.setMeetingDayFacetKeys(getMeetingDataByTypeWithDelimiters(item.getMeetingDaysAndTimes(), MEETING_DAYS));
                                    item.setMeetingTimeFacetKeys(getMeetingDataByTypeWithDelimiters(item.getMeetingDaysAndTimes(), MEETING_TIMES));
                                    item.setMeetingDayTimeFacetKeys(getMeetingDataByTypeWithDelimiters(item.getMeetingDaysAndTimes(), MEETING_DAY_TIMES));
                                }
                            }
                        }
                    }
                }

            }
            logger.info("End of method loadScheduledTerms of CourseSearchController:" + System.currentTimeMillis());
        } catch (Exception e) {
            // TODO: Eating this error sucks
            logger.error("Web service call failed.", e);
        }
    }


    /**
     * Returns back the data from the given meeting day times for given type with delimiters added.
     * Types:
     * MEETING_DAYS
     * MEETING_TIMES
     * MEETING_DAY_TIMES
     *
     * @param meetingDayTimes
     * @param type
     * @return
     */
    private Set<String> getMeetingDataByTypeWithDelimiters(Set<String> meetingDayTimes, String type) {
        Set<String> data = new LinkedHashSet<String>();
        if (MEETING_DAYS.equals(type)) {
            for (String meetingDayTime : meetingDayTimes) {
                List<String> dayTimes = Arrays.asList(meetingDayTime.split(":"));
                for (String dayTime : dayTimes) {
                    if (MEETING_DAY_TIMES_TBA.equalsIgnoreCase(dayTime)) {
                        data.add(";" + MEETING_DAY_TIMES_TBA + ";");
                    } else if (StringUtils.hasText(dayTime)) {
                        data.add(";" + dayTime.charAt(0) + ";");
                    }
                }
            }
        } else if (MEETING_TIMES.equals(type)) {
            for (String meetingDayTime : meetingDayTimes) {
                List<String> dayTimes = Arrays.asList(meetingDayTime.split(":"));
                for (String dayTime : dayTimes) {
                    if (MEETING_DAY_TIMES_TBA.equalsIgnoreCase(dayTime)) {
                        data.add(";" + MEETING_DAY_TIMES_TBA + ";");
                    } else if (StringUtils.hasText(dayTime)) {
                        data.add(";" + dayTime.substring(1, dayTime.length()) + ";");
                    }
                }
            }
        } else if (MEETING_DAY_TIMES.equals(type)) {
            for (String meetingDayTime : meetingDayTimes) {
                List<String> dayTimes = Arrays.asList(meetingDayTime.split(":"));
                for (String dayTime : dayTimes) {
                    if (StringUtils.hasText(dayTime)) {
                        data.add(";" + dayTime + ";");
                    }
                }
            }
        }

        return data;
    }

    private void loadTermsOffered(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadTermsOffered of CourseSearchController:" + System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequestInfo request = new SearchRequestInfo("myplan.course.info.atp");
        request.addParam("courseID", courseId);

        List<TypeInfo> termsOffered = new ArrayList<TypeInfo>();
        SearchResultInfo result = null;
        try {
            result = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PermissionDeniedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (SearchResultRow row : result.getRows()) {
            String id = SearchHelper.getCellValue(row, "atp.id");

            // Don't add the terms that are not found
            TypeInfo atpTypeInfo = null;
            try {
                atpTypeInfo = getTypeService().getType(id, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {

            }
            if (atpTypeInfo != null) {
                termsOffered.add(atpTypeInfo);
            }
        }

        Collections.sort(termsOffered, getAtpTypeComparator());
        course.setTermInfoList(termsOffered);
        logger.info("End of method loadTermsOffered of CourseSearchController:" + System.currentTimeMillis());
    }

    private void loadGenEduReqs(CourseSearchItem course) throws MissingParameterException {
        logger.info("Start of method loadGenEduReqs of CourseSearchController:" + System.currentTimeMillis());
        String courseId = course.getCourseId();
        SearchRequestInfo request = new SearchRequestInfo("myplan.course.info.gened");
        request.addParam("courseID", courseId);
        List<String> reqs = new ArrayList<String>();
        SearchResultInfo result = null;
        try {
            result = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PermissionDeniedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (SearchResultRow row : result.getRows()) {
            String genEd = SearchHelper.getCellValue(row, "gened.name");
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

    /**
     * Courses status map (which tells if that course is planned, bookmarked or not planned)
     *
     * @param studentID
     * @return
     * @throws Exception
     */
    private Map<String, CourseSearchItem.PlanState> getCourseStatusMap(String studentID) throws Exception {
        logger.info("Start of method getCourseStatusMap of CourseSearchController:" + System.currentTimeMillis());

        Map<String, CourseSearchItem.PlanState> savedCourseSet = new HashMap<String, CourseSearchItem.PlanState>();

        /*
         *  For each plan item in each plan set the state based on the type.
         */
        List<String> planItemTypes = Arrays.asList(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        List<PlanItemInfo> planItemList = getPlanHelper().getPlanItemsByTypes(getUserSessionHelper().getStudentId(), planItemTypes);
        for (PlanItemInfo planItem : planItemList) {
            if (PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType())) {
                String courseID = planItem.getRefObjectId();
                String crossListedCourse = getPlanHelper().getCrossListedCourse(planItem.getAttributes());
                CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(courseID, crossListedCourse);
                CourseSearchItem.PlanState state;
                if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                    state = CourseSearchItem.PlanState.SAVED;
                } else if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)
                        || planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
                    state = CourseSearchItem.PlanState.IN_PLAN;
                } else {
                    throw new RuntimeException("Unknown plan item type.");
                }
                if (courseInfo != null) {
                    savedCourseSet.put(String.format("%s:%s:%s", courseID, courseInfo.getSubjectArea().trim(), courseInfo.getCourseNumberSuffix().trim()), state);
                }
            }
        }
        logger.info("End of method getCourseStatusMap of CourseSearchController:" + System.currentTimeMillis());
        return savedCourseSet;
    }

    /**
     * A list of course search items are returned back if the provided courseId has crossListing courses
     * else a list with only one Object is sent back
     *
     * @param courseId
     * @return
     * @throws MissingParameterException
     */
    private List<CourseSearchItem> getCourseInfo(String courseId) throws MissingParameterException {
        logger.info("Start of method getCourseInfo of CourseSearchController:" + System.currentTimeMillis());
        List<CourseSearchItem> courseSearchItems = new ArrayList<CourseSearchItem>();

        SearchRequestInfo request = new SearchRequestInfo("myplan.course.info");
        request.addParam("courseID", courseId);
        SearchResultInfo result = null;
        try {
            result = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not load courseInfo for CourseId: " + courseId, e);
        }

        for (SearchResultRow row : result.getRows()) {
            CourseSearchItem course = new CourseSearchItem();
            String name = SearchHelper.getCellValue(row, "course.name");
            String number = SearchHelper.getCellValue(row, "course.number");
            String subject = SearchHelper.getCellValue(row, "course.subject");
            String level = SearchHelper.getCellValue(row, "course.level");
            String creditsID = SearchHelper.getCellValue(row, "course.credits");
            String code = SearchHelper.getCellValue(row, "course.code");
            String versionIndId = SearchHelper.getCellValue(row, "course.verIndId");
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

            courseSearchItems.add(course);
        }


        logger.info("End of method getCourseInfo of CourseSearchController:" + System.currentTimeMillis());
        return courseSearchItems;
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
            if (StringUtils.hasText(req)) {
                if (req.contains("&")) {
                    req = req.replace("&", "&amp;");
                }

                genEdsOut.append(req);
            }
        }
        return genEdsOut.toString();
    }

    private String urlEscape(String text) {
        text = text.replace(" ", "%20");
        text = text.replace("&", "%26");
        return text;
    }

    protected CluService getLuService() {
        if (this.luService == null) {
            this.luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return this.luService;
    }

    public TypeService getTypeService() {
        if (typeService == null) {
            typeService = (TypeService)
                    GlobalResourceLoader.getService(new QName(TypeServiceConstants.NAMESPACE, TypeServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
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

    public void setLuService(CluService luService) {
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

    public Comparator<TypeInfo> getAtpTypeComparator() {
        if (atpTypeComparator == null) {
            atpTypeComparator = UwMyplanServiceLocator.getInstance().getAtpTypeComparator();
        }
        return atpTypeComparator;
    }

    public void setAtpTypeComparator(Comparator<TypeInfo> atpTypeComparator) {
        this.atpTypeComparator = atpTypeComparator;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = UwMyplanServiceLocator.getInstance().getPlanHelper();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public SearchService getCourseOfferingSearchService() {
        if (courseOfferingSearchService == null) {
            this.courseOfferingSearchService = KsapFrameworkServiceLocator.getCourseOfferingSearchService();
        }
        return courseOfferingSearchService;
    }

    public void setCourseOfferingSearchService(SearchService courseOfferingSearchService) {
        this.courseOfferingSearchService = courseOfferingSearchService;
    }
}


