package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class AuditRunProgramParam extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(AuditRunProgramParam.class);

    private boolean blankOption;

    private transient EnumerationManagementService enumService;

    private HashMap<String,List<EnumeratedValueInfo>> hashMap=new HashMap<String, List<EnumeratedValueInfo>>();

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



    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue("0CHEM  1015", "Chemistry (BS) ACS Cert"));
        keyValues.add(new ConcreteKeyValue("0ESS   0011", "Earth and Space Sciences (BA)"));
        keyValues.add(new ConcreteKeyValue("0ECON  0011", "Economics (BA)"));
        keyValues.add(new ConcreteKeyValue("0ENGL  0011", "English"));
        keyValues.add(new ConcreteKeyValue("0HIST  0011", "History"));
        keyValues.add(new ConcreteKeyValue("0PHIL  0011", "Philosophy"));
        keyValues.add(new ConcreteKeyValue("0ACMS  2015", "ACMS: Biological and Life Sciences"));
        keyValues.add(new ConcreteKeyValue("0A A   0016", "Aeronautical & Astronautical Engineering"));
        keyValues.add(new ConcreteKeyValue("0ANTH  2011", "Anthropology: AG Option"));
        keyValues.add(new ConcreteKeyValue("0ART   0511", "Art: Design Studies" +
                "" +
                ""));



        return keyValues;
    }

    public AuditRunProgramParam() {
        super();
    }

    public boolean isBlankOption() {
        return blankOption;
    }

    public void setBlankOption(boolean blankOption) {
        this.blankOption = blankOption;
    }
}
