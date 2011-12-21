package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;

import org.apache.log4j.Logger;

import javax.naming.Context;
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
     * Placeholders so that ehcache (via AOP pointcounts) don't blow up when trying to
     * use these parameters as keys (computing the hashcode).
     */
    public final static String processKeyPlaceHolder = "placeHolder";
    public final static ContextInfo contextInfoPlaceHolder = new ContextInfo();
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
            termInfos = getAcademicCalendarService().getCurrentTerms(processKeyPlaceHolder, contextInfoPlaceHolder);
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
        }

        //  Add the "any" item.
        keyValues.add(new ConcreteKeyValue(CourseSearchForm.SEARCH_TERM_ANY_ITEM, "Any quarter"));

        //  Add term info to the list if the above service call was successful.
        if (termInfos != null) {
            //  Add the individual term items.
            for (TermInfo ti : termInfos) {
                keyValues.add(new ConcreteKeyValue(ti.getKey(), ti.getName() + listItemSuffix));
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
