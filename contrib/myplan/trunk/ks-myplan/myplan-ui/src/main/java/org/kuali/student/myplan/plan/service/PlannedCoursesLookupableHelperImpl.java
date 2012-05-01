package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.ContextInfo;

import javax.xml.namespace.QName;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Produce a list of planned course items.
 */
public class PlannedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private transient AcademicCalendarService academicCalendarService;

    private transient AcademicRecordService academicRecordService;

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();


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

    public AcademicRecordService getAcademicRecordService() {
        if (this.academicRecordService == null) {
            //   TODO: Use constants for namespace.
            this.academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return this.academicRecordService;
    }

    public void setAcademicRecordService(AcademicRecordService academicRecordService) {
        this.academicRecordService = academicRecordService;
    }


    private String term1 = "Autumn";
    private String term2 = "Winter";
    private String term3 = "Spring";
    private String term4 = "Summer";

    public enum terms {Autumn, Winter, Spring, Summer}

    ;

    @Override
    protected List<PlannedTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        try {

            List<PlannedCourseDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, true);
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
            for (PlannedCourseDataObject plan : plannedCoursesList) {
                String atp = plan.getPlanItemDataObject().getAtp();
                boolean exists = false;
                for (PlannedTerm term : plannedTerms) {

                    if (term.getPlanItemId().equalsIgnoreCase(atp)) {
                        term.getPlannedList().add(plan);
                        exists = true;
                    }
                }
                if (!exists) {
                    PlannedTerm term = new PlannedTerm();
                    term.setPlanItemId(atp);
                    /*String qtrYr = atp.substring(atpPrefix, atp.length());*/
                    String[] splitStr = AtpHelper.getAlphaTermAndYearForAtp(atp);/*qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");*/
                    StringBuilder sb = new StringBuilder();
                    sb.append(splitStr[0]).append(" ").append(splitStr[1]);
                    String QtrYear = sb.substring(0, 1).toUpperCase().concat(sb.substring(1));
                    term.setQtrYear(QtrYear);
                    term.getPlannedList().add(plan);
                    term.setCurrentTerm(false);
                    plannedTerms.add(term);
                }
            }


            /*
           Populating the backup list for the Plans
            */
            List<PlannedCourseDataObject> backupCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, true);
            int count = plannedTerms.size();
            for (PlannedCourseDataObject bl : backupCoursesList) {
                String atp = bl.getPlanItemDataObject().getAtp();

                boolean added = false;
                for (int i = 0; i < count; i++) {
                    if (atp.equalsIgnoreCase(plannedTerms.get(i).getPlanItemId())) {
                        plannedTerms.get(i).getBackupList().add(bl);
                        added = true;
                    }
                }
                if (!added) {
                    PlannedTerm plannedTerm = new PlannedTerm();
                    plannedTerm.setPlanItemId(atp);
                    StringBuffer str = new StringBuffer();
                    /*String qtrYr = atp.substring(atpPrefix, atp.length());*/
                    String[] splitStr = AtpHelper.getAlphaTermAndYearForAtp(atp);/*qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");*/
                    str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
                    String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                    plannedTerm.setQtrYear(QtrYear);
                    plannedTerm.getBackupList().add(bl);
                    plannedTerm.setCurrentTerm(false);
                    plannedTerms.add(plannedTerm);
                    count++;
                }
            }


            /*
           Used for sorting the planItemDataobjects
            */
            List<AcademicRecordDataObject> academicRecordDataObjectList = new ArrayList<AcademicRecordDataObject>();
            List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();
            try {

                studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords("9136CCB8F66711D5BE060004AC494FFE", CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not retrieve StudentCourseRecordInfo from the SWS");
            }
            Collections.sort(plannedTerms, new Comparator<PlannedTerm>() {
                @Override
                public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                    return plannedTerm1.getPlanItemId().compareTo(plannedTerm2.getPlanItemId());
                }
            });

            /***********TEST******************/
            Map<String, PlannedTerm> termsList = new HashMap<String, PlannedTerm>();
            String minTerm = studentCourseRecordInfos.get(0).getTermName();
            String maxTerm = plannedTerms.get(plannedTerms.size() - 1).getPlanItemId();
            populateMockList(minTerm, maxTerm, termsList);

            for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                if (termsList.containsKey(studentInfo.getTermName())) {
                    AcademicRecordDataObject academicRecordDataObject = new AcademicRecordDataObject();
                    academicRecordDataObject.setAtpId(studentInfo.getTermName());
                    academicRecordDataObject.setPersonId(studentInfo.getPersonId());
                    academicRecordDataObject.setCourseCode(studentInfo.getCourseCode());
                    academicRecordDataObject.setCourseTitle(studentInfo.getCourseTitle());
                    academicRecordDataObject.setCredit(studentInfo.getCalculatedGradeValue());
                    academicRecordDataObject.setGrade(studentInfo.getCreditsEarned());
                    academicRecordDataObject.setRepeated(studentInfo.getIsRepeated());
                    academicRecordDataObjectList.add(academicRecordDataObject);
                    termsList.get(studentInfo.getTermName()).getAcademicRecord().add(academicRecordDataObject);
                }
            }
            for (PlannedTerm plannedTerm : plannedTerms) {
                if (termsList.containsKey(plannedTerm.getPlanItemId())) {
                    if (plannedTerm.getPlannedList().size() > 0) {
                        termsList.get(plannedTerm.getPlanItemId());
                        termsList.put(plannedTerm.getPlanItemId(), plannedTerm);
                    }
                }
            }
            List<PlannedTerm> perfectPlannedTerms = new ArrayList<PlannedTerm>();
            for (String key : termsList.keySet()) {
                perfectPlannedTerms.add(termsList.get(key));
            }


            /*****************TEST End*********************************/
            Collections.sort(perfectPlannedTerms,new Comparator<PlannedTerm>() {
                @Override
                public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                    return plannedTerm1.getPlanItemId().compareTo(plannedTerm2.getPlanItemId());
                }
            });

            return perfectPlannedTerms;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void populateMockList(String minTerm, String maxTerm, Map<String, PlannedTerm> map) {
        String[] minTerms = AtpHelper.getTermAndYear(minTerm);
        String[] maxTerms = AtpHelper.getTermAndYear(maxTerm);
        int minYear = 0;
        int maxYear = 0;

        if (!minTerms[0].equalsIgnoreCase("4")) {
            minTerm = AtpHelper.getAtpFromYearAndNumTerm("4", String.valueOf(Integer.parseInt(minTerms[1]) - 1));
            minYear = Integer.parseInt(minTerms[1]) - 1;
        }
        else {
            minYear =Integer.parseInt(minTerms[1]);
        }
        if (!maxTerms[0].equalsIgnoreCase("3")) {
            if (maxTerms[0].equalsIgnoreCase("4")) {
                maxTerm = AtpHelper.getAtpFromYearAndNumTerm("3", String.valueOf(Integer.parseInt(maxTerms[1]) + 1));
                maxYear = Integer.parseInt(maxTerms[1]) + 1;

            } else {
                maxTerm = AtpHelper.getAtpFromYearAndNumTerm("3", maxTerms[1]);
                maxYear = Integer.parseInt(maxTerms[1]);
            }
        } else {
            maxYear=Integer.parseInt(maxTerms[1]);
        }
        String term1 = "";
        String term2 = "";
        String term3 = "";
        String term4 = "";
        for(int i=0;!term4.equalsIgnoreCase(maxTerm);i++){
            PlannedTerm plannedTerm1 = new PlannedTerm();
            term1 = AtpHelper.getAtpFromYearAndNumTerm("4", String.valueOf(minYear));
            plannedTerm1.setPlanItemId(term1);
            plannedTerm1.setQtrYear("Autumn"+" "+minYear);
            map.put(term1, plannedTerm1);
            minYear++;
            PlannedTerm plannedTerm2 = new PlannedTerm();
            term2 = AtpHelper.getAtpFromYearAndNumTerm("1", String.valueOf(minYear));
            plannedTerm2.setPlanItemId(term2);
            plannedTerm2.setQtrYear("Winter"+" "+minYear);
            map.put(term2, plannedTerm2);
            PlannedTerm plannedTerm3 = new PlannedTerm();
            term3 = AtpHelper.getAtpFromYearAndNumTerm("2", String.valueOf(minYear));
            plannedTerm3.setPlanItemId(term3);
            plannedTerm3.setQtrYear("Spring"+" "+minYear);
            map.put(term3, plannedTerm3);
            PlannedTerm plannedTerm4 = new PlannedTerm();
            term4 = AtpHelper.getAtpFromYearAndNumTerm("3", String.valueOf(minYear));
            plannedTerm4.setPlanItemId(term4);
            plannedTerm4.setQtrYear("Summer"+" "+minYear);
            map.put(term4, plannedTerm4);
        }


    }
}
