package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/17/12
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class DegreeAuditTacomaPrograms extends KeyValuesBase {


    private final Logger logger = Logger.getLogger(DegreeAuditTacomaPrograms.class);

    private boolean blankOption;

    private transient DegreeAuditService degreeAuditService;


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
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        try {
            List<AuditProgramInfo> auditProgramInfoList = getDegreeAuditService().getAuditPrograms( DegreeAuditConstants.CONTEXT_INFO );

            for (AuditProgramInfo programInfo : auditProgramInfoList)
            {
                if( programInfo.getCampus().equals( "TACOMA" ))
                {
                    ConcreteKeyValue keyValue = new ConcreteKeyValue( programInfo.getProgramId(), programInfo.getProgramTitle() );
                    keyValues.add(keyValue);
                }
            }
            Collections.sort(keyValues, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue keyValue1, KeyValue keyValue2) {
                    return keyValue1.getValue().compareTo(keyValue2.getValue());
                }
            });
        } catch (Exception e) {
            logger.error("could not retrieve AuditPrograms", e);
        }
        keyValues.add( 0, new ConcreteKeyValue(DegreeAuditConstants.DEFAULT_KEY, DegreeAuditConstants.DEFAULT_VALUE));
        return keyValues;

    }

    public DegreeAuditTacomaPrograms() {
        super();
    }

    public boolean isBlankOption() {
        return blankOption;
    }

    public void setBlankOption(boolean blankOption) {
        this.blankOption = blankOption;
    }
}
