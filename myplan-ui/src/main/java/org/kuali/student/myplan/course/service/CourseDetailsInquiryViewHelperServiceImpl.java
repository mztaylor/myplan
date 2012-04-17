package org.kuali.student.myplan.course.service;

import java.lang.String;
import java.text.BreakIterator;
import java.util.*;
import javax.xml.namespace.QName;

import org.apache.cxf.aegis.type.java5.XmlParamType;
import org.apache.log4j.Logger;


import org.joda.time.YearMonth;
import org.kuali.rice.core.api.criteria.EqualPredicate;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.core.statement.service.StatementService;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;


import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.course.util.CurriculumFacet;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;


public class CourseDetailsInquiryViewHelperServiceImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryViewHelperServiceImpl.class);

    private transient CourseService courseService;

    private transient StatementService statementService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AtpService atpService;

    private transient EnumerationManagementService enumService;

    private transient AcademicPlanService academicPlanService;

    private  transient CourseInfo courseInfo;

    public CourseInfo getCourseInfo() {
        return courseInfo;
    }

    public void setCourseInfo(CourseInfo courseInfo) {
        this.courseInfo = courseInfo;
    }

    /*Remove the HashMap after enumeration service is in the ehcache and remove the hashmap occurance in this*/
    private HashMap<String, List<EnumeratedValueInfo>> hashMap = new HashMap<String, List<EnumeratedValueInfo>>();

    public HashMap<String, List<EnumeratedValueInfo>> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, List<EnumeratedValueInfo>> hashMap) {
        this.hashMap = hashMap;
    }

    //TODO: These should be changed to an ehCache spring bean
    private Map<String, String> campusLocationCache;
    private Map<String, String> atpCache;

    @Override
    public CourseDetails retrieveDataObject(Map fieldValues) {
        return retrieveCourseDetails((String) fieldValues.get("courseId"));
    }


    /**
     * Populates course with catalog information (title, id, code, description) and next offering information.
     * Other properties are left empty and a flag is set to indicate only summary view
     * @param courseId
     * @return
     */
    public CourseDetails retrieveCourseSummary(String courseId) {
        CourseDetails courseDetails = new CourseDetails();
        courseDetails.setSummaryOnly(true);

        CourseInfo course = getCourseInfo();
        try {
            course = getCourseService().getCourse(courseId);
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        courseDetails.setCourseId(course.getId());
        courseDetails.setCode(course.getCode());
        courseDetails.setCourseDescription(course.getDescr().getFormatted());
        courseDetails.setCredit(CreditsFormatter.formatCredits(course));
        courseDetails.setCourseTitle(course.getCourseTitle());

        // Terms Offered
        initializeAtpTypesCache();
        List<String> termsOffered = new ArrayList<String>();
        for (String term : course.getTermsOffered()) {
            termsOffered.add(atpCache.get(term));
        }
        courseDetails.setTermsOffered(termsOffered);


        return courseDetails;

    }


    public CourseDetails retrieveCourseDetails(String courseId) {

        CourseDetails courseDetails = retrieveCourseSummary(courseId);
        courseDetails.setSummaryOnly(false);

        CourseInfo course = null;
        try {
            course = getCourseService().getCourse(courseId);
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        // Campus Locations
        initializeCampusLocations();
        List<String> enumeratedCampus = new ArrayList<String>();
        for (String campus : course.getCampusLocations()) {
            enumeratedCampus.add(this.campusLocationCache.get(campus));
        }

        courseDetails.setCampusLocations(enumeratedCampus);

        //  Lookup course statements and build the requisites list.
        List<StatementTreeViewInfo> statements = null;
        try {
            statements = getCourseService().getCourseStatements(courseId, null, null);
        } catch (DoesNotExistException e) {
            //  TODO: Is this a problem?
        } catch (Exception e) {
            logger.error("Course Statement lookup failed.", e);
        }

        List<String> reqs = new ArrayList<String>();
        for (StatementTreeViewInfo stvi : statements) {
            String statement = null;
            try {
                statement = getStatementService().translateStatementTreeViewToNL(stvi, "kuali.uw.rule.crsRequisite.myplan", "en");
            } catch (Exception e) {
                logger.error("Translation of Course Statement to natural language failed.", e);
                continue;
            }
            reqs.add(statement);
        }
        courseDetails.setRequisites(reqs);


        // Get only the abbre_val of gen ed requirements

        List<String> abbrGenEdReqs = new ArrayList<String>();
        Map<String, String> abbrAttributes = course.getAttributes();
        for (Map.Entry<String, String> entry : abbrAttributes.entrySet()) {
            if (entry.getValue().equals("true") && entry.getKey().startsWith(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX)) {
                String r = entry.getKey().replace(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX, "");
                abbrGenEdReqs.add(r);
            }
        }
        courseDetails.setAbbrGenEdRequirements(abbrGenEdReqs);


        //  Get general education requirements.
        List<String> genEdReqs = new ArrayList<String>();

        Map<String, String> attributes = course.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {

            if (entry.getValue().equals("true") && entry.getKey().startsWith(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX)) {
                String keyTemp = entry.getKey().replace(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX, "");
                String r = getGenEdReqValue(keyTemp);
                r = r + " (" + entry.getKey().replace(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX, "") + ")";
                genEdReqs.add(r);
            }
        }
        courseDetails.setGenEdRequirements(genEdReqs);


        /*
          Use the course offering service to see if the course is being offered in the selected term.
          Note: In the UW implementation of the Course Offering service, course id is actually course code.
        */
        try {
            //  Fetch the available terms from the Academic Calendar Service.
            List<TermInfo> termInfos = null;
            try {
                termInfos = getAcademicCalendarService().getCurrentTerms(CourseSearchConstants.PROCESS_KEY,
                        CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Web service call failed.", e);
                //  Create an empty list to Avoid NPE below allowing the data object to be fully initialized.
                termInfos = new ArrayList<TermInfo>();
            }

            List<String> scheduledTerms = new ArrayList<String>();
            for (TermInfo term : termInfos) {
                String key = term.getId();
                String subject = course.getSubjectArea();

                List<String> offerings = getCourseOfferingService()
                        .getCourseOfferingIdsByTermAndSubjectArea(key, subject, CourseSearchConstants.CONTEXT_INFO);

                if (offerings.contains(course.getCode())) {
                    scheduledTerms.add(term.getName());
                }
            }

            courseDetails.setScheduledTerms(scheduledTerms);

            AcademicPlanService academicPlanService = getAcademicPlanService();

            Person user = GlobalVariables.getUserSession().getPerson();

            ContextInfo context = new ContextInfo();
            String studentID = user.getPrincipalId();

            //   Get the first learning plan. There should only be one ...
            String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
            List<LearningPlanInfo> plans = academicPlanService.getLearningPlansForStudentByType(studentID, planTypeKey, context);
            if (plans.size() > 0) {
                LearningPlan plan = plans.get(0);

                //  Fetch the plan items which are associated with the plan.
                List<PlanItemInfo> planItemsInPlan = academicPlanService.getPlanItemsInPlan(plan.getId(), context);
                courseDetails.setInSavedCourseList(false);

                //  Iterate through the plan items and set flags to indicate whether the item is a planned/backup or saved course.
                for (PlanItem planItemInPlanTemp : planItemsInPlan) {
                    if (planItemInPlanTemp.getRefObjectId().equals(courseDetails.getCourseId())) {
                        //  Assuming type is planned or backup if not wishlist.
                        if (planItemInPlanTemp.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                            courseDetails.setInSavedCourseList(true);
                            courseDetails.setSavedCourseItemId(planItemInPlanTemp.getId());
                            courseDetails.setSavedCourseDateCreated(planItemInPlanTemp.getMeta().getCreateTime());
                        } else {
                            courseDetails.setInPlannedCourseList(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception loading course offering for:" + course.getCode(), e);
        }

        //Curriculum
        String courseCode = courseDetails.getCode();
        String subject=null;
        String number=null;
        if(courseCode!=null){
        String[] splitStr = courseCode.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        subject = splitStr[0];
        number = splitStr[1];
        String temp = getTitle(subject);
        StringBuffer value = new StringBuffer();
        value = value.append(temp);
        value = value.append(" (").append(subject.trim()).append(")");

        courseDetails.setCurriculumTitle(value.toString());
        }
        //If course not scheduled for future terms, Check for the last term when course was offered
        if (courseDetails.getScheduledTerms().size() == 0) {
            int year = Calendar.getInstance().get(Calendar.YEAR) - 10;
            List<CourseOfferingInfo> courseOfferingInfo = null;
            try {
                // The right strategy would be using the multiple equal predicates joined using an and
                courseOfferingInfo = getCourseOfferingService().searchForCourseOfferings(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("values", "" + year + "," + subject + "," + number + "")), CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("could not load courseOfferingInfo list");
            }
            if (courseOfferingInfo.size()>0) {
                String lastOffered = courseOfferingInfo.get(0).getTermId();
                lastOffered = lastOffered.substring(0, 1).toUpperCase().concat(lastOffered.substring(1, lastOffered.length()));
                courseDetails.setLastOffered(lastOffered);
            }

        }
        return courseDetails;
    }


    /**
     * To get the full text for gen ed requirements
     *
     * @param key
     * @return genEdReqValue
     */

    protected String getGenEdReqValue(String key) {

        String genEdReqValue = null;

        try {
            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!hashMap.containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationService().getEnumeratedValues("kuali.uw.lu.genedreq", null, null, null);
            } else {
                enumeratedValueInfoList = hashMap.get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String abbr = enumVal.getAbbrevValue();

                if (abbr.equalsIgnoreCase(key)) {
                    genEdReqValue = enumVal.getValue();
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }

        return genEdReqValue;
    }


    /**
     * To get the title for the respective display name
     *
     * @param display
     * @return
     */
    protected String getTitle(String display) {
        String titleValue = null;
        try {
            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!hashMap.containsKey(CourseSearchConstants.SUBJECT_AREA)) {
                enumeratedValueInfoList = getEnumerationValueInfoList(CourseSearchConstants.SUBJECT_AREA);
            } else {
                enumeratedValueInfoList = hashMap.get(CourseSearchConstants.SUBJECT_AREA);
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String code = enumVal.getCode().trim();
                if (code.equalsIgnoreCase(display.trim())) {
                    titleValue = enumVal.getValue().trim();
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("Could not load title value");
        }

        return titleValue;
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

    protected synchronized StatementService getStatementService() {
        if (this.statementService == null) {
            this.statementService = (StatementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.STATEMENT_SERVICE_NAMESPACE, "StatementService"));
        }
        return this.statementService;
    }

    protected synchronized EnumerationManagementService getEnumerationService() {
        if (this.enumService == null) {
            this.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return this.enumService;
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

    public void setAtpService(AtpService atpService) {
        this.atpService = atpService;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    protected synchronized void initializeCampusLocations() {
        if (null == campusLocationCache || campusLocationCache.isEmpty()) {
            try {
                List<EnumeratedValueInfo> campusLocations = null;
                if (!hashMap.containsKey(CourseSearchConstants.CAMPUS_LOCATION)) {
                    campusLocations = getEnumerationValueInfoList(CourseSearchConstants.CAMPUS_LOCATION);
                } else {
                    campusLocations = hashMap.get(CourseSearchConstants.CAMPUS_LOCATION);
                }
                if (this.campusLocationCache == null) {
                    this.campusLocationCache = new HashMap<String, String>();
                }
                for (EnumeratedValueInfo campus : campusLocations) {
                    this.campusLocationCache.put(campus.getCode(), campus.getValue());
                }

            } catch (Exception e) {
                logger.error("Could not load campus locations..");
            }
        }
    }

    public List<EnumeratedValueInfo> getEnumerationValueInfoList(String param) {

        List<EnumeratedValueInfo> enumeratedValueInfoList = null;

        try {

            enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(param, null, null, null);
            hashMap.put(param, enumeratedValueInfoList);
        } catch (Exception e) {
            logger.error("No Values for campuses found", e);
        }

        return enumeratedValueInfoList;
    }

    /**
     * Initializes ATP term cache.
     * AtpSeasonalTypes rarely change, so fetch them all and store them in a Map.
     */
    private synchronized void initializeAtpTypesCache() {

        if (null == atpCache || atpCache.isEmpty()) {
            atpCache = new HashMap<String, String>();
            List<AtpTypeInfo> atpTypeInfos;
            try {
                atpTypeInfos = getAtpService().getAtpTypes();
            } catch (OperationFailedException e) {
                logger.error("ATP types lookup failed.", e);
                return;
            }
            for (AtpTypeInfo ti : atpTypeInfos) {
                atpCache.put(ti.getId(), ti.getName().substring(0, 1).toUpperCase() + ti.getName().substring(1));
            }
        }
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
}