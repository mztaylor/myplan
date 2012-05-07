package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.DateFormatHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
        int count = 0;
        if (planItemDataObjects.size() > 0) {
            for (PlanItemDataObject pl : planItemDataObjects) {
                String[] str = AtpHelper.getAlphaTermAndYearForAtp(pl.getAtp());
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
        }
        return sb.toString();
    }
}
