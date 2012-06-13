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
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.enrollment.academicrecord.dto.GPAInfo;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private transient LuService luService;

    private static transient AcademicCalendarService academicCalendarService;

    private SAXReader reader = new SAXReader();

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    public void setLuService(LuService luService) {
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
                            String calculatedGradeVal = dataSection.elementText("Grade");
                            String creditsEarned = dataSection.elementText("Credits");
                            if (creditsEarned.contains(".")) {
                                creditsEarned = creditsEarned.trim().substring(0, creditsEarned.lastIndexOf(".") - 1);
                            }
                            String isRepeated = dataSection.elementText("RepeatCourse");
                            if (isRepeated.equalsIgnoreCase("true")) {
                                studentCourseRecordInfo.setIsRepeated(true);
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
                                studentCourseRecordInfo.setCourseCode(courseCode.toString());
                                studentCourseRecordInfo.setTermName(termName);
                                studentCourseRecordInfo.setPersonId(personId);
                                studentCourseRecordInfoList.add(studentCourseRecordInfo);
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
                registrationResponseTexts = getRegistrationResponseText(personId);
            } catch (OperationFailedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            for (String registrationResponseText : registrationResponseTexts) {
                Document registrationDocument = null;
                try {
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
                            String calculatedGradeVal = dataSection.elementText("Grade");
                            String creditsEarned = dataSection.elementText("Credits");
                            if (creditsEarned.contains(".")) {
                                creditsEarned = creditsEarned.trim().substring(0, creditsEarned.lastIndexOf(".") - 1);
                            }
                            String isRepeated = dataSection.elementText("RepeatCourse");
                            if (isRepeated.equalsIgnoreCase("true")) {
                                studentCourseRecordInfo.setIsRepeated(true);
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
                                studentCourseRecordInfoList.add(studentCourseRecordInfo);
                            }
                        }
                    }


                }
            }
        }

        return studentCourseRecordInfoList;
    }

    private List<String> getRegistrationResponseText(String personId) throws OperationFailedException {
        List<String> responseTexts = new ArrayList<String>();
        String[] currentTerm = AtpHelper.atpIdToTermAndYear(AtpHelper.getCurrentAtpId());
        for (int i = 0; i < 2; i++) {
            String registrationResponseText = null;
            if (currentTerm[0].equalsIgnoreCase("4")) {
                currentTerm[0] = "1";
                currentTerm[1] = String.valueOf(Integer.parseInt(currentTerm[1]) + 1);
                try {
                    registrationResponseText = studentServiceClient.getAcademicRecords(personId, currentTerm[1].trim(), currentTerm[0].trim(), null);
                } catch (ServiceException e) {
                    throw new OperationFailedException("SWS query failed.", e);
                }

            } else {
                currentTerm[0] = String.valueOf(Integer.parseInt(currentTerm[0]) + 1);
                try {
                    registrationResponseText = studentServiceClient.getAcademicRecords(personId, currentTerm[1].trim(), currentTerm[0].trim(), null);
                } catch (ServiceException e) {
                    throw new OperationFailedException("SWS query failed.", e);
                }
            }
            responseTexts.add(registrationResponseText);
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
        List<SearchRequest> requests = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest("myplan.course.getCourseTitleAndId");
        request.addParam("subject", subject);
        request.addParam("number", number);
        requests.add(request);
        SearchResult searchResult = new SearchResult();
        try {
            searchResult = getLuService().search(request);
        } catch (org.kuali.student.common.exceptions.MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
     * This method returns the GPA of a student for the period of time
     * indicated by the given Academic Calendar.
     *
     * @param personId            an Id of a student
     * @param academicCalendarKey a key of an Academic Calendar
     * @param context             Context information containing the principalId
     *                            and locale information about the caller of service
     *                            operation
     * @return a GPA
     * @throws DoesNotExistException     personId or academicCalendarKey not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public GPAInfo getGPAForAcademicCalendar(@WebParam(name = "personId") String personId, @WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
     * This method returns the number of credits a student earned in a
     * given Academic Calendar.
     *
     * @param personId            an Id of a student
     * @param academicCalendarKey a key for an AcademicCalendar
     * @param context             Context information containing the principalId
     *                            and locale information about the caller of service
     *                            operation
     * @return a number of credits represented by a string
     * @throws DoesNotExistException     personId, academicCalendarKey or
     *                                   calculationTypeKey not found
     * @throws InvalidParameterException invalid parameter
     * @throws MissingParameterException missing parameter
     * @throws OperationFailedException  unable to complete request
     */
    @Override
    public String getEarnedCreditsForAcademicCalendar(@WebParam(name = "personId") String personId, @WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "calculationTypeKey") String calculationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
}
