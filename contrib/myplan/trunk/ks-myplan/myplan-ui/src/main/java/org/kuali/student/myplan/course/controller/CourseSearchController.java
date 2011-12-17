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
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.*;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
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

    private transient CourseService courseService;

    private transient Map<String, String> atpCache;

    private TermInfoComparator atpTypeComparator;

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

    @RequestMapping(params = "methodToCall=searchForCourses")
    public ModelAndView searchForCourses(@ModelAttribute("KualiForm") CourseSearchForm form, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) {

        HashMap<String, Credit> creditMap = new HashMap<String, Credit>();
        {
            // Don't think this will ever be used
            Credit credit = new Credit();
            credit.id = "u";
            credit.min = 0.0f;
            credit.max = 0.0f;
            credit.display = "Unknown";
            credit.type = CourseSearchItem.CreditType.unknown;
        }

        try {
            SearchRequest searchRequest = new SearchRequest();

            searchRequest.setSearchKey("myplan.course.info.credits.details");
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
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

        //  Initialize facets.
        CurriculumFacet curriculumFacet = new CurriculumFacet();
        CreditsFacet creditsFacet = new CreditsFacet();
        CourseLevelFacet courseLevelFacet = new CourseLevelFacet();
        GenEduReqFacet genEduReqFacet = new GenEduReqFacet();
        TermsFacet termsFacet = new TermsFacet();

        CourseSearchStrategy searcher = new CourseSearchStrategy();
        try {
            List<SearchRequest> requests = searcher.queryToRequests(form);

            HashMap<String, Hit> courseMap = new HashMap<String, Hit>();

            done:
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

                            if (courseMap.size() == MAX_HITS) {
                                break done;
                            }
                        }
                    }
                }
            }


            ArrayList<CourseSearchItem> searchResults = new ArrayList<CourseSearchItem>();

            Hit[] hits = courseMap.values().toArray(new Hit[0]);
            Arrays.sort(hits, new HitComparator());

            for (Hit hit : hits) {
                String courseId = hit.courseID;

                {
                    CourseSearchItem course = new CourseSearchItem();
                    {
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setSearchKey("myplan.course.info");
                        List<SearchParam> params = new ArrayList<SearchParam>();
                        params.add(new SearchParam("courseID", courseId));
                        searchRequest.setParams(params);

                        SearchResult searchResult = getLuService().search(searchRequest);
                        for (SearchResultRow row : searchResult.getRows()) {
                            Iterator<SearchResultCell> i = row.getCells().iterator();
                            String name = i.next().getValue();
                            String number = i.next().getValue();
                            String subject = i.next().getValue();
                            String level = i.next().getValue();
                            String id = i.next().getValue();

                            course.setCourseId(courseId);
                            course.setSubject(subject);
                            course.setNumber(number);
                            course.setLevel(level);
                            course.setCourseName(name);
                            course.setCode(subject + " " + number);

                            Credit credit = null;
                            if (creditMap.containsKey(id)) {
                                credit = creditMap.get(id);
                            } else {
                                credit = creditMap.get("u");
                            }
                            course.setCreditMin(credit.min);
                            course.setCreditMax(credit.max);
                            course.setCreditType(credit.type);
                            course.setCredit(credit.display);
                        }
                    }

                    // Load Terms Offered
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

                        Collections.sort(termsOffered, atpTypeComparator);
                        course.setTermInfoList(termsOffered);
                        course.setTermsDisplayName(formatTermsOffered(termsOffered));
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


                    searchResults.add(course);
                }
            }

            //  Add the facet data to the response.
            form.setCurriculumFacetItems(curriculumFacet.getFacetItems());
            form.setCreditsFacetItems(creditsFacet.getFacetItems());
            form.setGenEduReqFacetItems(genEduReqFacet.getFacetItems());
            form.setCourseLevelFacetItems(courseLevelFacet.getFacetItems());
            form.setTimeScheduleFacetItems(termsFacet.getFacetItems());

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

    private String formatTermsOffered(List<AtpTypeInfo> terms) {
        StringBuffer termsOut = new StringBuffer();
        int i = 0;
        for (AtpTypeInfo term : terms) {
            if (i > 0 && i != termsOut.length()) {
                termsOut.append(", ");
            }
            termsOut.append(term.getName().substring(0, 2).toUpperCase());
            i++;
        }
        return termsOut.toString();
    }


    // TODO: This should be turned into a ATP service level cache
    //@Cacheable("atpType")
    private AtpTypeInfo getATPType(String key) {
        try {
            return getAtpService().getAtpType(key);
        } catch (Exception e) {
            logger.error("Could not find ATP Type: " + key);
            return null;
        }
    }

    protected LuService getLuService() {
        if (luService == null) {
            luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

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

    public TermInfoComparator getAtpTypeComparator() {
        return atpTypeComparator;
    }

    @Autowired
    public void setAtpTypeComparator(TermInfoComparator atpTypeComparator) {
        this.atpTypeComparator = atpTypeComparator;
    }
}
