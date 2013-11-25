package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.academicrecord.dto.*;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.r2.common.util.constants.LuServiceConstants;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;
import org.springframework.util.StringUtils;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/24/12
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class UwAcademicRecordServiceImpl implements AcademicRecordService {

    private final static Logger logger = Logger.getLogger(UwAcademicRecordServiceImpl.class);

    private StudentServiceClient studentServiceClient;

    private transient CluService luService;

    private static transient AcademicCalendarService academicCalendarService;

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    protected CluService getLuService() {
        if (this.luService == null) {
            this.luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return this.luService;
    }

    public void setLuService(CluService luService) {
        this.luService = luService;
    }

    private static AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }


    /**
     * This method returns a list of StudentCourseRecords for a
     * student and a term where each record is a course the student
     * attempted. The Term includes nested or sub-Terms.
     *
     * @param personId an Id of a student
     * @param termId   a key of a Term
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a list of StudentCourseRecords
     * @throws org.kuali.student.r2.common.exceptions.DoesNotExistException
     *          personId or termId not found
     * @throws org.kuali.student.r2.common.exceptions.InvalidParameterException
     *          invalid parameter
     * @throws org.kuali.student.r2.common.exceptions.MissingParameterException
     *          missing parameter
     * @throws org.kuali.student.r2.common.exceptions.OperationFailedException
     *          unable to complete request
     */
    @Override
    public List<StudentCourseRecordInfo> getAttemptedCourseRecordsForTerm(@WebParam(name = "personId") String personId, @WebParam(name = "termId") String termId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    /**
     * This method returns a list of StudentCourseRecord for a student
     * where each returned course is a course the student completed
     * for any term.
     *
     * @param personId an Id of a student
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a list of StudentCourseRecords
     * @throws DoesNotExistException     personId not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecords(@WebParam(name = "personId") String personId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        String enrollmentResponseText = null;
        Set<String> termsEnrolled = new HashSet<String>();
        Map<String, String> courseIds = new HashMap<String, String>();
        List<StudentCourseRecordInfo> studentCourseRecordInfoList = new ArrayList<StudentCourseRecordInfo>();
        List<TermInfo> planningTermInfo = null;
        try {
            enrollmentResponseText = studentServiceClient.getAcademicRecords(personId, null, null, null);

        } catch (ServiceException e) {
            throw new OperationFailedException("SWS query failed.", e);
        }


        /***************************Enrollment Section ********************************************/
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(new StringReader(enrollmentResponseText));
        } catch (Exception e) {
            throw new OperationFailedException("Could not parse reply from the Student Term Service.", e);
        }

        DefaultXPath xpath = new DefaultXPath("//s:Enrollment");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("s", "http://webservices.washington.edu/student/");
        xpath.setNamespaceURIs(namespaces);
        List sections = xpath.selectNodes(document);

        /*Implementation for populating the StudentCourseRecordInfo list*/
        if (sections != null) {
            StringBuffer cc = new StringBuffer();
            for (Object node : sections) {
                /*
               * Dividing each node in following format to get the data populated in the StudentCourseRecordInfo
               * Each node-->one Registrations Elements
               * Each Registrations--> Multiple Registration Elements
               * Each Registration--> one Section element , Grade, Credits
               * Each Section-->  CurriculumAbbreviation, CourseNumber , Quarter , Year
               * */

                Element registrationsSection = (Element) node;
                List<?> registrations = new ArrayList<Object>();

                registrations = registrationsSection.elements("Registrations");
                for (Object registration : registrations) {
                    Element registrationSection = (Element) registration;
                    List<?> registrationList = new ArrayList<Object>();
                    registrationList = registrationSection.elements("Registration");
                    for (Object data : registrationList) {
                        StudentCourseRecordInfo studentCourseRecordInfo = new StudentCourseRecordInfo();
                        Element dataSection = (Element) data;
                        List<?> dataList = new ArrayList<Object>();
                        dataList = dataSection.elements("Section");
                        String auditor = dataSection.elementText("Auditor");
                        if (auditor.equalsIgnoreCase("false")) {
                            Boolean isActive = Boolean.valueOf(dataSection.elementText("IsActive"));
                            if (isActive) {
                                String calculatedGradeVal = dataSection.elementText("Grade");
                                String creditsEarned = dataSection.elementText("Credits");
                                if (creditsEarned.contains(".")) {
                                    creditsEarned = creditsEarned.trim().substring(0, creditsEarned.lastIndexOf(".") - 1);
                                }
                                String isRepeated = dataSection.elementText("RepeatCourse");
                                if (isRepeated.equalsIgnoreCase("true")) {
                                    studentCourseRecordInfo.setIsRepeated(true);
                                } else {
                                    studentCourseRecordInfo.setIsRepeated(false);
                                }
                                studentCourseRecordInfo.setCalculatedGradeValue(calculatedGradeVal);
                                studentCourseRecordInfo.setCreditsEarned(creditsEarned.trim());
                                for (Object Section : dataList) {
                                    Element section = (Element) Section;
                                    String curriculumAbbreviation = section.elementText("CurriculumAbbreviation");
                                    String courseNumber = section.elementText("CourseNumber");
                                    String[] results = this.getCourseTitleAndId(curriculumAbbreviation, courseNumber);
                                    /*TODO: StudentCourseRecordInfo doesnot have a courseId property so using Id to set the course Id*/
                                    if (results.length > 0 && results[0] != null) {
                                        studentCourseRecordInfo.setId(results[0]);
                                    }
                                    if (results.length > 0 && results[1] != null) {
                                        studentCourseRecordInfo.setCourseTitle(results[1]);
                                    }
                                    StringBuffer courseCode = new StringBuffer();
                                    courseCode = courseCode.append(curriculumAbbreviation).append(" ").append(courseNumber);
                                    //  termName is really an ATP Id.
                                    String termName = AtpHelper.getAtpIdFromTermAndYear(section.elementText("Quarter"), section.elementText("Year"));
                                    /*Added this getTermsEnrolled() for checking in the registration section to see
                                    if the current and the future registered terms are covered up in this section.*/
                                    termsEnrolled.add(termName);
                                    studentCourseRecordInfo.setCourseCode(courseCode.toString());
                                    studentCourseRecordInfo.setTermName(termName);
                                    studentCourseRecordInfo.setPersonId(personId);
                                    studentCourseRecordInfo.setActivityCode(section.elementText("SectionID"));
                                    if (!courseIds.containsKey(studentCourseRecordInfo.getCourseCode())) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());
                                    } else if (courseIds.containsKey(studentCourseRecordInfo.getCourseCode()) && !courseIds.get(studentCourseRecordInfo.getCourseCode()).equalsIgnoreCase(termName)) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());

                                    } else if (courseIds.containsKey(studentCourseRecordInfo.getCourseCode()) && courseIds.get(studentCourseRecordInfo.getCourseCode()).equalsIgnoreCase(termName) && !studentCourseRecordInfo.getCreditsEarned().equals("0")) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /**************************************End of Enrollment Section******************************************************************************/

        /**************************************************Registration Section****************************************************/
        {
            List<String> registrationResponseTexts = null;
            try {
                registrationResponseTexts = getRegistrationResponseText(personId, termsEnrolled);
            } catch (OperationFailedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            for (String registrationResponseText : registrationResponseTexts) {
                Document registrationDocument = null;
                try {
                    SAXReader reader = new SAXReader();
                    registrationDocument = reader.read(new StringReader(registrationResponseText));
                } catch (DocumentException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                DefaultXPath regXpath = new DefaultXPath("//s:Registrations");
                Map<String, String> regNamespaces = new HashMap<String, String>();
                regNamespaces.put("s", "http://webservices.washington.edu/student/");
                regXpath.setNamespaceURIs(regNamespaces);
                List regSections = regXpath.selectNodes(registrationDocument);
                List<String> links = new ArrayList<String>();
                if (regSections != null) {
                    StringBuffer cc = new StringBuffer();
                    for (Object node : regSections) {
                        List<?> registrationList = new ArrayList<Object>();

                        Element registration = (Element) node;
                        registrationList = registration.elements("Registration");
                        for (Object data : registrationList) {
                            Element registrationLink = (Element) data;
                            String link = registrationLink.elementText("Href");
                            link = link.replace("/student", "");
                            links.add(link);
                        }

                    }
                }

                for (String link : links) {
                    String registration = null;
                    try {
                        registration = studentServiceClient.getAcademicRecords(personId, null, null, link);
                    } catch (ServiceException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                    Document document1 = null;
                    try {
                        SAXReader reader = new SAXReader();
                        document1 = reader.read(new StringReader(registration));
                    } catch (DocumentException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                    DefaultXPath regXpath1 = new DefaultXPath("//s:Registration");
                    Map<String, String> regNamespaces1 = new HashMap<String, String>();
                    regNamespaces1.put("s", "http://webservices.washington.edu/student/");
                    regXpath1.setNamespaceURIs(regNamespaces1);
                    List regSections1 = regXpath1.selectNodes(document1);
                    for (Object node : regSections1) {

                        StudentCourseRecordInfo studentCourseRecordInfo = new StudentCourseRecordInfo();
                        Element dataSection = (Element) node;
                        List<?> dataList = new ArrayList<Object>();
                        dataList = dataSection.elements("Section");
                        String auditor = dataSection.elementText("Auditor");
                        if (auditor.equalsIgnoreCase("false")) {
                            Boolean isActive = Boolean.valueOf(dataSection.elementText("IsActive"));
                            if (isActive) {
                                String calculatedGradeVal = dataSection.elementText("Grade");
                                String creditsEarned = dataSection.elementText("Credits");
                                if (creditsEarned.contains(".")) {
                                    creditsEarned = creditsEarned.trim().substring(0, creditsEarned.lastIndexOf(".") - 1);
                                }
                                String isRepeated = dataSection.elementText("RepeatCourse");
                                if (isRepeated.equalsIgnoreCase("true")) {
                                    studentCourseRecordInfo.setIsRepeated(true);
                                } else {
                                    studentCourseRecordInfo.setIsRepeated(false);
                                }
                                studentCourseRecordInfo.setCalculatedGradeValue(calculatedGradeVal);
                                studentCourseRecordInfo.setCreditsEarned(creditsEarned.trim());
                                for (Object Section : dataList) {
                                    Element section = (Element) Section;
                                    String curriculumAbbreviation = section.elementText("CurriculumAbbreviation");
                                    String courseNumber = section.elementText("CourseNumber");
                                    String[] results = this.getCourseTitleAndId(curriculumAbbreviation, courseNumber);
                                    /*TODO: StudentCourseRecordInfo does not have a courseId property so using Id to set the course Id*/
                                    if (results.length > 0 && results[0] != null) {
                                        studentCourseRecordInfo.setId(results[0]);
                                    }
                                    if (results.length > 0 && results[1] != null) {
                                        studentCourseRecordInfo.setCourseTitle(results[1]);
                                    }
                                    StringBuffer courseCode = new StringBuffer();
                                    courseCode = courseCode.append(curriculumAbbreviation).append(" ").append(courseNumber);
                                    //  termName is really an ATP Id.
                                    String termName = AtpHelper.getAtpIdFromTermAndYear(section.elementText("Quarter"), section.elementText("Year"));
                                    studentCourseRecordInfo.setCourseCode(courseCode.toString());
                                    studentCourseRecordInfo.setTermName(termName);
                                    studentCourseRecordInfo.setPersonId(personId);
                                    studentCourseRecordInfo.setActivityCode(section.elementText("SectionID"));


                                    if (!courseIds.containsKey(studentCourseRecordInfo.getCourseCode())) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());
                                    } else if (courseIds.containsKey(studentCourseRecordInfo.getCourseCode()) && !courseIds.get(studentCourseRecordInfo.getCourseCode()).equalsIgnoreCase(termName)) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());

                                    } else if (courseIds.containsKey(studentCourseRecordInfo.getCourseCode()) && courseIds.get(studentCourseRecordInfo.getCourseCode()).equalsIgnoreCase(termName) && !studentCourseRecordInfo.getCreditsEarned().equals("0")) {
                                        studentCourseRecordInfoList.add(studentCourseRecordInfo);
                                        courseIds.put(studentCourseRecordInfo.getCourseCode(), studentCourseRecordInfo.getTermName());
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }

        return studentCourseRecordInfoList;
    }

    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecordsForCourse(@WebParam(name = "personId") String personId, @WebParam(name = "courseId") String courseId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    private List<String> getRegistrationResponseText(String personId, Set<String> termsEnrolled) throws OperationFailedException {
        List<String> responseTexts = new ArrayList<String>();
        String[] currentTerm = AtpHelper.atpIdToTermAndYear(AtpHelper.getCurrentAtpId());

        for (int i = 0; i < 2; i++) {
            String registrationResponseText = null;
            if (currentTerm[0].equalsIgnoreCase(PlanConstants.ATP_TERM_4)) {
                currentTerm[0] = PlanConstants.ATP_TERM_1;
                currentTerm[1] = String.valueOf(Integer.parseInt(currentTerm[1]) + 1);
                AtpHelper.YearTerm yearTerm = new AtpHelper.YearTerm(Integer.parseInt(currentTerm[1].trim()), Integer.parseInt(currentTerm[0].trim()));
                String atpId = yearTerm.toATP();
                /*If the terms already are covered up in enrollment section then we skip this part */
                if (!termsEnrolled.contains(atpId)) {
                    try {
                        registrationResponseText = studentServiceClient.getAcademicRecords(personId, yearTerm.getYearAsString(), yearTerm.getTermAsID(), null);
                    } catch (ServiceException e) {
                        throw new OperationFailedException("SWS query failed.", e);
                    }
                    termsEnrolled.add(atpId);
                }
            } else {
                currentTerm[0] = String.valueOf(Integer.parseInt(currentTerm[0]) + 1);
                AtpHelper.YearTerm yearTerm = new AtpHelper.YearTerm(Integer.parseInt(currentTerm[1].trim()), Integer.parseInt(currentTerm[0].trim()));
                String atpId = yearTerm.toATP();
                /*If the terms already are covered up in enrollment section then we skip this part */
                if (!termsEnrolled.contains(atpId)) {
                    try {
                        registrationResponseText = studentServiceClient.getAcademicRecords(personId, yearTerm.getYearAsString(), yearTerm.getTermAsID(), null);
                    } catch (ServiceException e) {
                        throw new OperationFailedException("SWS query failed.", e);
                    }
                    termsEnrolled.add(atpId);
                }
            }
            if (StringUtils.hasText(registrationResponseText)) {
                responseTexts.add(registrationResponseText);
            }
        }

        return responseTexts;
    }

    /**
     * populate the courseTitle for studentCourseRecordInfo for course code
     *
     * @param subject
     * @param number
     * @return
     */
    private String[] getCourseTitleAndId(String subject, String number) {
        List<SearchRequestInfo> requests = new ArrayList<SearchRequestInfo>();
        SearchRequestInfo request = new SearchRequestInfo(CourseSearchConstants.COURSE_SEARCH_FOR_COURSE_ID);
        request.addParam("subject", subject);
        request.addParam("number", number);
        request.addParam("lastScheduledTerm", AtpHelper.getLastScheduledAtpId());
        requests.add(request);
        SearchResultInfo searchResult = new SearchResultInfo();

        try {
            searchResult = getLuService().search(request, PlanConstants.CONTEXT_INFO);
        } catch (MissingParameterException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }

        String results[] = new String[2];
        if (searchResult.getRows().size() > 0) {
            results[0] = searchResult.getRows().get(0).getCells().get(0).getValue();
            results[1] = searchResult.getRows().get(0).getCells().get(1).getValue();

        }
        return results;
    }

    /**
     * This method returns a list of StudentCourseRecord for a student
     * and a term where each returned course is a course the student
     * completed The Term includes nested or sub-Terms.
     *
     * @param personId an Id of a student
     * @param termId   a key of a Term
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a list of StudentCourseRecords
     * @throws DoesNotExistException     personId or termId not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecordsForTerm(@WebParam(name = "personId") String personId, @WebParam(name = "termId") String termId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    /**
     * This method returns the GPA of a student for all courses taken
     * within a given a Term including its sub-Terms.
     *
     * @param personId an Id of a student
     * @param termId   a key of a Term
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a GPA
     * @throws DoesNotExistException     personId, termId or
     *                                   calculationTypeKey not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public GPAInfo getGPAForTerm(@WebParam(name = "personId") String personId, @WebParam(name = "termId") String termId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    /**
     * This method returns the cumulative GPA of a student.
     *
     * @param personId an Id of a student
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a GPA
     * @throws DoesNotExistException     personId or calculationTypeKey
     *                                   not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public GPAInfo getCumulativeGPA(@WebParam(name = "personId") String personId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    @Override
    public GPAInfo getCumulativeGPAForProgram(@WebParam(name = "personId") String personId, @WebParam(name = "programId") String programId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public GPAInfo getCumulativeGPAForTermAndProgram(@WebParam(name = "personId") String personId, @WebParam(name = "programId") String programId, @WebParam(name = "termKey") String termKey, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public LoadInfo getLoadForTerm(@WebParam(name = "personId") String personId, @WebParam(name = "termId") String termId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<StudentProgramRecordInfo> getProgramRecords(@WebParam(name = "personId") String personId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<StudentCredentialRecordInfo> getAwardedCredentials(@WebParam(name = "personId") String personId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<StudentTestScoreRecordInfo> getTestScoreRecords(@WebParam(name = "personId") String personId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<StudentTestScoreRecordInfo> getTestScoreRecordsByType(@WebParam(name = "personId") String personId, @WebParam(name = "testTypeKey") String testTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * This method returns the number of credits a student earned by
     * courss within in a given Term including its sub-Terms.
     *
     * @param personId an Id of a student
     * @param termId   a key for a Term
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a number of credits represented by a string
     * @throws DoesNotExistException     personId, termId or
     *                                   calculationTypeKey not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public String getEarnedCreditsForTerm(@WebParam(name = "personId") String personId, @WebParam(name = "termId") String termId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    /**
     * This method returns the number of credits a student earned
     * across all terms.
     *
     * @param personId an Id of a student
     * @param context  Context information containing the principalId
     *                 and locale information about the caller of service
     *                 operation
     * @return a number of credits represented by a string
     * @throws DoesNotExistException     personId or calculationTypeKey
     *                                   not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public String getEarnedCredits(@WebParam(name = "personId") String personId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    @Override
    public String getEarnedCumulativeCreditsForProgramAndTerm(@WebParam(name = "personId") String personId, @WebParam(name = "programId") String programId, @WebParam(name = "termId") String termId, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }
}
