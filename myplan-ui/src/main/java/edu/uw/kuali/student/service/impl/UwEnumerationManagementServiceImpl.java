package edu.uw.kuali.student.service.impl;

import org.apache.log4j.Logger;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.core.class1.enumerationmanagement.model.EnumeratedValueEntity;
import org.kuali.student.r2.core.class1.enumerationmanagement.service.impl.EnumerationManagementServiceImpl;
import org.kuali.student.r2.core.search.dto.SearchParamInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.dto.SearchResultRowInfo;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 10/11/13
 * Time: 9:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class UwEnumerationManagementServiceImpl extends EnumerationManagementServiceImpl {

    private final static Logger logger = Logger.getLogger(UwEnumerationManagementServiceImpl.class);

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequest, ContextInfo contextInfo) throws MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<EnumeratedValueEntity> returnValues = new ArrayList<EnumeratedValueEntity>();
        if (searchRequest.getSearchKey().equals("enumeration.management.search")) {
            List<String> enumTypes = null;
            List<String> enumCodes = null;
            for (SearchParamInfo parm : searchRequest.getParams()) {
                if ((parm.getKey().equals("enumeration.queryParam.enumerationType")) && (parm.getValues() != null)) {
                    enumTypes = parm.getValues();
                } else if ((parm.getKey().equals("enumeration.queryParam.enumerationCode") && (parm.getValues() != null))) {
                    enumCodes = parm.getValues();
                } else if ((parm.getKey().equals("enumeration.queryParam.enumerationOptionalCode") && (parm.getValues() != null))) {
                    enumCodes = parm.getValues();
                }
            }

            for (String type : enumTypes) {
                List<EnumeratedValueEntity> enumvalues = getEnumValueDao().getByEnumerationKey(type);
                if ((enumCodes != null) && (enumCodes.size() > 0)) {
                    for (EnumeratedValueEntity enumValue : enumvalues) {
                        for (String code : enumCodes) {
                            if (enumValue.getCode().equals(code)) {
                                returnValues.add(enumValue);
                                break;
                            } else if (enumValue.getCode().startsWith(code)) {
                                returnValues.add(enumValue);
                                break;
                            }
                        }
                    }
                } else {
                    returnValues.addAll(enumvalues);
                }
            }

        }

        if (returnValues == null) {
            return null;
        }

        SearchResultInfo searchResult = new SearchResultInfo();

        if (!CollectionUtils.isEmpty(returnValues)) {
            //Use a hashset of the cell values to remove duplicates
            for (EnumeratedValueEntity enumValue : returnValues) {
                SearchResultRowInfo row = new SearchResultRowInfo();
                row.addCell("enumeration.resultColumn.code", enumValue.getCode());
                row.addCell("enumeration.resultColumn.abbrevValue", enumValue.getAbbrevValue());
                row.addCell("enumeration.resultColumn.value", enumValue.getValue());
                //row.addCell("enumeration.resultColumn.effectiveDate", enumValue.getEffectiveDate());
                //row.addCell("enumeration.resultColumn.expirationDate", enumValue.getExpirationDate());
                row.addCell("enumeration.resultColumn.sortKey", enumValue.getSortKey());
                searchResult.getRows().add(row);
            }
            return searchResult;
        }


        try {
            searchResult = getSearchManager().search(searchRequest, contextInfo);
        } catch (InvalidParameterException e) {
            logger.error("Invalid params for search", e);
        }
        return searchResult;

    }

}
