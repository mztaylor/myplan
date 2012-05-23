package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.DateFormatHelper;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/3/12
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class crudMessagePropertyEditor extends CollectionListPropertyEditor {
    private final static Logger logger = Logger.getLogger(crudMessagePropertyEditor.class);

    @Override
    protected String makeHtmlList(Collection c) {
        List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
        Iterator<Object> iterator = c.iterator();
        StringBuffer sb = new StringBuffer();
        while (iterator.hasNext()) {
            StringBuffer str = new StringBuffer();
            PlanItemDataObject pl = (PlanItemDataObject) iterator.next();
            planItemDataObjects.add(pl);
        }
        /*Dividing the plan items on same date and different date*/
        Map<String,String> planItemsMap=new HashMap<String, String>();
        if(planItemDataObjects.size()>0){

            for(PlanItemDataObject pl:planItemDataObjects){
                String[] str = AtpHelper.atpIdToTermNameAndYear(pl.getAtp());
                String date = DateFormatHelper.getDateFomatted(pl.getDateAdded().toString());
                if(planItemsMap.containsKey(date)){
                    StringBuffer sbuf=new StringBuffer();
                    sbuf=sbuf.append(planItemsMap.get(date)).append(",").append(str[0]).append(" ").append(str[1]);
                    planItemsMap.put(date,sbuf.toString());
                } else {
                    planItemsMap.put(date,str[0]+" "+str[1]);
                }
            }

        }
        int count = 0;
        StringBuffer startsSub=new StringBuffer();
        startsSub=startsSub.append("<dd>").append("Added to ");
        StringBuffer sub=new StringBuffer();
        sub=sub.append("<dd>").append("and ");
        for (String key : planItemsMap.keySet()) {

            if (count == 0) {
                if(planItemsMap.get(key).contains(",")){
                    String[] terms=planItemsMap.get(key).split(",");
                    for(String term:terms){
                        String[] str=term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        sb = startsSub.append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(),str[1].trim())).append("\">").append(term).append(" plan").append("</a> ").append(",");
                    }
                    String formattedString=sb.substring(0,sb.lastIndexOf(",")-1);
                    StringBuffer formattedSubBuf=new StringBuffer();
                    formattedSubBuf=formattedSubBuf.append(formattedString);
                    sb=formattedSubBuf.append(" on ").append(key);
                }else {
                    String[] str=planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String atpId=  AtpHelper.getAtpIdFromTermAndYear(str[0].trim(),str[1].trim());
                    sb = sb.append("<dd>").append("Added to ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                            .append(" on ").append(key);
                }

            }
            if (count > 0) {
                if(planItemsMap.get(key).contains(",")){
                    String[] terms=planItemsMap.get(key).split(",");
                    for(String term:terms){
                        String[] str=term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        sb = sub.append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(),str[1].trim())).append("\">").append(term).append(" plan").append("</a> ").append(",");
                    }
                    String formattedString=sb.substring(0,sb.lastIndexOf(",")-1);
                    StringBuffer formattedSubBuf=new StringBuffer();
                    formattedSubBuf=formattedSubBuf.append(formattedString);
                    sb=formattedSubBuf.append(" on ").append(key);
                }else {
                    String[] str=planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String atpId=  AtpHelper.getAtpIdFromTermAndYear(str[0].trim(),str[1].trim());
                    sb = sb.append("<dd>").append("and ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                            .append(" on ").append(key);
                }

            }
            count++;

         }
        
        
        
        /*

        
        if (planItemDataObjects.size() > 0) {
            for (PlanItemDataObject pl : planItemDataObjects) {
                String[] str = AtpHelper.atpIdToTermNameAndYear(pl.getAtp());
                String date = DateFormatHelper.getDateFomatted(pl.getDateAdded().toString());
                String aptId = pl.getAtp();
                if (count == 0) {
                    sb = sb.append("<dd>").append("Added to ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(aptId).append("\">").append(str[0]).append(" ").append(str[1]).append(" plan").append("</a> ")
                            .append(" on ").append(date);
                }
                if (count > 0) {
                     sb = sb.append("<dd>").append(" and ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(aptId).append("\">").append(str[0]).append(" ").append(str[1]).append(" plan").append("</a> ")
                            .append(" on ").append(date);
                }
                count++;
            }
        }*/
        return sb.toString();
    }
}
