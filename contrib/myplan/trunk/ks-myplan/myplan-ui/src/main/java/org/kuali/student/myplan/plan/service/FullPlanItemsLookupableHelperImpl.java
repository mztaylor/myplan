package org.kuali.student.myplan.plan.service;

import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.FullPlanItemsDataObject;
import org.kuali.student.myplan.plan.dataobject.FullPlanTermItemsDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/12
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class FullPlanItemsLookupableHelperImpl extends PlanItemLookupableHelperBase {
    /*
   atpPrefix is the length of "kuali.uw.atp." prefix in "kuali.uw.atp.spring2014"
    */
    private int atpPrefix = 13;
    private int primaryIndex = 0;
    private String term1 = "autumn";
    private String term2 = "winter";
    private String term3 = "spring";
    private String term4 = "summer";


    public enum terms {Autumn, Winter, Spring, Summer}

    ;

    @Override
    protected List<FullPlanItemsDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        try {
            List<PlanItemDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, true);
            List<FullPlanItemsDataObject> fullPlanItemsDataObjects = new ArrayList<FullPlanItemsDataObject>();


            /*Based on the Planned course list populate the FullPlanItemsDataObject*/
            for (PlanItemDataObject plan : plannedCoursesList) {
                for (String atp : plan.getAtpIds()) {
                    String qtrYr = atp.substring(atpPrefix, atp.length());
                    String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String year = null;
                    String quarter = null;
                    if (splitStr.length == 2) {
                        year = splitStr[1].trim();

                        quarter = splitStr[0].trim();
                    }
                    boolean exists = false;
                    if (fullPlanItemsDataObjects.size() > 0) {

                        for (FullPlanItemsDataObject fpl : fullPlanItemsDataObjects) {

                            if (String.valueOf(fpl.getYear()).equalsIgnoreCase(year)) {

                                boolean termExists = false;
                                for (FullPlanTermItemsDataObject fpt : fpl.getTerms()) {

                                    if (fpt.getTerm().equalsIgnoreCase(quarter)) {
                                        PlanItemDataObject planItemDataObject = new PlanItemDataObject();
                                        List<String> atps = new ArrayList<String>();
                                        atps.add(atp);
                                        planItemDataObject.setAtpIds(atps);
                                        planItemDataObject.setCourseDetails(plan.getCourseDetails());
                                        planItemDataObject.setDateAdded(plan.getDateAdded());
                                        planItemDataObject.setId(plan.getId());
                                        fpt.getPlanItemDataObjects().add(planItemDataObject);
                                        /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                                        if(plan.getCourseDetails().getCredit().length()>2){
                                            String credit=plan.getCourseDetails().getCredit().substring(2,plan.getCourseDetails().getCredit().length()) ;
                                            fpt.setTotalCredits(fpt.getTotalCredits() + Integer.parseInt(credit));
                                        }else{
                                            fpt.setTotalCredits(fpt.getTotalCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                                        }
                                        termExists = true;
                                    }
                                }
                                if (!termExists) {

                                    FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
                                    List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
                                    PlanItemDataObject planItemDataObject = new PlanItemDataObject();
                                    List<String> atps = new ArrayList<String>();
                                    atps.add(atp);
                                    planItemDataObject.setAtpIds(atps);
                                    planItemDataObject.setCourseDetails(plan.getCourseDetails());
                                    planItemDataObject.setDateAdded(plan.getDateAdded());
                                    planItemDataObject.setId(plan.getId());
                                    planItemDataObjects.add(planItemDataObject);
                                    fullPlanTermItemsDataObject.setTerm(quarter);
                                    /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                                    if(plan.getCourseDetails().getCredit().length()>2){
                                        String credit=plan.getCourseDetails().getCredit().substring(2,plan.getCourseDetails().getCredit().length()) ;
                                        fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(credit));
                                    }else{
                                        fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                                    }
                                    fullPlanTermItemsDataObject.setPlanItemDataObjects(planItemDataObjects);
                                    fpl.getTerms().add(fullPlanTermItemsDataObject);
                                }
                                exists = true;

                            }
                        }
                        if (!exists) {
                            FullPlanItemsDataObject fullPlanItemsDataObject = new FullPlanItemsDataObject();
                            fullPlanItemsDataObject.setYear(Integer.parseInt(year));
                            List<FullPlanTermItemsDataObject> fullPlanTermItemsDataObjects = new ArrayList<FullPlanTermItemsDataObject>();
                            FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
                            List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
                            PlanItemDataObject planItemDataObject = new PlanItemDataObject();
                            List<String> atps = new ArrayList<String>();
                            atps.add(atp);
                            planItemDataObject.setAtpIds(atps);
                            planItemDataObject.setCourseDetails(plan.getCourseDetails());
                            planItemDataObject.setDateAdded(plan.getDateAdded());
                            planItemDataObject.setId(plan.getId());
                            planItemDataObjects.add(planItemDataObject);
                            fullPlanTermItemsDataObject.setTerm(quarter);

                            fullPlanTermItemsDataObject.setPlanItemDataObjects(planItemDataObjects);
                            /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                            if(plan.getCourseDetails().getCredit().length()>2){
                                String credit=plan.getCourseDetails().getCredit().substring(2,plan.getCourseDetails().getCredit().length()) ;
                                fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(credit));
                            }else{
                                fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                            }
                            fullPlanTermItemsDataObjects.add(fullPlanTermItemsDataObject);
                            fullPlanItemsDataObject.setTerms(fullPlanTermItemsDataObjects);

                            fullPlanItemsDataObjects.add(fullPlanItemsDataObject);


                        }

                    } else {
                        FullPlanItemsDataObject fullPlanItemsDataObject = new FullPlanItemsDataObject();
                        fullPlanItemsDataObject.setYear(Integer.parseInt(year));


                        List<FullPlanTermItemsDataObject> fullPlanTermItemsDataObjects = new ArrayList<FullPlanTermItemsDataObject>();
                        FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
                        List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
                        PlanItemDataObject planItemDataObject = new PlanItemDataObject();
                        List<String> atps = new ArrayList<String>();
                        atps.add(atp);
                        planItemDataObject.setAtpIds(atps);
                        planItemDataObject.setCourseDetails(plan.getCourseDetails());
                        planItemDataObject.setDateAdded(plan.getDateAdded());
                        planItemDataObject.setId(plan.getId());
                        planItemDataObjects.add(planItemDataObject);
                        fullPlanTermItemsDataObject.setTerm(quarter);

                        fullPlanTermItemsDataObject.setPlanItemDataObjects(planItemDataObjects);
                        /*TODO: Remove this logic of substringing the credit once logic for handling the creditRanges is done     */
                        if(plan.getCourseDetails().getCredit().length()>2){
                            String credit=plan.getCourseDetails().getCredit().substring(2,plan.getCourseDetails().getCredit().length()) ;
                            fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(credit));
                        }else{
                            fullPlanTermItemsDataObject.setTotalCredits(fullPlanTermItemsDataObject.getTotalCredits() + Integer.parseInt(plan.getCourseDetails().getCredit()));
                        }
                        fullPlanTermItemsDataObjects.add(fullPlanTermItemsDataObject);
                        fullPlanItemsDataObject.setTerms(fullPlanTermItemsDataObjects);

                        fullPlanItemsDataObjects.add(fullPlanItemsDataObject);


                    }
                }

            }

            /*Ordering base on year*/
            List<FullPlanItemsDataObject> orderedFullPlanTerms = new ArrayList<FullPlanItemsDataObject>();
            int size = fullPlanItemsDataObjects.size();
            for (int i = 0; i < size; i++) {
                this.yearOrdered(fullPlanItemsDataObjects, orderedFullPlanTerms);
                size--;
                i--;
            }
            Collections.reverse(orderedFullPlanTerms);

            /*Populating missing quarters/Terms*/
            for (FullPlanItemsDataObject fullPlanItemsDataObject : orderedFullPlanTerms) {
                if (fullPlanItemsDataObject.getTerms().size() != 4) {
                    this.addMissingTerms(fullPlanItemsDataObject.getTerms());
                }
            }

            /*Sorting the list based on quarters/Terms in the List*/
            for (FullPlanItemsDataObject fpt : orderedFullPlanTerms) {
                List<FullPlanTermItemsDataObject> termOrderedFullPlanTerms = new ArrayList<FullPlanTermItemsDataObject>();
                int size2 = fpt.getTerms().size();
                for (int i = 0; i < size2; i++) {
                    this.termOrdered(fpt.getTerms(), termOrderedFullPlanTerms);
                    size2--;
                    i--;
                }
                Collections.reverse(termOrderedFullPlanTerms);
                fpt.setTerms(termOrderedFullPlanTerms);
            }

            /*Reordering the List to reflect the academic calender format*/
            populateOrderedAcademicTermList(orderedFullPlanTerms, fullPlanItemsDataObjects);

            return fullPlanItemsDataObjects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateOrderedAcademicTermList(List<FullPlanItemsDataObject> orderedFullPlanTerms, List<FullPlanItemsDataObject> fullPlanItemsDataObjects) {
        List<FullPlanTermItemsDataObject> fullPlanItemsDataObjectList = new ArrayList<FullPlanTermItemsDataObject>();
        FullPlanTermItemsDataObject fullPlanTerm1 = new FullPlanTermItemsDataObject();
        fullPlanTerm1.setTerm(term1);
        fullPlanItemsDataObjectList.add(fullPlanTerm1);
        FullPlanTermItemsDataObject fullPlanTerm2 = new FullPlanTermItemsDataObject();
        fullPlanTerm2.setTerm(term2);
        fullPlanItemsDataObjectList.add(fullPlanTerm2);

        for (FullPlanItemsDataObject fpi : orderedFullPlanTerms) {


            for (FullPlanTermItemsDataObject fpt : fpi.getTerms()) {
                FullPlanTermItemsDataObject fullPlanTermItemsDataObject1 = new FullPlanTermItemsDataObject();
                fullPlanTermItemsDataObject1.setTerm(fpt.getTerm());
                fullPlanTermItemsDataObject1.setTotalCredits(fpt.getTotalCredits());
                fullPlanTermItemsDataObject1.setPlanItemDataObjects(fpt.getPlanItemDataObjects());
                fullPlanItemsDataObjectList.add(fullPlanTermItemsDataObject1);

            }
        }
        FullPlanTermItemsDataObject fullPlanTerm3 = new FullPlanTermItemsDataObject();
        fullPlanTerm3.setTerm(term3);
        fullPlanItemsDataObjectList.add(fullPlanTerm3);
        FullPlanTermItemsDataObject fullPlanTerm4 = new FullPlanTermItemsDataObject();
        fullPlanTerm4.setTerm(term4);
        fullPlanItemsDataObjectList.add(fullPlanTerm4);

        for (int i = 0; i < orderedFullPlanTerms.size(); i++) {
            FullPlanItemsDataObject fullPlanItemsDataObject1 = new FullPlanItemsDataObject();
            fullPlanItemsDataObject1.setYear(orderedFullPlanTerms.get(i).getYear() - 1);
            StringBuffer yearRange = new StringBuffer();
            yearRange = yearRange.append(orderedFullPlanTerms.get(i).getYear() - 1).append("-").append(orderedFullPlanTerms.get(i).getYear());
            fullPlanItemsDataObject1.setYearRange(yearRange.toString());
            for (int j = 0; j < 4; j++) {

                fullPlanItemsDataObject1.getTerms().add(fullPlanItemsDataObjectList.get(primaryIndex));
                fullPlanItemsDataObjectList.remove(primaryIndex);

            }
            fullPlanItemsDataObjects.add(fullPlanItemsDataObject1);

        }
    }


    private void addMissingTerms(List<FullPlanTermItemsDataObject> termOrderedFullPlanTerms) {

        boolean autumn = false;
        boolean winter = false;
        boolean spring = false;
        boolean summer = false;
        int size = termOrderedFullPlanTerms.size();
        for (int i = 0; i < size; i++) {
            String Qtr = termOrderedFullPlanTerms.get(i).getTerm().substring(0, 1).toUpperCase().concat(termOrderedFullPlanTerms.get(i).getTerm().substring(1, termOrderedFullPlanTerms.get(i).getTerm().length()));
            terms fd = terms.valueOf(Qtr.trim());
            switch (fd) {
                case Autumn:
                    if (!autumn) {
                        autumn = true;
                    } else {
                        autumn = false;
                    }
                    break;
                case Winter:
                    if (!winter) {
                        winter = true;
                    } else {
                        winter = false;
                    }
                    break;
                case Spring:
                    if (!spring) {
                        spring = true;
                    } else {
                        spring = false;
                    }
                    break;
                case Summer:
                    if (!summer) {
                        summer = true;
                    } else {
                        summer = false;
                    }
                    break;
            }
        }
        if (!autumn) {
            FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
            fullPlanTermItemsDataObject.setTerm(term1);
            termOrderedFullPlanTerms.add(fullPlanTermItemsDataObject);
        }
        if (!winter) {
            FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
            fullPlanTermItemsDataObject.setTerm(term2);
            termOrderedFullPlanTerms.add(fullPlanTermItemsDataObject);
        }
        if (!spring) {
            FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
            fullPlanTermItemsDataObject.setTerm(term3);
            termOrderedFullPlanTerms.add(fullPlanTermItemsDataObject);
        }
        if (!summer) {
            FullPlanTermItemsDataObject fullPlanTermItemsDataObject = new FullPlanTermItemsDataObject();
            fullPlanTermItemsDataObject.setTerm(term4);
            termOrderedFullPlanTerms.add(fullPlanTermItemsDataObject);
        }


    }


    private void yearOrdered(List<FullPlanItemsDataObject> fullPlanItemsDataObjects, List<FullPlanItemsDataObject> orderedFullPlanTerms) {
        int actualYear = -1;
        for (FullPlanItemsDataObject fpl : fullPlanItemsDataObjects) {
            int resultYear = fpl.getYear();

            if (resultYear > actualYear) {
                actualYear = resultYear;
            }

        }
        for (FullPlanItemsDataObject fpl : fullPlanItemsDataObjects) {
            if (String.valueOf(actualYear).equalsIgnoreCase(String.valueOf(fpl.getYear()))) {
                orderedFullPlanTerms.add(fpl);
                fullPlanItemsDataObjects.remove(fpl);
                break;
            }
        }

    }


    private void termOrdered(List<FullPlanTermItemsDataObject> fullPlanItemsDataObjects, List<FullPlanTermItemsDataObject> orderedFullPlanTerms) {
        List<String> term = new ArrayList<String>();
        int count = 0;
        int resultQuarter = 0;
        int maxQ = 0;
        String actualQuarter = null;
        for (FullPlanTermItemsDataObject fpt : fullPlanItemsDataObjects) {
            String Qtr = fpt.getTerm().substring(0, 1).toUpperCase().concat(fpt.getTerm().substring(1, fpt.getTerm().length()));
            term.add(Qtr);
        }
        for (int i = 0; i < term.size(); i++) {
            String tempQuarter = term.get(i);
            terms fd = terms.valueOf(term.get(i));
            switch (fd) {
                case Spring:
                    resultQuarter = 1;
                    break;
                case Summer:
                    resultQuarter = 2;
                    break;
                case Autumn:
                    resultQuarter = 3;
                    break;
                case Winter:
                    resultQuarter = 4;
                    break;
            }
            if (resultQuarter > maxQ) {
                maxQ = resultQuarter;
                actualQuarter = tempQuarter;

            }

        }
        for (int j = 0; j < fullPlanItemsDataObjects.size(); j++) {
            if (fullPlanItemsDataObjects.get(j).getTerm().equalsIgnoreCase(actualQuarter)) {
                orderedFullPlanTerms.add(fullPlanItemsDataObjects.get(j));
                fullPlanItemsDataObjects.remove(fullPlanItemsDataObjects.get(j));
                break;
            }
        }


    }


}
