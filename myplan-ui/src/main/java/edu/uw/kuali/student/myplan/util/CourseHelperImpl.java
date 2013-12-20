package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.controller.QueryTokenizer;
import org.kuali.student.myplan.course.controller.TokenPairs;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.SearchHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultCell;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.course.dto.CourseCrossListingInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.infc.CourseCrossListing;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;
import org.kuali.student.r2.lum.util.constants.CourseServiceConstants;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;

public class CourseHelperImpl implements CourseHelper {

    private final Logger logger = Logger.getLogger(CourseHelperImpl.class);

    private StudentServiceClient studentServiceClient;

    private CourseService courseService;

    private CourseOfferingService courseOfferingService;

    private AcademicPlanService academicPlanService;

    private CluService luService;


    private static Document newDocument(String xml) throws DocumentException {
        SAXReader sax = new SAXReader();
        StringReader sr = new StringReader(xml);
        Document doc = sax.read(sr);
        return doc;
    }

    private static Map<String, String> NAMESPACES = new HashMap<String, String>() {{
        put("s", "http://webservices.washington.edu/student/");
    }};


    private static DefaultXPath newXPath(String expr) {
        DefaultXPath path = new DefaultXPath(expr);
        path.setNamespaceURIs(NAMESPACES);
        return path;
    }

