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
            ti.setKey(TERM_KEY_PREFIX + ccl.getQuarter().toLowerCase() + ccl.getYear());
            ti.setName(ccl.getQuarter() + " " + ccl.getYear());
            termInfos.add(ti);

            ccl.incrementQuarter();
        }

        return termInfos;
    }

    @Override
    public TypeInfo getAcademicCalendarType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getAcademicCalendarTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
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
    public List<AcademicCalendarInfo> getAcademicCalendarsByKeyList(@WebParam(name = "academicCalendarKeyList") List<String> academicCalendarKeyList, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getAcademicCalendarKeysByType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsByStartYear(@WebParam(name = "year") Integer year, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsByCredentialProgramType(@WebParam(name = "credentialProgramTypeKey") String credentialProgramTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> getAcademicCalendarsByCredentialProgramTypeForStartYear(@WebParam(name = "credentialProgramTypeKey") String credentialProgramTypeKey, @WebParam(name = "year") Integer year, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForAcademicCalendarKeys(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<AcademicCalendarInfo> searchForAcademicCalendars(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateAcademicCalendar(@WebParam(name = "validationType") String validationType, @WebParam(name = "academicCalendarInfo") AcademicCalendarInfo academicCalendarInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public AcademicCalendarInfo createAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "academicCalendarInfo") AcademicCalendarInfo academicCalendarInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
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
    public AcademicCalendarInfo copyAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "newAcademicCalendarKey") String newAcademicCalendarKey, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public String getAcademicCalendarData(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "calendarDataFormatTypeKey") String calendarDataFormatTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getCampusCalendarType(@WebParam(name = "campusCalendarTypeKey") String campusCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getCampusCalendarTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getCampusCalendarState(@WebParam(name = "campusCalendarStateKey") String campusCalendarStateKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getCampusCalendarStates(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CampusCalendarInfo getCampusCalendar(@WebParam(name = "campusCalendarKey") String campusCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CampusCalendarInfo> getCampusCalendarsByKeyList(@WebParam(name = "campusCalendarKeyList") List<String> campusCalendarKeyList, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getCampusCalendarKeysByType(@WebParam(name = "campusCalendarTypeKey") String campusCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CampusCalendarInfo> getCampusCalendarsByStartYear(@WebParam(name = "year") Integer year, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForCampusCalendarKeys(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CampusCalendarInfo> searchForCampusCalendars(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCampusCalendar(@WebParam(name = "validationType") String validationType, @WebParam(name = "campusCalendarInfo") CampusCalendarInfo campusCalendarInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CampusCalendarInfo createCampusCalendar(@WebParam(name = "campusCalendarKey") String campusCalendarKey, @WebParam(name = "campusCalendarInfo") CampusCalendarInfo campusCalendarInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CampusCalendarInfo updateCampusCalendar(@WebParam(name = "campusCalendarKey") String campusCalendarKey, @WebParam(name = "campusCalendarInfo") CampusCalendarInfo campusCalendarInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCampusCalendar(@WebParam(name = "campusCalendarKey") String campusCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypesForAcademicCalendarType(@WebParam(name = "academicCalendarTypeKey") String academicCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getTermTypesForTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StateInfo getTermState(@WebParam(name = "termStateKey") String termStateKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StateInfo> getTermStates(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo getTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getTermsByKeyList(@WebParam(name = "termKeyList") List<String> termKeyList, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getTermKeysByType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getTermsForAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getIncludedTermsInTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> getContainingTerms(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForTermKeys(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TermInfo> searchForTerms(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateTerm(@WebParam(name = "validationType") String validationType, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo createTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TermInfo updateTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "termInfo") TermInfo termInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo addTermToAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo removeTermFromAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo addTermToTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "includedTermKey") String includedTermKey, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo removeTermFromTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "includedTermKey") String includedTermKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getKeyDateType(@WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getKeyDateTypesForTermType(@WebParam(name = "termTypeKey") String termTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo getKeyDate(@WebParam(name = "keyDateKey") String keyDateKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesByKeyList(@WebParam(name = "keyDateKeyList") List<String> keyDateKeyList, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getKeyDateKeysByType(@WebParam(name = "keyDateTypeKey") String keyDateTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForAcademicCalendarByDate(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForTermByDate(@WebParam(name = "termKey") String termKey, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getAllKeyDatesForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> getKeyDatesForAllTermsByDate(@WebParam(name = "termKey") String termKey, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForKeyDateKeys(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<KeyDateInfo> searchForKeyDates(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateKeyDate(@WebParam(name = "validationType") String validationType, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo createKeyDateForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "keyDateKey") String keyDateKey, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public KeyDateInfo updateKeyDate(@WebParam(name = "keyDateKey") String keyDateKey, @WebParam(name = "keyDateInfo") KeyDateInfo keyDateInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteKeyDate(@WebParam(name = "keyDateKey") String keyDateKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getHolidayType(@WebParam(name = "holidayTypeKey") String holidayTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getHolidayTypesForCampusCalendarType(@WebParam(name = "campusCalendarTypeKey") String campusCalendarTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> getHolidaysForAcademicCalendar(@WebParam(name = "academicCalendarKey") String academicCalendarKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForHolidayKeys(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<HolidayInfo> searchForHolidays(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateHoliday(@WebParam(name = "validationType") String validationType, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo createHolidayForCampusCalendar(@WebParam(name = "campusCalendarKey") String campusCalendarKey, @WebParam(name = "holidayKey") String holidayKey, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public HolidayInfo updateHoliday(@WebParam(name = "holidayKey") String holidayKey, @WebParam(name = "holidayInfo") HolidayInfo holidayInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteHoliday(@WebParam(name = "holidayKey") String holidayKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationDateGroupInfo getRegistrationDateGroup(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateRegistrationDateGroup(@WebParam(name = "validationType") String validationType, @WebParam(name = "registrationDateGroupInfo") RegistrationDateGroupInfo registrationDateGroupInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationDateGroupInfo updateRegistrationDateGroup(@WebParam(name = "termKey") String termKey, @WebParam(name = "registrationDateGroupInfo") RegistrationDateGroupInfo registrationDateGroupInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public Integer getInstructionalDaysForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getDataDictionaryEntryKeys(@WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public DictionaryEntryInfo getDataDictionaryEntry(@WebParam(name = "entryKey") String entryKey, @WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException, DoesNotExistException {
         throw new RuntimeException("Not implemented.");
    }
}
