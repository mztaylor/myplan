package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 *  Assembles a list of published terms.
 */
public class PublishedTermsListBuilder extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(PublishedTermsListBuilder.class);

    private AcademicCalendarService academicCalendarService;

    private final static String listItemSuffix = " quarter only";

    /**
     *  Build and returns the list of available terms.
     *
     * @return A List of available terms as KeyValue items.
     */
    @Override
    public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        //  Fetch the available terms from the Academic Calendar Service.
        List<TermInfo> termInfos = null;
        try {
            termInfos = getAcademicCalendarService().getCurrentTerms(null, null);
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
        }

        //  Add the "any" item.
        keyValues.add(new ConcreteKeyValue("1", "Any quarter"));

        //  Add the individual term items.
        short keyCount = 1;
        for (TermInfo ti : termInfos) {
            keyCount++;
            String termName = ti.getName();
            keyValues.add(new ConcreteKeyValue(String.valueOf(keyCount), termName + listItemSuffix));
        }

        return keyValues;
    }

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

}
