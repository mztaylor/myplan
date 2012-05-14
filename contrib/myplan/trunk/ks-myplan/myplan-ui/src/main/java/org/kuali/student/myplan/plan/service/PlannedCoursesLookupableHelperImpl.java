package org.kuali.student.myplan.plan.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.List;

/**
 * Produce a list of planned course items.
 */
public class PlannedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private final Logger logger = Logger.getLogger(PlannedCoursesLookupableHelperImpl.class);

    private transient AcademicCalendarService academicCalendarService;

    private transient AcademicRecordService academicRecordService;

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

    /*Count of no of future years to be shown the quarter view */
    private int futureTermsCount = 6;

    private String atpTerm1 = "1";
    private String atpTerm2 = "2";
    private String atpTerm3 = "3";
    private String atpTerm4 = "4";

    /**
     * Skip the validation so that we use the criteriaFields param to pass in args to the getSearchResults method.
     *
     * @param form
     * @param searchCriteria
     * @return
     */
    @Override
    public boolean validateSearchParameters(LookupForm form, Map<String, String> searchCriteria) {
        return true;
    }

    @Override
    protected List<PlannedTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {

        String focusAtpId = fieldValues.get(PlanConstants.FOCUS_ATP_ID_KEY);
        String[] focusQuarterYear = new String[2];

        try {
            if (StringUtils.isEmpty(focusAtpId)) {
                focusQuarterYear = AtpHelper.atpIdToTermAndYear(AtpHelper.getFirstAtpIdOfAcademicYear(AtpHelper.getCurrentAtpId()));
            } else {
                focusQuarterYear = AtpHelper.atpIdToTermAndYear(AtpHelper.getFirstAtpIdOfAcademicYear(focusAtpId));
            }
        } catch (Exception e) {
            //  Log and set the year to the current year.
            //  TODO: This logic isn't correct, but does position the quarter view pretty close.
            logger.error("Could not get the requested focus ATP, so using the current academic year.", e);
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            focusQuarterYear[0] = atpTerm1;
            focusQuarterYear[1] = year;
        }

        try {
            List<PlannedTerm> plannedTerms = new ArrayList<PlannedTerm>();

            List<PlannedCourseDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, true);
            Collections.sort(plannedCoursesList);

            /*academic record SWS call to get the studentCourseRecordInfo list */
            List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();
            try {
                /*TODO:Replace the hard coded personId with the actual once logic to get that is known */
                studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords("9136CCB8F66711D5BE060004AC494FFE", PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
            }

            /*
             *  Populating the PlannedTerm List.
             */
            for (PlannedCourseDataObject plan : plannedCoursesList) {
                String atp = plan.getPlanItemDataObject().getAtp();
                boolean exists = false;
                for (PlannedTerm term : plannedTerms) {
                    if (term.getAtpId().equalsIgnoreCase(atp)) {
                        term.getPlannedList().add(plan);
                        exists = true;
                    }
                }
                if (!exists) {
                    PlannedTerm term = new PlannedTerm();
                    term.setAtpId(atp);
                    /*String qtrYr = atp.substring(atpPrefix, atp.length());*/
                    String[] splitStr = AtpHelper.atpIdToTermNameAndYear(atp); /*qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");*/
                    StringBuilder sb = new StringBuilder();
                    sb.append(splitStr[0]).append(" ").append(splitStr[1]);
                    String QtrYear = sb.substring(0, 1).toUpperCase().concat(sb.substring(1));
                    term.setQtrYear(QtrYear);
                    term.getPlannedList().add(plan);
                    plannedTerms.add(term);
                }
            }

            /*
             * Populating the backup list for the Plans
            */
            List<PlannedCourseDataObject> backupCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, true);
            int count = plannedTerms.size();
            for (PlannedCourseDataObject bl : backupCoursesList) {
                String atp = bl.getPlanItemDataObject().getAtp();

                boolean added = false;
                for (int i = 0; i < count; i++) {
                    if (atp.equalsIgnoreCase(plannedTerms.get(i).getAtpId())) {
                        plannedTerms.get(i).getBackupList().add(bl);
                        added = true;
                    }
                }
                if (!added) {
                    PlannedTerm plannedTerm = new PlannedTerm();
                    plannedTerm.setAtpId(atp);
                    StringBuffer str = new StringBuffer();
                    /*String qtrYr = atp.substring(atpPrefix, atp.length());*/
                    String[] splitStr = AtpHelper.atpIdToTermNameAndYear(atp); /*qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");*/
                    str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
                    String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                    plannedTerm.setQtrYear(QtrYear);
                    plannedTerm.getBackupList().add(bl);
                    plannedTerms.add(plannedTerm);
                    count++;
                }
            }

            /*
             * Used for sorting the planItemDataobjects
             */
            List<AcademicRecordDataObject> academicRecordDataObjectList = new ArrayList<AcademicRecordDataObject>();

            Collections.sort(plannedTerms, new Comparator<PlannedTerm>() {
                @Override
                public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                    return plannedTerm1.getAtpId().compareTo(plannedTerm2.getAtpId());
                }
            });

            /*********** Implementation to populate the plannedTerm list with academic record and planned terms ******************/
            if (studentCourseRecordInfos.size() > 0 || plannedTerms.size() > 0) {
                Map<String, PlannedTerm> termsList = new HashMap<String, PlannedTerm>();
                String minTerm = null;
                if (studentCourseRecordInfos.size() > 0) {
                    minTerm = studentCourseRecordInfos.get(0).getTermName();
                } else {
                    minTerm = AtpHelper.getCurrentAtpId();
                }
                String maxTerm = AtpHelper.getCurrentAtpId();
                populateMockList(minTerm, maxTerm, termsList);
                if (plannedTerms.size() > 0) {
                    for (PlannedTerm plannedTerm : plannedTerms) {
                        if (termsList.containsKey(plannedTerm.getAtpId())) {
                            if (plannedTerm.getPlannedList().size() > 0 || plannedTerm.getBackupList().size() > 0) {
                                termsList.get(plannedTerm.getAtpId());
                                termsList.put(plannedTerm.getAtpId(), plannedTerm);
                            }
                        }
                    }
                }
                if (studentCourseRecordInfos.size() > 0) {
                    for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                        if (termsList.containsKey(studentInfo.getTermName())) {
                            AcademicRecordDataObject academicRecordDataObject = new AcademicRecordDataObject();
                            academicRecordDataObject.setAtpId(studentInfo.getTermName());
                            academicRecordDataObject.setPersonId(studentInfo.getPersonId());
                            academicRecordDataObject.setCourseCode(studentInfo.getCourseCode());
                            /*TODO: StudentCourseRecordInfo doesnot have a courseId property so using Id to set the course Id*/
                            academicRecordDataObject.setCourseId(studentInfo.getId());
                            academicRecordDataObject.setCourseTitle(studentInfo.getCourseTitle());
                            academicRecordDataObject.setCredit(studentInfo.getCreditsEarned());
                            academicRecordDataObject.setGrade(studentInfo.getCalculatedGradeValue());
                            academicRecordDataObject.setRepeated(studentInfo.getIsRepeated());
                            academicRecordDataObjectList.add(academicRecordDataObject);
                            termsList.get(studentInfo.getTermName()).getAcademicRecord().add(academicRecordDataObject);
                        }
                    }
                }
                if (plannedTerms.size() > 0) {
                    for (PlannedTerm plannedTerm : plannedTerms) {
                        if (termsList.containsKey(plannedTerm.getAtpId())) {
                            if (plannedTerm.getPlannedList().size() > 0 || plannedTerm.getBackupList().size() > 0) {
                                termsList.get(plannedTerm.getAtpId());
                                termsList.put(plannedTerm.getAtpId(), plannedTerm);
                            }
                        }
                    }
                }
                List<PlannedTerm> perfectPlannedTerms = new ArrayList<PlannedTerm>();
                for (String key : termsList.keySet()) {
                    perfectPlannedTerms.add(termsList.get(key));
                }

                Collections.sort(perfectPlannedTerms,
                        new Comparator<PlannedTerm>() {
                            @Override
                            public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                                return plannedTerm1.getAtpId().compareTo(plannedTerm2.getAtpId());
                            }
                        });

                //  Can't do this step until the sort has been done else the index won't be correct.
                int i = 0;
                for (PlannedTerm pt : perfectPlannedTerms) {
                    String[] qy = AtpHelper.atpIdToTermAndYear(pt.getAtpId());
                    if (qy[0].equals(focusQuarterYear[0])
                            && qy[1].equals(focusQuarterYear[1])) {
                        pt.setIndex(i);
                        break;
                    }
                    i++;
                }

                return perfectPlannedTerms;
            }

            /*Implementation to populate the future terms till 6 years from current term if no academic record data and planned term data are present*/
            else {
                List<PlannedTerm> plannedTermList = new ArrayList<PlannedTerm>();
                String currentAtpId = AtpHelper.getCurrentAtpId();
                this.populateFutureData(currentAtpId, plannedTermList);

                return plannedTermList;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateMockList(String minTerm, String maxTerm, Map<String, PlannedTerm> map) {
        String[] minTerms = AtpHelper.atpIdToTermAndYear(minTerm);
        String[] maxTerms = AtpHelper.atpIdToTermAndYear(maxTerm);
        int minYear = 0;
        int maxYear = 0;

        if (!minTerms[0].equalsIgnoreCase(atpTerm4)) {
            minTerm = AtpHelper.getAtpFromNumTermAndYear(atpTerm4, String.valueOf(Integer.parseInt(minTerms[1]) - 1));
            minYear = Integer.parseInt(minTerms[1]) - 1;
        } else {
            minYear = Integer.parseInt(minTerms[1]);
        }
        if (!maxTerms[0].equalsIgnoreCase(atpTerm3)) {
            if (maxTerms[0].equalsIgnoreCase(atpTerm4)) {
                maxTerm = AtpHelper.getAtpFromNumTermAndYear(atpTerm3, String.valueOf(Integer.parseInt(maxTerms[1]) + futureTermsCount));
                maxYear = Integer.parseInt(maxTerms[1]) + futureTermsCount;

            } else {
                maxTerm = AtpHelper.getAtpFromNumTermAndYear(atpTerm3, String.valueOf(Integer.parseInt(maxTerms[1]) + futureTermsCount));
                maxYear = Integer.parseInt(maxTerms[1]) + futureTermsCount;
            }
        } else {
            maxTerm = AtpHelper.getAtpFromNumTermAndYear(atpTerm3, String.valueOf(Integer.parseInt(maxTerms[1]) + futureTermsCount));
            maxYear = Integer.parseInt(maxTerms[1]) + futureTermsCount;
        }
        String term1 = "";
        String term2 = "";
        String term3 = "";
        String term4 = "";
        for (int i = 0; !term4.equalsIgnoreCase(maxTerm); i++) {
            PlannedTerm plannedTerm1 = new PlannedTerm();
            term1 = AtpHelper.getAtpFromNumTermAndYear(atpTerm4, String.valueOf(minYear));
            plannedTerm1.setAtpId(term1);
            plannedTerm1.setQtrYear(PlanConstants.TERM_4 + " " + minYear);
            map.put(term1, plannedTerm1);
            minYear++;
            PlannedTerm plannedTerm2 = new PlannedTerm();
            term2 = AtpHelper.getAtpFromNumTermAndYear(atpTerm1, String.valueOf(minYear));
            plannedTerm2.setAtpId(term2);
            plannedTerm2.setQtrYear(PlanConstants.TERM_1 + " " + minYear);
            map.put(term2, plannedTerm2);
            PlannedTerm plannedTerm3 = new PlannedTerm();
            term3 = AtpHelper.getAtpFromNumTermAndYear(atpTerm2, String.valueOf(minYear));
            plannedTerm3.setAtpId(term3);
            plannedTerm3.setQtrYear(PlanConstants.TERM_2 + " " + minYear);
            map.put(term3, plannedTerm3);
            PlannedTerm plannedTerm4 = new PlannedTerm();
            term4 = AtpHelper.getAtpFromNumTermAndYear(atpTerm3, String.valueOf(minYear));
            plannedTerm4.setAtpId(term4);
            plannedTerm4.setQtrYear(PlanConstants.TERM_3 + " " + minYear);
            map.put(term4, plannedTerm4);
        }
    }


    private void populateFutureData(String termId, List<PlannedTerm> plannedTermList) {
        String[] strings = AtpHelper.atpIdToTermAndYear(termId);
        int minYear = Integer.parseInt(strings[1]);
        if (!strings[0].equalsIgnoreCase(atpTerm4)) {
            minYear = minYear - 1;
        }
        String term1 = "";
        String term2 = "";
        String term3 = "";
        String term4 = "";
        for (int i = 0; i <= 5; i++) {
            PlannedTerm plannedTerm1 = new PlannedTerm();
            term1 = AtpHelper.getAtpFromNumTermAndYear(atpTerm4, String.valueOf(minYear));
            plannedTerm1.setAtpId(term1);
            plannedTerm1.setQtrYear(PlanConstants.TERM_4 + " " + minYear);
            plannedTermList.add(plannedTerm1);
            minYear++;
            PlannedTerm plannedTerm2 = new PlannedTerm();
            term2 = AtpHelper.getAtpFromNumTermAndYear(atpTerm1, String.valueOf(minYear));
            plannedTerm2.setAtpId(term2);
            plannedTerm2.setQtrYear(PlanConstants.TERM_1 + " " + minYear);
            plannedTermList.add(plannedTerm2);
            PlannedTerm plannedTerm3 = new PlannedTerm();
            term3 = AtpHelper.getAtpFromNumTermAndYear(atpTerm2, String.valueOf(minYear));
            plannedTerm3.setAtpId(term3);
            plannedTerm3.setQtrYear(PlanConstants.TERM_2 + " " + minYear);
            plannedTermList.add(plannedTerm3);
            PlannedTerm plannedTerm4 = new PlannedTerm();
            term4 = AtpHelper.getAtpFromNumTermAndYear(atpTerm3, String.valueOf(minYear));
            plannedTerm4.setAtpId(term4);
            plannedTerm4.setQtrYear(PlanConstants.TERM_3 + " " + minYear);
            plannedTermList.add(plannedTerm4);
        }
    }
}
