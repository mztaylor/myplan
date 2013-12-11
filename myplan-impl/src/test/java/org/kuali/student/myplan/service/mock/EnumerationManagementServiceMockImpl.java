package org.kuali.student.myplan.service.mock;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.class1.enumerationmanagement.model.EnumeratedValueEntity;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.r2.core.enumerationmanagement.dto.EnumerationInfo;
import org.kuali.student.r2.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.dto.SearchResultRowInfo;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 12/10/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnumerationManagementServiceMockImpl implements EnumerationManagementService {

    private static String[] enumeratedValues = new String[]{"uw.academicplan.placeholder.elective|Elective|General Elective|uw.academicplan.placeholder",
            "uw.academicplan.placeholder.studyabroad|Study Abroad|Study Abroad|uw.academicplan.placeholder",
            "uw.academicplan.placeholder.foreignlanguage|FL|Foreign Language|uw.academicplan.placeholder",
            "uw.academicplan.placeholder.other|Other|Other|uw.academicplan.placeholder",
            "course.genedrequirement.qsr_ind|QSR|Quantitative and Symbolic Reasoning|uw.course.genedrequirement",
            "course.genedrequirement.aofk.vlpa_ind|VLPA|Visual, Literary, and Performing Arts|uw.course.genedrequirement",
            "course.genedrequirement.aofk.is_ind|I&S|Individuals and Societies|uw.course.genedrequirement",
            "course.genedrequirement.englishcomp_ind|C|English Composition|uw.course.genedrequirement",
            "course.genedrequirement.writing_ind|W|Additional Writing|uw.course.genedrequirement",
            "course.genedrequirement.aofk.nw_ind|NW|The Natural World|uw.course.genedrequirement"};

    @Override
    public List<EnumerationInfo> getEnumerations(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EnumerationInfo getEnumeration(@WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EnumeratedValueInfo> getEnumeratedValues(@WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "contextTypeKey") String contextTypeKey, @WebParam(name = "contextValue") String contextValue, @WebParam(name = "contextDate") Date contextDate, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<EnumeratedValueInfo> enumeratedValueInfoList = new ArrayList<EnumeratedValueInfo>();
        for (String enumValue : enumeratedValues) {
            String[] values = enumValue.split("\\|");
            if (values[3].equals(enumerationKey)) {
                EnumeratedValueInfo enumeratedValueInfo = new EnumeratedValueInfo();
                enumeratedValueInfo.setCode(values[0]);
                enumeratedValueInfo.setAbbrevValue(values[1]);
                enumeratedValueInfo.setValue(values[2]);
                enumeratedValueInfo.setEnumerationKey(values[3]);
                enumeratedValueInfoList.add(enumeratedValueInfo);
            }
        }
        return enumeratedValueInfoList;
    }

    @Override
    public List<ValidationResultInfo> validateEnumeratedValue(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "code") String code, @WebParam(name = "enumeratedValueInfo") EnumeratedValueInfo enumeratedValueInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EnumeratedValueInfo updateEnumeratedValue(@WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "code") String code, @WebParam(name = "enumeratedValueInfo") EnumeratedValueInfo enumeratedValueInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteEnumeratedValue(@WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "code") String code, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EnumeratedValueInfo addEnumeratedValue(@WebParam(name = "enumerationKey") String enumerationKey, @WebParam(name = "code") String code, @WebParam(name = "enumeratedValueInfo") EnumeratedValueInfo enumeratedValueInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getSearchTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequestInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws MissingParameterException, InvalidParameterException, OperationFailedException, PermissionDeniedException {
        SearchResultInfo searchResult = new SearchResultInfo();
        for (String enumValue : enumeratedValues) {
            String[] values = enumValue.split("\\|");
            SearchResultRowInfo row = new SearchResultRowInfo();
            row.addCell("enumeration.resultColumn.code", values[0]);
            row.addCell("enumeration.resultColumn.abbrevValue", values[1]);
            row.addCell("enumeration.resultColumn.value", values[2]);
            row.addCell("enumeration.resultColumn.enumKey", values[3]);
            searchResult.getRows().add(row);
        }
        return searchResult;

    }
}
