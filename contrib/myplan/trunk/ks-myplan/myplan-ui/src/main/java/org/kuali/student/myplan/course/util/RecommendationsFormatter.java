package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/3/12
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class RecommendationsFormatter extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(RecommendationsFormatter.class);

    private transient CourseOfferingService courseOfferingService;

    @Autowired
    private CourseHelper courseHelper;


    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = UwMyplanServiceLocator.getInstance().getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
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

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    @Override
    public String getAsText() {

        CourseDetails courseDetails = (CourseDetails) super.getValue();
        StringBuffer sb = new StringBuffer();
        String singleQuarterUrl = "inquiry?methodToCall=start&viewId=SingleTerm-InquiryView&term_atp_id=";

        List<RecommendedItemDataObject> plannedRecommendations = new ArrayList<RecommendedItemDataObject>();
        List<RecommendedItemDataObject> recommendedItemDataObjects = new ArrayList<RecommendedItemDataObject>();
        for (RecommendedItemDataObject recommendedItemDataObject : courseDetails.getPlannedCourseSummary().getRecommendedItemDataObjects()) {
            if (recommendedItemDataObject.isPlanned()) {
                plannedRecommendations.add(recommendedItemDataObject);
            } else {
                recommendedItemDataObjects.add(recommendedItemDataObject);
            }
        }

        int counter = 0;
        for (RecommendedItemDataObject recommendedItemDataObject : plannedRecommendations) {
            String atpId = recommendedItemDataObject.getAtpId();
            if (counter == 0) {
                sb = sb.append("<dd>").append("Planned for ").append("<a href=\"").append(singleQuarterUrl).append(atpId).append("\">").append(AtpHelper.atpIdToTermName(atpId)).append("</a> ")
                        .append(String.format(" as recommended by %s ", recommendedItemDataObject.getAdviserName()));
            } else if (counter == recommendedItemDataObjects.size()) {
                sb = sb.append(" and <a href=\"").append(singleQuarterUrl).append(atpId).append("\">").append(AtpHelper.atpIdToTermName(atpId)).append("</a> ")
                        .append(String.format(" as recommended by %s ", recommendedItemDataObject.getAdviserName()));
            } else {
                sb = sb.append(", <a href=\"").append(singleQuarterUrl).append(atpId).append("\">").append(AtpHelper.atpIdToTermName(atpId)).append("</a> ")
                        .append(String.format(" as recommended by %s ", recommendedItemDataObject.getAdviserName()));
            }
            counter++;
        }

        if (sb.length() > 0) {
            sb.append("</dd>");
        }

        for (RecommendedItemDataObject recommendedItemDataObject : recommendedItemDataObjects) {
            sb = sb.append("<dd>").append(String.format("Recommended by %s for <a href=\"inquiry?methodToCall=start&viewId=SingleTerm-InquiryView&term_atp_id=%s\">%s</a> on %s", recommendedItemDataObject.getAdviserName(), recommendedItemDataObject.getAtpId(), AtpHelper.atpIdToTermName(recommendedItemDataObject.getAtpId()), recommendedItemDataObject.getDateAdded())).append("</dd>");
        }

        return sb.toString();
    }

}
