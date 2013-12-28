package org.kuali.student.myplan.sampleplan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.r2.common.util.constants.ProgramServiceConstants;
import org.kuali.student.r2.lum.program.dto.MajorDisciplineInfo;
import org.kuali.student.r2.lum.program.service.ProgramService;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/17/12
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgramKeyValues extends KeyValuesBase {
    private final Logger logger = Logger.getLogger(ProgramKeyValues.class);

    private transient ProgramService programService;

    @Override
    public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        List<MajorDisciplineInfo> majorDisciplineInfos = (List<MajorDisciplineInfo>) GlobalVariables.getUserSession().retrieveObject(PlanConstants.SESSION_KEY_ADVISER_MAJORS);
        if (!CollectionUtils.isEmpty(majorDisciplineInfos)) {
            for (MajorDisciplineInfo majorDisciplineInfo : majorDisciplineInfos) {
                keyValues.add(new ConcreteKeyValue(majorDisciplineInfo.getId(), majorDisciplineInfo.getCode()));
            }
        }

        return keyValues;
    }


    public ProgramKeyValues() {
        super();
    }


    public ProgramService getProgramService() {
        if (programService == null) {
            programService = (ProgramService)
                    GlobalResourceLoader.getService(new QName(ProgramServiceConstants.PROGRAM_NAMESPACE, "ProgramService"));
        }
        return programService;
    }

    public void setProgramService(ProgramService programService) {
        this.programService = programService;
    }
}
