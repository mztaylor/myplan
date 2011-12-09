package org.kuali.student.myplan.course.service;

import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.enrollment.acal.dto.*;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.r2.common.datadictionary.dto.DictionaryEntryInfo;
import org.kuali.student.r2.common.dto.*;
import org.kuali.student.r2.common.exceptions.*;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UW implementation of AcademicCalendarService.
 */
public class UwAcademicCalendarServiceImpl implements AcademicCalendarService {
    @Override
    public List<TermInfo> getCurrentTerms(@WebParam(name = "processKey") String processKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<TermInfo> termInfos = new ArrayList<TermInfo>();

        TermInfo ti1 = new TermInfo();
        ti1.setName("Autumn 11");

        TermInfo ti2 = new TermInfo();
        ti2.setName("Winter 12");

        TermInfo ti3 = new TermInfo();
        ti3.setName("Spring 12");

        termInfos.add(ti1);
        termInfos.add(ti2);
        termInfos.add(ti3);

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
