package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.FullPlanTermItemsDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;

import javax.xml.namespace.QName;
import java.awt.*;
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

    private String term1 = "Autumn";
    private String term2 = "Winter";
    private String term3 = "Spring";
    private String term4 = "Summer";

    public enum terms {Autumn, Winter, Spring, Summer}

    ;

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
                                /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                                if (plan.getCourseDetails().getCredit().length() > 2) {
                                    String[] str = plan.getCourseDetails().getCredit().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                                    String credit = str[2];
                                    plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(credit));
                                } else {
                                    plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                                }
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
                            /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                            if (plan.getCourseDetails().getCredit().length() > 2) {
                                String[] str2 = plan.getCourseDetails().getCredit().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                                String credit = str2[2];
                                plannedTerm1.setCredits(plannedTerm1.getCredits() + Integer.parseInt(credit));
                            } else {
                                plannedTerm1.setCredits(plannedTerm1.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                            }
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
                        /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                        if (plan.getCourseDetails().getCredit().length() > 2) {
                            String[] str3 = plan.getCourseDetails().getCredit().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            String credit = str3[2];
                            plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(credit));
                        } else {
                            plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                        }
                        plannedTerm.setCurrentTerm(false);
                        plannedTerms.add(plannedTerm);

                    }
                }

            }
            /*
           Populating the backup list for the Plans
            */
            List<PlanItemDataObject> backupCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, true);

            int count = plannedTerms.size();
            for (PlanItemDataObject bl : backupCoursesList) {
                for (String atp : bl.getAtpIds()) {

                    boolean added = false;
                    for (int i = 0; i < count; i++) {
                        if (atp.equalsIgnoreCase(plannedTerms.get(i).getPlanItemId())) {
                            List<PlanItemDataObject> backupLists = new ArrayList<PlanItemDataObject>();
                            backupLists.add(bl);
                            plannedTerms.get(i).setBackupList(backupLists);
                            added = true;
                        }
                    }
                    if (!added) {
                        PlannedTerm plannedTerm = new PlannedTerm();
                        plannedTerm.setPlanItemId(atp);
                        StringBuffer str = new StringBuffer();
                        String qtrYr = atp.substring(atpPrefix, atp.length());
                        String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
                        String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                        plannedTerm.setQtrYear(QtrYear);
                        plannedTerm.getBackupList().add(bl);
                        /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                        if (bl.getCourseDetails().getCredit().length() > 2) {
                            String[] str4 = bl.getCourseDetails().getCredit().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            String credit = str4[2];
                            plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(credit));
                        } else {
                            plannedTerm.setCredits(plannedTerm.getCredits() + Integer.parseInt(bl.getCourseDetails().getCredit()));
                        }
                        plannedTerm.setCurrentTerm(false);
                        plannedTerms.add(plannedTerm);

                    }
                }


            }

            /*
           Used for sorting the planItemDataobjects
            */
            List<PlannedTerm> orderedPlanTerms = new ArrayList<PlannedTerm>();
            int size = plannedTerms.size();
            for (int i = 0; i < size; i++) {
                this.populateOrderedList(plannedTerms, orderedPlanTerms);
                size--;
                i--;
            }
            Collections.reverse(orderedPlanTerms);

            /*TODO: Remove this once logic for populating the missing terms based on the atps from service is made*/
            /*This logic adds the missing terms in the plannedTerms List for each year*/

            List<PlannedTerm> orderedList = new ArrayList<PlannedTerm>();
            int listSize = orderedPlanTerms.size();
            List<String> yearExistsCompare = new ArrayList<String>();
            for (PlannedTerm plannedTerm:orderedPlanTerms) {


                String[] split = plannedTerm.getQtrYear().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                if (!yearExistsCompare.contains(split[1].trim())) {
                    yearExistsCompare.add(split[1].trim());
                    List<String> quarters = new ArrayList<String>();
                    int year = Integer.parseInt(split[1].trim());

                    for (PlannedTerm pl:orderedPlanTerms) {
                        String[] str = pl.getQtrYear().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        if (split[1].trim().equalsIgnoreCase(str[1].trim())) {
                            quarters.add(str[0].trim());
                            orderedList.add(pl);
                        }

                    }
                    if (quarters.size() < 4) {

                        this.addMissingTerms(orderedList, quarters, year);

                    }
                }


            }
            /*
           Used for sorting the planItemDataobjects
            */
            List<PlannedTerm> orderPlanTerms = new ArrayList<PlannedTerm>();
            int size1 = orderedList.size();
            for (int i = 0; i < size1; i++) {
                this.populateOrderedList(orderedList, orderPlanTerms);
                size1--;
                i--;
            }
            Collections.reverse(orderPlanTerms);


            List<PlannedTerm> plannedTermsOrderedList = new ArrayList<PlannedTerm>();
            /* TODO: Remove these when implementation for getting the past records is included
                These are hardcoded to show the past data in the list

             */
            String yearParam = orderPlanTerms.get(0).getQtrYear();
            String[] splitStr = yearParam.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            populatePreviousData(plannedTermsOrderedList, splitStr[0].trim(), splitStr[1].trim());

            for (PlannedTerm pl : orderPlanTerms) {
                plannedTermsOrderedList.add(pl);
            }


            /* TODO: Remove these when implementation for getting the future records is included
               These are hardcoded to show the future data in the list

            */

            String[] split = orderPlanTerms.get(orderPlanTerms.size() - 1).getQtrYear().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            int year = 0;
            String quarter = null;
            if (split.length == 2) {
                year = Integer.parseInt(split[1].trim());
                quarter = split[0].trim();
            }
            populateFutureData(plannedTermsOrderedList, quarter, year);


            return plannedTermsOrderedList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populatePreviousData(List<PlannedTerm> plannedTerms, String term, String year) {

        List<String> atps = new ArrayList<String>();
        int yearVal = Integer.parseInt(year);
        if (term.equalsIgnoreCase("Winter")) {
            String termYrVal = "kuali.uw.atp.autumn" + (yearVal - 1);
            atps.add(termYrVal);
        }
        if (term.equalsIgnoreCase("Spring")) {
            String termYrVal = "kuali.uw.atp.autumn" + (yearVal - 1);
            atps.add(termYrVal);
            String termYrVal2 = "kuali.uw.atp.winter" + yearVal;
            atps.add(termYrVal2);
        }
        if (term.equalsIgnoreCase("Summer")) {
            String termYrVal = "kuali.uw.atp.autumn" + (yearVal - 1);
            atps.add(termYrVal);
            String termYrVal1 = "kuali.uw.atp.winter" + yearVal;
            atps.add(termYrVal1);
            String termYrVal2 = "kuali.uw.atp.spring" + yearVal;
            atps.add(termYrVal2);
        }

        for (String atp : atps) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId(atp);
            String qtrYr = atp.substring(atpPrefix, atp.length());
            String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            StringBuffer str = new StringBuffer();
            str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
            String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
            pl.setQtrYear(QtrYear);
            /*PlanItemDataObject planItemDataObject = new PlanItemDataObject();
            CourseDetails courseDetails = new CourseDetails();
            planItemDataObject.setCourseDetails(courseDetails);
            List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
            planItemDataObjects.add(planItemDataObject);
            pl.setPlannedList(planItemDataObjects);*/
            plannedTerms.add(pl);
        }


    }

    private void populateFutureData(List<PlannedTerm> plannedTerms, String term, int year) {

        List<String> atps = new ArrayList<String>();

        if (term.equalsIgnoreCase("Autumn")) {
            String termYrVal = "kuali.uw.atp.winter" + (year + 1);
            atps.add(termYrVal);
            String termYrVal1 = "kuali.uw.atp.spring" + (year + 1);
            atps.add(termYrVal1);
            String termYrVal2 = "kuali.uw.atp.summer" + (year + 1);
            atps.add(termYrVal2);

        }
        if (term.equalsIgnoreCase("Winter")) {
            String termYrVal = "kuali.uw.atp.spring" + year;
            atps.add(termYrVal);
            String termYrVal1 = "kuali.uw.atp.summer" + year;
            atps.add(termYrVal1);

        }
        if (term.equalsIgnoreCase("Spring")) {
            String termYrVal = "kuali.uw.atp.summer" + year;
            atps.add(termYrVal);

        }

        for (String atp : atps) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId(atp);
            String qtrYr = atp.substring(atpPrefix, atp.length());
            String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            StringBuffer str = new StringBuffer();
            str = str.append(splitStr[0]).append(" ").append(splitStr[1]);
            String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
            pl.setQtrYear(QtrYear);
            /*PlanItemDataObject planItemDataObject = new PlanItemDataObject();
            CourseDetails courseDetails = new CourseDetails();
            planItemDataObject.setCourseDetails(courseDetails);
            List<PlanItemDataObject> planItemDataObjects = null;
            planItemDataObjects.add(planItemDataObject);
            pl.setPlannedList(planItemDataObjects);*/
            plannedTerms.add(pl);
        }

    }

    private void addMissingTerms(List<PlannedTerm> termOrderedPlanTerms, List<String> quarters, int year) {

        boolean autumnExists = false;
        boolean winterExists = false;
        boolean springExists = false;
        boolean summerExists = false;
         for(String term:quarters){
             terms fd = terms.valueOf(term);

             switch (fd) {
                 case Autumn:
                     if (!autumnExists) {
                         autumnExists = true;
                     } else {
                         autumnExists = false;
                     }
                     break;
                 case Winter:
                     if (!winterExists) {
                         winterExists = true;
                     } else {
                         winterExists = false;
                     }
                     break;
                 case Spring:
                     if (!springExists) {
                         springExists = true;
                     } else {
                         springExists = false;
                     }
                     break;
                 case Summer:
                     if (!summerExists) {
                         summerExists = true;
                     } else {
                         summerExists = false;
                     }
                     break;
             }
         }
        if (!autumnExists) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId("kuali.uw.atp.autumn"+(year));
            String qtrYr = term1+" "+(year);
            pl.setQtrYear(qtrYr);
            termOrderedPlanTerms.add(pl);
        }
        if (!winterExists) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId("kuali.uw.atp.winter"+(year));
            String qtrYr = term2+" "+(year);
            pl.setQtrYear(qtrYr);
            termOrderedPlanTerms.add(pl);

        }
        if (!springExists) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId("kuali.uw.atp.spring"+(year));
            String qtrYr = term3+" "+(year);
            pl.setQtrYear(qtrYr);
            termOrderedPlanTerms.add(pl);

        }
        if (!summerExists) {
            PlannedTerm pl = new PlannedTerm();
            pl.setPlanItemId("kuali.uw.atp.summer"+(year));
            String qtrYr = term4+" "+(year);
            pl.setQtrYear(qtrYr);
            termOrderedPlanTerms.add(pl);

        }

    }

    private void populateOrderedList(List<PlannedTerm> plannedTerms, List<PlannedTerm> orderedPlanTerms) {
        if (plannedTerms != null) {
            StringBuffer cc = new StringBuffer();
            int maxQ = 0;
            String[] quarters = new String[plannedTerms.size()];
            Integer[] resultYear = new Integer[plannedTerms.size()];
            //Logical implementation to get the last offered year
            int actualYear = -1;
            String actualQuarter = null;
            int resultQuarter = 0;
            int count = 0;
            for (PlannedTerm pl : plannedTerms) {
                String[] splitStr = pl.getQtrYear().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                int year = 0;
                String quarter = null;
                if (splitStr.length == 2) {
                    resultYear[count] = Integer.parseInt(splitStr[1].trim());

                    quarters[count] = splitStr[0].trim();
                }


                if (resultYear[count] > actualYear) {
                    actualYear = resultYear[count];
                }
                count++;
            }
            //Logical implementation to get the last offered quarter
            for (int i = 0; i < quarters.length; i++) {
                String tempQuarter = quarters[i];
                terms fd = terms.valueOf(quarters[i]);
                switch (fd) {
                    case Autumn:
                        resultQuarter = 1;
                        break;
                    case Winter:
                        resultQuarter = 2;
                        break;
                    case Spring:
                        resultQuarter = 3;
                        break;
                    case Summer:
                        resultQuarter = 4;
                        break;
                }
                if (resultYear[i].equals(actualYear) && resultQuarter > maxQ) {
                    maxQ = resultQuarter;
                    actualQuarter = tempQuarter;

                }
            }

            cc = cc.append(actualQuarter).append(" ").append(actualYear);
            int size = plannedTerms.size();
            for (int j = 0; j < size; j++) {
                if (plannedTerms.get(j).getQtrYear().equalsIgnoreCase(cc.toString())) {
                    orderedPlanTerms.add(plannedTerms.get(j));
                    plannedTerms.remove(plannedTerms.get(j));
                    break;
                }
            }
        }
    }
}