    /**
     * returns back all the section statuses
     *
     * @param mapmap
     * @param yt
     * @param curric
     * @param num
     * @throws ServiceException
     * @throws DocumentException
     */
    @Override
    public void getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, AtpHelper.YearTerm yt,
                                    String curric, String num) throws ServiceException, DocumentException {

        StudentServiceClient client = getStudentServiceClient();
        // call SWS get enrollment info for all sections
        String year = yt.getYearAsString();
        String quarter = yt.getTermAsID();
        String xml = client.getAllSectionsStatus(year, quarter, curric, num);
        Document doc = newDocument(xml);
        DefaultXPath statusPath = newXPath("/s:List/s:SectionStatuses/s:SectionStatus");
        List list = statusPath.selectNodes(doc);

        // loop through each section, extract info
        for (Object node : list) {
            Element sectionStatus = (Element) node;
            formatSectionStatus(mapmap, sectionStatus, yt);
        }
    }


    /**
     * Used to Split the course code into division and Code.
     * eg: "COM 243" is returned as CourseCode with division=COM and number=243 and section=null.
     * eg: "COM 243 A" is returned as CourseCode with division=COM , number=243 and section=A.
     *
     * @param courseCode
     * @return
     */
    @Override
    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode) {
        String subject = null;
        String number = null;
        String activityCd = null;
        if (courseCode.matches(CourseSearchConstants.FORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = getDivisionVerifiedSubject(splitStr[0].trim().toUpperCase()).trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.COURSE_CODE_WITH_SECTION_REGEX)) {
            activityCd = courseCode.substring(courseCode.lastIndexOf(" "), courseCode.length()).trim();
            courseCode = courseCode.substring(0, courseCode.lastIndexOf(" ")).trim();
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = getDivisionVerifiedSubject(splitStr[0].trim().toUpperCase()).trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = getDivisionVerifiedSubject(splitStr[0].trim().toUpperCase()).trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {
            int size = courseCode.length();
            int splitIndex = size - 3;
            subject = getDivisionVerifiedSubject(courseCode.substring(0, splitIndex).trim().toUpperCase()).trim();
            number = courseCode.substring(splitIndex, size).toLowerCase();
        }
        return new DeconstructedCourseCode(subject, number, activityCd);
    }

    /**
     * Used to get the division verified subject
     * Eg "A S" if typed in as AS it is matched in division map and actual "A S" is returned
     *
     * @param subject
     * @return
     */
    private String getDivisionVerifiedSubject(String subject) {
        HashMap<String, String> divisionMap = fetchCourseDivisions();
        ArrayList<String> divisions = new ArrayList<String>();
        extractDivisions(divisionMap, subject, divisions, false);
        if (!CollectionUtils.isEmpty(divisions)) {
            return divisions.get(0);
        }
        return subject;
    }

    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120)
     * Uses last scheduled term to calculate the course Id
     *
     * @param subjectArea
     * @param number
     * @return
     */
    @Override
    public String getCourseId(String subjectArea, String number) {
        return getCourseIdForTerm(subjectArea, number, AtpHelper.getLastScheduledAtpId());
    }


    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120) for a given term
     *
     * @param subjectArea
     * @param number
     * @return
     */
    @Override
    public String getCourseIdForTerm(String subjectArea, String number, String termId) {
        SearchRequestInfo request = new SearchRequestInfo(CourseSearchConstants.COURSE_SEARCH_FOR_COURSE_ID);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_SUBJECT_PARAM, subjectArea.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_NUMBER_PARAM, number.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_LAST_SCHEDULED_PARAM, termId);
        SearchResultInfo searchResult = new SearchResultInfo();
        try {
            searchResult = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Failed to get courseId for given subject and number", e);
        }
        String courseId = null;
        if (searchResult.getRows().size() > 0) {
            courseId = searchResult.getRows().get(0).getCells().get(0).getValue();
        }
        return courseId;
    }

    /**
     * returns the courseInfo for the given courseId by verifying the courId to be a verifiedcourseId
     *
     * @param courseId
     * @return
     */
    @Override
    public CourseInfo getCourseInfo(String courseId) {

        CourseInfo courseInfo = null;
        try {
            String versionVerifiedId = getVerifiedCourseId(courseId);
            if (StringUtils.isNotEmpty(versionVerifiedId)) {
                courseInfo = getCourseService().getCourse(versionVerifiedId, CourseSearchConstants.CONTEXT_INFO);
            }
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }


        return courseInfo;
    }

    /**
     * This method is used to get the courseInfo based on courseId and courseCd
     * Mostly used for crossListed courses to get exact courseInfo for that course
     * In this method if the courseCd given is a crossListed then that is swapped from the list of crossListings to be a actual courseInfo
     * <p/>
     * Eg: HIST 420 is crossListed and MATH 403 is original both have the same courseId
     * Search for courseInfo based on courseId gives back courseInfo for MATH 403
     * but the actual one we requested is for HIST 420 then that is picked out of the crossListings
     * and made as actual courseInfo and adds the MATH 403 as a crossListing course
     *
     * @param courseId
     * @param courseCd
     * @return
     */
    @Override
    public CourseInfo getCourseInfoByIdAndCd(String courseId, String courseCd) {
        CourseInfo courseInfo = getCourseInfo(courseId);

        /*If requested courseInfo is for a crossListed course then CrossListed course becomes the main course info and the rest becomes the crossListings*/
        if (!StringUtils.isEmpty(courseCd) && courseInfo != null) {
            DeconstructedCourseCode courseCode = getCourseDivisionAndNumber(courseCd);
            List<CourseCrossListingInfo> courseCrossListingInfos = new ArrayList<CourseCrossListingInfo>();
            if (!courseCode.getSubject().trim().equals(courseInfo.getSubjectArea().trim()) || !courseCode.getNumber().trim().equals(courseInfo.getCourseNumberSuffix().trim())) {
                CourseCrossListingInfo crossListing = new CourseCrossListingInfo();
                crossListing.setCode(courseInfo.getCode());
                crossListing.setCourseNumberSuffix(courseInfo.getCourseNumberSuffix());
                crossListing.setSubjectArea(courseInfo.getSubjectArea());
                crossListing.setTypeKey(CourseAssemblerConstants.COURSE_OFFICIAL_IDENT_TYPE);
                courseCrossListingInfos.add(crossListing);
                for (CourseCrossListingInfo crossListingInfo : courseInfo.getCrossListings()) {
                    if (courseCode.getSubject().trim().equals(crossListingInfo.getSubjectArea().trim()) && courseCode.getNumber().trim().equals(crossListingInfo.getCourseNumberSuffix().trim())) {
                        courseInfo.setSubjectArea(crossListingInfo.getSubjectArea());
                        courseInfo.setCourseNumberSuffix(crossListingInfo.getCourseNumberSuffix());
                        courseInfo.setCode(crossListingInfo.getCode());
                    } else {
                        courseCrossListingInfos.add(crossListingInfo);
                    }
                }
                if (!CollectionUtils.isEmpty(courseCrossListingInfos)) {
                    courseInfo.setCrossListings(courseCrossListingInfos);
                }
            }

        }

        return courseInfo;
    }


    /**
     * @param element
     * @param name
     * @return
     */
    private static int getAsInteger(Element element, String name) {
        int result = 0;
        try {
            String enrollmentLimit = element.elementText(name);
            result = Integer.valueOf(enrollmentLimit);
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * @param parent
     * @param sectionStatus
     * @param yt
     * @throws DocumentException
     */
    private void formatSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> parent, Element sectionStatus,
                                     AtpHelper.YearTerm yt) throws DocumentException {
        String sln = sectionStatus.elementText("SLN");
        int enrollMaximum = getAsInteger(sectionStatus, "LimitEstimateEnrollment");
        int enrollCount = getAsInteger(sectionStatus, "CurrentEnrollment");
        String status = sectionStatus.elementText("Status");
        String limitEstimateEnrollmentIndicator = sectionStatus.elementText("LimitEstimateEnrollmentIndicator");
        boolean enrollEstimate = "estimate".equalsIgnoreCase(limitEstimateEnrollmentIndicator);

        LinkedHashMap<String, Object> childmap = new LinkedHashMap<String, Object>();
        childmap.put("enrollCount", enrollCount);
        childmap.put("enrollMaximum", enrollMaximum);
        childmap.put("status", status);
        childmap.put("enrollEstimate", enrollEstimate);
        String atpId = yt.toATP().replace('.', '-');
        String key = "enrl_" + atpId + "_" + sln;
        parent.put(key, childmap);
    }

    /**
     * @param delimiter
     * @param list
     * @return
     */
    @Override
    public String joinStringsByDelimiter(char delimiter, String... list) {
        return StringUtils.join(list, delimiter);
    }

    /**
     * Takes a courseId that can be either a version independent Id or a version dependent Id and
     * returns a version dependent Id. In case of being passed in a version depend
     *
     * @param courseId
     * @return
     */
    @Override
    public String getVerifiedCourseId(String courseId) {
        return getCourseVersionIdByTerm(courseId, AtpHelper.getLastScheduledAtpId());
    }

    /**
     * returns a course version Id for given term and courseId
     *
     * @param courseId
     * @param termId
     * @return
     */
    @Override
    public String getCourseVersionIdByTerm(String courseId, String termId) {
        String courseVersionId = null;
        try {
            SearchRequestInfo req = new SearchRequestInfo("myplan.course.version.id");
            req.addParam("courseId", courseId);
            req.addParam("courseId", courseId);
            req.addParam("lastScheduledTerm", termId);
            SearchResultInfo result = getLuService().search(req, CourseSearchConstants.CONTEXT_INFO);
            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    if ("lu.resultColumn.cluId".equals(cell.getKey())) {
                        courseVersionId = cell.getValue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("version verified Id retrieval failed", e);
        }
        return courseVersionId;
    }


    /**
     * returns the course code from given activityId
     * <p/>
     * eg: for activityId '2013:2:CHEM:152:A' course code CHEM  152 is returned
     *
     * @param activityId
     * @return
     */
    @Override
    public String getCourseCdFromActivityId(String activityId) {
        ActivityOfferingDisplayInfo activityDisplayInfo = null;
        try {
            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
        }
        if (activityDisplayInfo != null) {
            /*TODO: move this to Coursehelper to make it institution neutral*/
            String courseOfferingId = null;
            for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
                    courseOfferingId = attributeInfo.getValue();
                    break;
                }
            }
            CourseOfferingInfo courseOfferingInfo = null;
            try {
                courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
            }

            if (courseOfferingInfo != null) {
                return courseOfferingInfo.getCourseCode();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * returns the Activity code from given activityId
     * <p/>
     * eg: for activityId '2013:2:CHEM:152:A' activity code A is returned
     *
     * @param activityId
     * @return
     */
    @Override
    public String getCodeFromActivityId(String activityId) {
        ActivityOfferingDisplayInfo activityDisplayInfo = null;
        try {
            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
        }
        if (activityDisplayInfo != null) {
            return activityDisplayInfo.getActivityOfferingCode();
        } else {
            return null;
        }
    }

    /**
     * UW Implementation checks by breaking down courseOfferingId.
     * KSAP should use courseOffering service to accomplish the same
     *
     * @param subjectArea
     * @param courseNumber
     * @param courseOfferingIds
     * @return
     */
    @Override
    public boolean isCourseInOfferingIds(String subjectArea, String courseNumber, Set<String> courseOfferingIds) {
        for (String offeringId : courseOfferingIds) {
            if (offeringId.contains(":" + subjectArea + ":" + courseNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns a SLN for given params
     *
     * @param year
     * @param term
     * @param subject
     * @param number
     * @param activityCd
     * @return
     */
    @Override
    public String getSLN(String year, String term, String subject, String number, String activityCd) {
        String sln = null;
        String activityId = joinStringsByDelimiter(':', year, term, subject, number, activityCd);
        ActivityOfferingDisplayInfo activityOfferingInfo = null;
        try {
            activityOfferingInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, CourseSearchConstants.CONTEXT_INFO);
            if (activityOfferingInfo != null) {
                for (AttributeInfo attributeInfo : activityOfferingInfo.getAttributes()) {
                    if (attributeInfo.getKey().equalsIgnoreCase("SLN")) {
                        sln = attributeInfo.getValue();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("could not load the ActivityOfferinInfo from SWS", e);
        }

        return sln;
    }

    /**
     * builds the refObjId for activity PlanItems (eg: '2013:2:CHEM:152:A')
     *
     * @param atpId
     * @param subject
     * @param number
     * @param activityCd
     * @return
     */
    @Override
    public String buildActivityRefObjId(String atpId, String subject, String number, String activityCd) {
        AtpHelper.YearTerm yearTerm = AtpHelper.atpToYearTerm(atpId);
        return joinStringsByDelimiter(':', yearTerm.getYearAsString(), yearTerm.getTermAsString(), subject, number, activityCd);
    }

    /**
     * Populates available course Divisions Map
     *
     * @return
     */
    @Override
    public HashMap<String, String> fetchCourseDivisions() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            SearchRequestInfo request = new SearchRequestInfo("myplan.distinct.clu.divisions");

            SearchResultInfo result = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);

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
            logger.error("Failed to fetch course divisions", e);
        }
        return map;
    }

    /**
     * Extracts the division for given Query
     *
     * @param divisionMap
     * @param query
     * @param divisions
     * @param isSpaceAllowed
     * @return
     */
    @Override
    public String extractDivisions(HashMap<String, String> divisionMap, String query, List<String> divisions, boolean isSpaceAllowed) {
        boolean match = true;
        while (match) {
            match = false;
            // Retokenize after each division found is removed
            // Remove extra spaces to normalize input
            if (!isSpaceAllowed) {
                query = query.trim().replaceAll("[\\s\\\\/:?\\\"<>|`~!@#$%^*()_+-={}\\]\\[;',.]", " ");
            } else {
                query = query.replaceAll("[\\\\/:?\\\"<>|`~!@#$%^*()_+-={}\\]\\[;',.]", " ");
            }
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

    /**
     * Checks to see if the division and level exists
     *
     * @param division
     * @param level
     * @return true if for the given division and level courses exists else false
     */
    @Override
    public boolean isValidCourseLevel(String division, String level) {
        List<SearchRequestInfo> requests = new ArrayList<SearchRequestInfo>();
        SearchRequestInfo request = new SearchRequestInfo(CourseSearchConstants.COURSE_SEARCH_FOR_DIVISION_LEVELS);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_SUBJECT_PARAM, division.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_NUMBER_PARAM, level.trim());
        requests.add(request);
        SearchResultInfo searchResult = new SearchResultInfo();
        try {
            searchResult = getLuService().search(request, CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not get courses by division and level", e);
        }
        if (!CollectionUtils.isEmpty(searchResult.getRows())) {
            return true;
        }
        return false;
    }


    /**
     * Method used to generate a compositeKey for CourseOffering data search
     * format: courseId|subject|number
     * <p/>
     * Eg: 'c87e6090-a177-445b-9a7d-7915120e4adc:ENGL:201'
     *
     * @param courseId
     * @param subject
     * @param number
     * @return
     */
    @Override
    public String getKeyForCourseOffering(String courseId, String subject, String number) {
        return String.format("%s|%s|%s", courseId, subject, number);
    }


    /**
     * Method used to know if the given courseCd is a alias/CrossListed or a regular parent course
     *
     * @param courseInfo
     * @param courseCd
     * @return
     */
    @Override
    public boolean isCrossListedCourse(CourseInfo courseInfo, String courseCd) throws DoesNotExistException {
        DeconstructedCourseCode courseCode = getCourseDivisionAndNumber(courseCd);
        if (courseCode.getSubject().trim().equals(courseInfo.getSubjectArea().trim()) && courseCode.getNumber().trim().equals(courseInfo.getCourseNumberSuffix().trim())) {
            return false;
        } else {
            for (CourseCrossListingInfo crossListingInfo : courseInfo.getCrossListings()) {
                if (courseCode.getSubject().trim().equals(crossListingInfo.getSubjectArea().trim()) && courseCode.getNumber().equals(crossListingInfo.getCourseNumberSuffix().trim())) {
                    return true;
                }
            }
        }
        throw new DoesNotExistException();
    }

    /**
     * Checks if the courseCd's given are similar or not , either they might be in formatted or unFormatted form
     *
     * @param courseCd1
     * @param courseCd2
     * @return true if subject and numbers matched for the two courses else false
     */
    @Override
    public boolean isSimilarCourses(String courseCd1, String courseCd2) {
        if (!StringUtils.isEmpty(courseCd1) && !StringUtils.isEmpty(courseCd2)) {
            DeconstructedCourseCode courseCode1 = getCourseDivisionAndNumber(courseCd1);
            DeconstructedCourseCode courseCode2 = getCourseDivisionAndNumber(courseCd2);
            return courseCode1.getSubject().equals(courseCode2.getSubject()) && courseCode1.getNumber().equals(courseCode2.getNumber());
        }
        return false;
    }

    protected CluService getLuService() {
        if (luService == null) {
            luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return luService;
    }

    public void setLuService(CluService luService) {
        this.luService = luService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public synchronized void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    public StudentServiceClient getStudentServiceClient() {
        if (studentServiceClient == null) {
            studentServiceClient = (StudentServiceClient) GlobalResourceLoader.getService(StudentServiceClient.SERVICE_NAME);
        }
        return studentServiceClient;
    }

}
