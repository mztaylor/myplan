package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/17/12
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class DegreeAuditSeattlePrograms extends KeyValuesBase {


    private final Logger logger = Logger.getLogger(DegreeAuditSeattlePrograms.class);

    private boolean blankOption;

    private transient DegreeAuditService degreeAuditService;

    private transient EnumerationManagementService enumService;

    private HashMap<String, List<EnumeratedValueInfo>> hashMap = new HashMap<String, List<EnumeratedValueInfo>>();

    public HashMap<String, List<EnumeratedValueInfo>> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, List<EnumeratedValueInfo>> hashMap) {
        this.hashMap = hashMap;
    }


    protected synchronized EnumerationManagementService getEnumerationService() {
        if (this.enumService == null) {
            this.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return this.enumService;
    }


    public DegreeAuditService getDegreeAuditService() {
        if (degreeAuditService == null) {
            degreeAuditService = (DegreeAuditService)
                    GlobalResourceLoader.getService(new QName(DegreeAuditServiceConstants.NAMESPACE,
                            DegreeAuditServiceConstants.SERVICE_NAME));
        }
        return degreeAuditService;
    }


    @Override
    public List<KeyValue> getKeyValues() {
        List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();
        try {
            auditProgramInfoList = getDegreeAuditService().getAuditPrograms(DegreeAuditConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("could not retrieve AuditPrograms", e);
        }

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        for (AuditProgramInfo programInfo : auditProgramInfoList) {
            /*Seattle campus programs starts with 0*/
            if (programInfo.getProgramId().startsWith("0")) {
                keyValues.add(new ConcreteKeyValue(programInfo.getProgramId(), programInfo.getProgramTitle()));
            }
        }
        /*Removing Duplicate entries from Key Values*/
        HashSet hs = new HashSet();
        hs.addAll(keyValues);
        keyValues.clear();
        keyValues.addAll(hs);
        Collections.sort(keyValues, new Comparator<KeyValue>() {
            @Override
            public int compare(KeyValue keyValue1, KeyValue keyValue2) {
                return keyValue1.getKey().compareTo(keyValue2.getKey());
            }
        });
        return keyValues;
    }

    public DegreeAuditSeattlePrograms() {
        super();
    }

    public boolean isBlankOption() {
        return blankOption;
    }

    public void setBlankOption(boolean blankOption) {
        this.blankOption = blankOption;
    }
}
