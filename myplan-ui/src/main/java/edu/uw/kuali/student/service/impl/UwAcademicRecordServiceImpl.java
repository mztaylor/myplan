package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.enrollment.academicrecord.dto.GPAInfo;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.jws.WebParam;
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

    private StudentServiceClient studentServiceClient;
    private final static Logger logger = Logger.getLogger(UwAcademicRecordServiceImpl.class);
    private SAXReader reader = new SAXReader();
    ;

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        String responseText = null;
        List<StudentCourseRecordInfo> studentCourseRecordInfoList = new ArrayList<StudentCourseRecordInfo>();
        try {
            responseText = studentServiceClient.getAcademicRecords(personId);
        }
        catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Document document = null;
        try {
            document = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        DefaultXPath xpath = new DefaultXPath("//s:Enrollment");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("s", "http://webservices.washington.edu/student/");
        xpath.setNamespaceURIs(namespaces);
        String regId = null;
        List sections = xpath.selectNodes(document);
        List<String> queryString = new ArrayList<String>();
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
                        String calculatedGradeVal = dataSection.elementText("Grade");
                        String creditsEarned = dataSection.elementText("Credits");
                        String isRepeated=dataSection.elementText("RepeatCourse");
                        if(isRepeated.equalsIgnoreCase("true")){
                            studentCourseRecordInfo.setIsRepeated(true);
                        }
                        studentCourseRecordInfo.setCalculatedGradeValue(calculatedGradeVal);
                        studentCourseRecordInfo.setCreditsEarned(creditsEarned);
                        for (Object Section : dataList) {
                            Element section = (Element) Section;
                            String curriculumAbbreviation = section.elementText("CurriculumAbbreviation");
                            String courseNumber = section.elementText("CourseNumber");
                            StringBuffer courseCode = new StringBuffer();
                            courseCode = courseCode.append(curriculumAbbreviation).append(" ").append(courseNumber);
                            StringBuffer termName = new StringBuffer();
                            termName = termName.append("kuali.uw.atp.").append(section.elementText("Quarter")).append(section.elementText("Year"));
                            studentCourseRecordInfo.setCourseCode(courseCode.toString());
                            studentCourseRecordInfo.setTermName(termName.toString());
                            studentCourseRecordInfo.setPersonId(regId);
                            studentCourseRecordInfoList.add(studentCourseRecordInfo);
                        }
                    }

                }

            }
        }

        return studentCourseRecordInfoList;


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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
