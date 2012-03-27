package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.CourseSearchConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 *  Assembles a list of published terms.
 *
 *  FIXME: This is very similar to the course.util.PublishedTermsListBuilder. They need to be combined.
 *
 */
public class PublishedTermsListBuilder extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(PublishedTermsListBuilder.class);

    private AcademicCalendarService academicCalendarService;

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
            termInfos = getAcademicCalendarService().getCurrentTerms(CourseSearchConstants.PROCESS_KEY,
                CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
        }

        //  Add term info to the list if the above service call was successful.
        if (termInfos != null) {
            //  Add the individual term items.
            for (TermInfo ti : termInfos) {
                keyValues.add(new ConcreteKeyValue(ti.getId(), ti.getName()));
            }
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
