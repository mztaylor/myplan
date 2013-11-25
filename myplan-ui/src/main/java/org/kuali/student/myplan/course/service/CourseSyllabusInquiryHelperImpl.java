package org.kuali.student.myplan.course.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.course.dataobject.SyllabusItem;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 10/31/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseSyllabusInquiryHelperImpl extends KualiInquirableImpl {
    private final Logger logger = Logger.getLogger(CourseSyllabusInquiryHelperImpl.class);

    private transient CourseOfferingService courseOfferingService;

    @Override
    public SyllabusItem retrieveDataObject(Map fieldValues) {
        String activityOfferingId = String.valueOf(fieldValues.get(PlanConstants.PARAM_ACTIVITY_ID));
        SyllabusItem syllabusItem = null;
        if (StringUtils.isNotBlank(activityOfferingId)) {
            ActivityOfferingDisplayInfo activityDisplayInfo = null;
            try {
                activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityOfferingId, PlanConstants.CONTEXT_INFO);
                if (activityDisplayInfo != null) {
                    String syllabus = null;
                    String courseOfferingId = null;
                    for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                        if (CourseSearchConstants.SYLLABUS_DESCRIPTION.equalsIgnoreCase(attributeInfo.getKey())) {
                            syllabus = attributeInfo.getValue();
                        } else if (CourseSearchConstants.PRIMARY_ACTIVITY_OFFERING_ID.equalsIgnoreCase(attributeInfo.getKey())) {
                            courseOfferingId = attributeInfo.getValue();
                        }
                    }
                    if (StringUtils.isNotBlank(syllabus)) {

                        CourseOfferingInfo courseOfferingInfo = null;
                        try {
                            courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
                        }

                        syllabusItem = new SyllabusItem();
                        AtpHelper.YearTerm yearTerm = AtpHelper.atpToYearTerm(courseOfferingInfo.getTermId());
                        syllabusItem.setTerm(yearTerm.getTermAsID());
                        syllabusItem.setYear(yearTerm.getYearAsString());
                        syllabusItem.setDescription(syllabus);
                        syllabusItem.setSubject(courseOfferingInfo.getSubjectArea().trim());
                        syllabusItem.setNumber(courseOfferingInfo.getCourseNumberSuffix().trim());
                        syllabusItem.setCredit(courseOfferingInfo.getCreditOptionName());
                        syllabusItem.setTitle(activityDisplayInfo.getCourseOfferingTitle());
                        syllabusItem.setActivityCode(activityDisplayInfo.getActivityOfferingCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Could not retrieve ActivityOffering data for syllabus using activityId " + activityDisplayInfo, e);
            }
        }
        return syllabusItem;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }
}
