package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.myplan.util.CircularTermList;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.enrollment.acal.dto.*;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.class1.state.dto.StateInfo;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.springframework.util.StringUtils;

import javax.jws.WebParam;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UW implementation of AcademicCalendarService.
 */
public class UwAcademicCalendarServiceImpl implements AcademicCalendarService {
    private final static Logger logger = Logger.getLogger(UwAcademicCalendarServiceImpl.class);


    /**
     * The number of terms to inspect when looking for section data.
     */
    private static final short PUBLISHED_QUARTER_COUNT = 3;

    private static int CRITERIA_LENGTH = 23;

    private StudentServiceClient studentServiceClient;

    public UwAcademicCalendarServiceImpl() {
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    /**
     * Provides a list of terms which have published section info.
     *
     * @param processKey
     * @param context
     * @return A list of TermInfos in chronological order.
     */
    @Override
    public List<TermInfo> getCurrentTerms(@WebParam(name = "processKey") String processKey,
                                          @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Tests a String representation of an Integer ("0" or "1") for truth or falsity.
     *
     * @param value
     * @return
     */
    private boolean isTrue(String value) {
        boolean rVal = false;
        if (value.trim().equals("1")) {
            rVal = true;
        }
        return rVal;
    }

    @Override
    public List<TypeInfo> getAcademicCalendarTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getAcademicCalendarType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getAcademicCalendarState(@WebParam(name = "academicCalendarStateKey") String academicCalendarStateKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getAcademicCalendarStates(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcademicCalendarInfo getAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsByIds(@WebParam(name = "academicCalendarIds") List<String> academicCalendarIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getAcademicCalendarIdsByType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsByStartYear(@WebParam(name = "year") Integer year, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsForTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForAcademicCalendarIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> searchForAcademicCalendars(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateAcademicCalendar(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "academicCalendarInfo") AcademicCalendarInfo academicCalendarInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcademicCalendarInfo createAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "academicCalendarInfo") AcademicCalendarInfo academicCalendarInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcademicCalendarInfo updateAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "academicCalendarInfo") AcademicCalendarInfo academicCalendarInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcademicCalendarInfo copyAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public String getAcademicCalendarData(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "calendarDataFormatTypeKey") String calendarDataFormatTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getHolidayCalendarType(@WebParam(name = "holidayCalendarTypeKey") String holidayCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getHolidayCalendarTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getHolidayCalendarState(@WebParam(name = "holidayCalendarStateKey") String holidayCalendarStateKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getHolidayCalendarStates(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayCalendarInfo getHolidayCalendar(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayCalendarInfo> getHolidayCalendarsByIds(@WebParam(name = "holidayCalendarIds") List<String> holidayCalendarIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getHolidayCalendarIdsByType(@WebParam(name = "holidayCalendarTypeKey") String holidayCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayCalendarInfo> getHolidayCalendarsByStartYear(@WebParam(name = "year") Integer year, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForHolidayCalendarIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayCalendarInfo> searchForHolidayCalendars(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateHolidayCalendar(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "holidayCalendarTypeKey") String holidayCalendarTypeKey, @WebParam(name = "holidayCalendarInfo") HolidayCalendarInfo holidayCalendarInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayCalendarInfo createHolidayCalendar(@WebParam(name = "holidayCalendarTypeKey") String holidayCalendarTypeKey, @WebParam(name = "holidayCalendarInfo") HolidayCalendarInfo holidayCalendarInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayCalendarInfo copyHolidayCalendar(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayCalendarInfo updateHolidayCalendar(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "holidayCalendarInfo") HolidayCalendarInfo holidayCalendarInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteHolidayCalendar(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypesForAcademicCalendarType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypesForTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getTermState(@WebParam(name = "termStateKey") String termStateKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getTermStates(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo getTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getTermsByIds(@WebParam(name = "termIds") List<String> termIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getTermIdsByType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getTermsByCode(@WebParam(name = "code") String code, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getTermsForAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getIncludedTermsInTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getContainingTerms(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForTermIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> searchForTerms(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        Predicate p = criteria.getPredicate();
        String str = p.toString();
        str = str.substring(CRITERIA_LENGTH, str.length() - 1);
        String campus = null;
        if (str.contains("|")) {
            String[] arr = str.split(PlanConstants.CODE_KEY_SEPARATOR);
            str = arr[0];
            campus = OrgHelper.getCampusName(arr[1]);
        }

        List<TermInfo> termInfos = new ArrayList<TermInfo>();

        /*
         *  Query the student web service for the current term. If the last drop date has passed
         *  then make the next term the current term.
         */
        String responseText = null;
        try {
            responseText = studentServiceClient.getCurrentTerm();
        } catch (ServiceException e) {
            throw new RuntimeException("Query to Student Term Service failed.", e);
        }

        Document termDocument = null;
        try {
            SAXReader reader = new SAXReader();
            termDocument = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        String lastAddDayDateString = termDocument.getRootElement().element("LastAddDay").getTextTrim();
        String currentTerm = termDocument.getRootElement().element("Quarter").getTextTrim();
        String year = termDocument.getRootElement().element("Year").getTextTrim();

        if (str.equalsIgnoreCase(PlanConstants.INPROGRESS)) {
            TermInfo termInfo = new TermInfo();
            String atp = AtpHelper.getAtpIdFromTermAndYear(currentTerm, year);
            termInfo.setId(atp);
            termInfo.setName(currentTerm + " " + year);
            termInfos.add(termInfo);

            List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();

            AttributeInfo lastAddAttr = new AttributeInfo();
            lastAddAttr.setKey(AtpHelper.LAST_ADD_DAY);
            lastAddAttr.setValue(lastAddDayDateString);

            AttributeInfo priorityOneRegStAttr = new AttributeInfo();
            priorityOneRegStAttr.setKey(AtpHelper.PRIORITY_ONE_REGISTRATION_START);
            priorityOneRegStAttr.setValue(getPriorityOneRegistrationStartDate(termDocument));

            AttributeInfo priorityOneRegEndAttr = new AttributeInfo();
            priorityOneRegEndAttr.setKey(AtpHelper.PRIORITY_ONE_REGISTRATION_END);
            priorityOneRegEndAttr.setValue(getPriorityOneRegistrationEndDate(termDocument));

            attributes.add(lastAddAttr);
            attributes.add(priorityOneRegStAttr);
            attributes.add(priorityOneRegEndAttr);
            termInfo.setAttributes(attributes);
        }
        if (str.equalsIgnoreCase(PlanConstants.PUBLISHED) || str.equalsIgnoreCase(PlanConstants.PLANNING)) {
            CircularTermList ccl = new CircularTermList(currentTerm, Integer.valueOf(year));

            DateTime lastDropDay = new DateTime(lastAddDayDateString);

            logger.info(String.format("The Student Term Service reports [%s/%s] [%s] is the current term with last add day [%s].",
                    ccl.getQuarterName(), ccl.getQuarterNumber(), ccl.getYear(), lastAddDayDateString));

            /*
            *  If the last drop day has passed then increment to the next term.
            */
            if (lastDropDay.isBeforeNow()) {
                ccl.incrementQuarter();
                logger.info(String.format("The last drop day has passed, so the current term has been incremented to [%s/%s] [%s].",
                        ccl.getQuarterName(), ccl.getQuarterNumber(), ccl.getYear()));
            }

            /*
            *  Query the term service and determine which terms are published.
            */

                for (short i = 0; i < PUBLISHED_QUARTER_COUNT; i++) {
                    try {
                        logger.info(String.format("Querying the Student Term Service for quarter [%s/%s] year [%s] to determine if section information as been published.",
                                ccl.getQuarterName(), ccl.getQuarterNumber(), ccl.getYear()));
                        responseText = studentServiceClient.getTermInfo(String.valueOf(ccl.getYear()), ccl.getQuarterName());
                    } catch (ServiceException e) {
                        logger.error("Call to Student Term Service failed.", e);
                        break;
                    }

                    Document sectionDocument = null;
                    try {
                        SAXReader reader = new SAXReader();
                        termDocument = reader.read(new StringReader(responseText));
                    } catch (Exception e) {
                        logger.error("Could not parse reply from the Student Term Service.", e);
                        break;
                    }

                    //  If the time schedule for any campus is published then create a TermInfo and continue.
                    //  Otherwise, stop processing.
                    Element timeSchedulePublished = termDocument.getRootElement().element("TimeSchedulePublished");
                    if ((StringUtils.hasText(campus) && isTrue(timeSchedulePublished.element(campus).getText())) || (!StringUtils.hasText(campus) && (isTrue(timeSchedulePublished.element("Bothell").getText())
                            || isTrue(timeSchedulePublished.element("Seattle").getText())
                            || isTrue(timeSchedulePublished.element("Tacoma").getText())))) {

                        String lastDropDayStr = termDocument.getRootElement().element("LastDropDay").getTextTrim();

                        TermInfo ti = new TermInfo();
                        //  Create the ATP ID.
                    ti.setId(ccl.getYear() + ccl.getQuarterNumber());
                        ti.setName(ccl.getQuarterName() + " " + ccl.getYear());

                        List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();
                        AttributeInfo lastDropAttr = new AttributeInfo();
                        lastDropAttr.setKey(AtpHelper.LAST_ADD_DAY);
                        lastDropAttr.setValue(lastDropDayStr);

                        AttributeInfo priorityOneRegStAttr = new AttributeInfo();
                        priorityOneRegStAttr.setKey(AtpHelper.PRIORITY_ONE_REGISTRATION_START);
                        priorityOneRegStAttr.setValue(getPriorityOneRegistrationStartDate(termDocument));

                        AttributeInfo priorityOneRegEndAttr = new AttributeInfo();
                        priorityOneRegEndAttr.setKey(AtpHelper.PRIORITY_ONE_REGISTRATION_END);
                        priorityOneRegEndAttr.setValue(getPriorityOneRegistrationEndDate(termDocument));

                        attributes.add(lastDropAttr);
                        attributes.add(priorityOneRegStAttr);
                        attributes.add(priorityOneRegEndAttr);
                        ti.setAttributes(attributes);

                        termInfos.add(ti);
                    if (str.equalsIgnoreCase(PlanConstants.PLANNING)) {
                        break;
                    }

                        ccl.incrementQuarter();
                    } else {
                        break;
                    }

                }
        }
        return termInfos;

    }

    @Override
    public List<ValidationResultInfo> validateTerm(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo createTerm(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo updateTerm(@WebParam(name = "termId") String termId, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo addTermToAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo removeTermFromAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo addTermToTerm(@WebParam(name = "termId") String termId, @WebParam(name = "includedTermId") String includedTermId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo removeTermFromTerm(@WebParam(name = "termId") String termId, @WebParam(name = "includedTermId") String includedTermId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getKeyDateType(@WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getKeyDateTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getKeyDateTypesForTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getKeyDateState(@WebParam(name = "keyDateStateKey") String keyDateStateKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getKeyDateStates(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo getKeyDate(@WebParam(name = "keyDateId") String keyDateId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesByIds(@WebParam(name = "keyDateIds") List<String> keyDateIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getKeyDateIdsByType(@WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getKeyDateIdsByTypeForTerm(@WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForTermByDate(@WebParam(name = "termId") String termId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getImpactedKeyDates(@WebParam(name = "keyDateId") String keyDateId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForKeyDateIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> searchForKeyDates(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateKeyDate(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "termId") String termId, @WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo createKeyDate(@WebParam(name = "termId") String termId, @WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo updateKeyDate(@WebParam(name = "keyDateId") String keyDateId, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteKeyDate(@WebParam(name = "keyDateId") String keyDateId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo calculateKeyDate(@WebParam(name = "keyDateId") String keyDateId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getAcalEventType(@WebParam(name = "acalEventTypeKey") String acalEventTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getAcalEventTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getAcalEventTypesForAcademicCalendarType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getAcalEventState(@WebParam(name = "acalEventStateKey") String acalEventStateKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getAcalEventStates(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcalEventInfo getAcalEvent(@WebParam(name = "acalEventId") String acalEventId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcalEventInfo> getAcalEventsByIds(@WebParam(name = "acalEventIds") List<String> acalEventIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getAcalEventIdsByType(@WebParam(name = "acalEventTypeKey") String acalEventTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcalEventInfo> getAcalEventsForAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcalEventInfo> getAcalEventsForAcademicCalendarByDate(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcalEventInfo> getImpactedAcalEvents(@WebParam(name = "acalEventId") String acalEventId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForAcalEventIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcalEventInfo> searchForAcalEvents(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateAcalEvent(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "termId") String termId, @WebParam(name = "acalEventTypeKey") String acalEventTypeKey, @WebParam(name = "acalEventInfo") AcalEventInfo acalEventInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcalEventInfo createAcalEvent(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "acalEventTypeKey") String acalEventTypeKey, @WebParam(name = "acalEventInfo") AcalEventInfo acalEventInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcalEventInfo updateAcalEvent(@WebParam(name = "acalEventId") String acalEventId, @WebParam(name = "acalEventInfo") AcalEventInfo acalEventInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteAcalEvent(@WebParam(name = "acalEventId") String acalEventId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcalEventInfo calculateAcalEvent(@WebParam(name = "acalEventId") String acalEventId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getHolidayType(@WebParam(name = "holidayTypeKey") String holidayTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getHolidayTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getHolidayTypesForHolidayCalendarType(@WebParam(name = "holidayCalendarTypeKey") String holidayCalendarTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getHolidayState(@WebParam(name = "holidayStateKey") String holidayStateKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getHolidayStates(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo getHoliday(@WebParam(name = "holidayId") String holidayId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getHolidaysByIds(@WebParam(name = "holidayIds") List<String> holidayIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getHolidayIdsByType(@WebParam(name = "holidayTypeKey") String holidayTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getHolidaysForHolidayCalendar(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getHolidaysForHolidayCalendarByDate(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getHolidaysByDateForAcademicCalendar(@WebParam(name = "academicCalendarId") String academicCalendarId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getImpactedHolidays(@WebParam(name = "holidayId") String holidayId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForHolidayIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> searchForHolidays(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateHoliday(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "holidayTypeKey") String holidayTypeKey, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo createHoliday(@WebParam(name = "holidayCalendarId") String holidayCalendarId, @WebParam(name = "holidayTypeKey") String holidayTypeKey, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo updateHoliday(@WebParam(name = "holidayId") String holidayId, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteHoliday(@WebParam(name = "holidayId") String holidayId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo calculateHoliday(@WebParam(name = "holidayId") String holidayId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Integer getInstructionalDaysForTerm(@WebParam(name = "termId") String termId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }


    /**
     * Calculates and returns the start date of priority 1 registration for the provided term
     *
     * @param termDocument
     * @return
     */
    private String getPriorityOneRegistrationStartDate(Document termDocument) {
        Element registrationPeriods = termDocument.getRootElement().element("RegistrationPeriods");
        String earliestStartRegistrationDateStr = null;
        DateTime earliestStartRegistrationDate = null;
        for (Element registrationPeriod : (List<Element>) registrationPeriods.elements()) {
            String startDateStr = registrationPeriod.element("StartDate").getTextTrim();
            if (null == earliestStartRegistrationDateStr) {
                earliestStartRegistrationDateStr = startDateStr;
                earliestStartRegistrationDate = new DateTime(earliestStartRegistrationDateStr);
            } else {
                DateTime startDt = new DateTime(startDateStr);
                if (startDt.isBefore(earliestStartRegistrationDate)) {
                    earliestStartRegistrationDate = startDt;
                    earliestStartRegistrationDateStr = startDateStr;
                }
            }
        }

        return (null == earliestStartRegistrationDateStr) ? "" : earliestStartRegistrationDateStr;
    }

    /**
     * Calculates and returns the start date of priority 1 registration for the provided term
     *
     * @param termDocument
     * @return
     */
    private String getPriorityOneRegistrationEndDate(Document termDocument) {
        Element registrationPeriods = termDocument.getRootElement().element("RegistrationPeriods");
        String endRegistrationDateStr = null;
        DateTime endRegistrationDate = null;
        for (Element registrationPeriod : (List<Element>) registrationPeriods.elements()) {
            String endDateStr = registrationPeriod.element("EndDate").getTextTrim();
            if (null == endRegistrationDateStr) {
                endRegistrationDateStr = endDateStr;
                endRegistrationDate = new DateTime(endRegistrationDateStr);
            } else {
                DateTime endDt = new DateTime(endDateStr);
                if (endDt.isAfter(endRegistrationDate)) {
                    endRegistrationDate = endDt;
                    endRegistrationDateStr = endDateStr;
                }
            }
        }

        return (null == endRegistrationDateStr) ? "" : endRegistrationDateStr;
    }
}
