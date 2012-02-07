package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.myplan.util.CircularTermList;
import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.enrollment.acal.dto.*;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.r2.common.datadictionary.dto.DictionaryEntryInfo;
import org.kuali.student.r2.common.dto.*;
import org.kuali.student.r2.common.exceptions.*;

import org.apache.log4j.Logger;
import org.kuali.student.r2.core.state.dto.StateInfo;
import org.kuali.student.r2.core.type.dto.TypeInfo;

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
    private static final String DEFAULT_CURRICULUM = "chem";
    private static final String TERM_KEY_PREFIX = "kuali.uw.atp.";

    private SAXReader reader;

    private StudentServiceClient studentServiceClient;

    public UwAcademicCalendarServiceImpl() {
        this.reader = new SAXReader();
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

        List<TermInfo> termInfos = new ArrayList<TermInfo>();

        /*
         *  Estimate the current term.
         */
        DateTime now = new DateTime();
        String currentTerm = null;
        int month = now.getMonthOfYear();
        switch (month) {
            case 1: case 2: case 3:
                currentTerm = "Winter";
                break;
            case 4: case 5: case 6:
                currentTerm = "Spring";
                break;
            case 7: case 8: case 9:
                currentTerm = "Summer";
                break;
            case 10: case 11: case 12:
                currentTerm = "Autumn";
                break;
        }

        String year = String.valueOf(now.getYear());
        logger.info(String.format("Current estimated as: %s %s", currentTerm, year));
        //  Go ahead and initialize the term list.
        CircularTermList ccl = new CircularTermList(currentTerm, now.getYear());

        /*
         *  Query the student term service to get the last drop date.
         */
        String responseText = null;
        try {
            responseText = studentServiceClient.getTermInfo(year, currentTerm);
        } catch (ServiceException e) {
            throw new RuntimeException("Query to Student Term Service failed.", e);
        }

        Document termDocument = null;
        try {
            termDocument = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        String lastDropDayDateString =  termDocument.getRootElement().element("LastDropDay").getTextTrim();
        DateTime lastDropDay = new DateTime(lastDropDayDateString);

        logger.info(String.format("Last drop day for %s %s is: %s",
            ccl.getQuarter(), ccl.getYear(), lastDropDayDateString));

        /*
         *  If the last drop day has passed then increment to the next term.
         */
        if (lastDropDay.isBeforeNow()) {
            logger.info("The last drop day has passed. Incrementing to the next term.");
            ccl.incrementQuarter();
        }

        /*
         *  Query the section service for available calendar info.
         *  Stop processing if any service exceptions are encountered.
         */
        for (short i = 0; i < PUBLISHED_QUARTER_COUNT; i++) {
            responseText = null;
            try {
                logger.info(String.format("Querying the Student Section Service for: %s - %s %s",
                    DEFAULT_CURRICULUM, ccl.getQuarter(), ccl.getYear()));
                responseText = studentServiceClient.getSectionInfo(String.valueOf(ccl.getYear()), ccl.getQuarter(), DEFAULT_CURRICULUM);
            } catch (ServiceException e) {
                logger.error("Call to Student Section Service failed.", e);
                break;
            }

            Document sectionDocument = null;
            try {
                termDocument = reader.read(new StringReader(responseText));
            } catch (Exception e) {
                logger.error("Could not parse reply from the Student Section Service.", e);
                break;
            }

            TermInfo ti = new TermInfo();
            ti.setId(TERM_KEY_PREFIX + ccl.getQuarter().toLowerCase() + ccl.getYear());
            ti.setName(ccl.getQuarter() + " " + ccl.getYear());
            termInfos.add(ti);

            ccl.incrementQuarter();
        }

        return termInfos;
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
        throw new RuntimeException("Not implemented.");
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

}
