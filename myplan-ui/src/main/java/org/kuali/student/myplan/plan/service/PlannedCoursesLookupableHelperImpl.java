package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Produce a list of planned course items.
 */
public class PlannedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private transient AcademicCalendarService academicCalendarService;
    private final Logger logger = Logger.getLogger(PlannedCoursesLookupableHelperImpl.class);

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    /*
   atpPrefix is the length of "kuali.uw.atp." prefix in "kuali.uw.atp.spring2014"
    */
    private int atpPrefix = 13;

    @Override
    protected List<PlannedTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        try {

            List<PlanItemDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, true);
            Collections.sort(plannedCoursesList);
            List<PlannedTerm> plannedTerms = new ArrayList<PlannedTerm>();
            List<TermInfo> termInfos = null;
            try {
                termInfos = getAcademicCalendarService().getCurrentTerms(CourseSearchConstants.PROCESS_KEY,
                        CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Web service call failed.", e);
                //  Create an empty list to Avoid NPE below allowing the data object to be fully initialized.
                termInfos = new ArrayList<TermInfo>();
            }
            /*
            Populating the PlannedTerm List
             */
            for (PlanItemDataObject plan : plannedCoursesList) {
                for (String atp : plan.getAtpIds()) {
                    boolean exists = false;
                    if (plannedTerms.size() > 0) {
                        for (PlannedTerm plannedTerm : plannedTerms) {

                            if (plannedTerm.getPlanItemId().equalsIgnoreCase(atp)) {
                                plannedTerm.getPlannedList().add(plan);
                                plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                                exists = true;

                            }
                        }
                        if (!exists) {
                            PlannedTerm plannedTerm1 = new PlannedTerm();
                            plannedTerm1.setPlanItemId(atp);
                            StringBuffer str = new StringBuffer();
                            String qtrYr = atp.substring(atpPrefix, atp.length());
                            String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
                            String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                            plannedTerm1.setQtrYear(QtrYear);
                            plannedTerm1.getPlannedList().add(plan);
                            plannedTerm1.setCredits(plannedTerm1.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                            plannedTerm1.setCurrentTerm(false);
                            plannedTerms.add(plannedTerm1);


                        }

                    } else {
                        PlannedTerm plannedTerm = new PlannedTerm();
                        plannedTerm.setPlanItemId(atp);
                        StringBuffer str = new StringBuffer();
                        String qtrYr = atp.substring(atpPrefix, atp.length());
                        String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
                        String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                        plannedTerm.setQtrYear(QtrYear);
                        plannedTerm.getPlannedList().add(plan);
                        plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                        plannedTerm.setCurrentTerm(false);
                        plannedTerms.add(plannedTerm);

                    }
                }

            }


            return plannedTerms;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
