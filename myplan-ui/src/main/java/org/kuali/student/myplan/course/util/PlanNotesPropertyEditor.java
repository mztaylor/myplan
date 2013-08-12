package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.plan.dataobject.FullPlanItemsDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/1/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlanNotesPropertyEditor extends PropertyEditorSupport {

    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.UL;

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        FullPlanItemsDataObject fullPlanItemsDataObject = (FullPlanItemsDataObject) super.getValue();
        Map<String, List<String>> termNotes = new HashMap<String, List<String>>();
        for (PlannedTerm plannedTerm : fullPlanItemsDataObject.getTerms()) {
            for (PlannedCourseDataObject plannedCourseDataObject : plannedTerm.getPlannedList()) {
                if (plannedCourseDataObject.getNote() != null) {
                    String code = plannedCourseDataObject.getPlaceHolderCode() != null ? plannedCourseDataObject.getPlaceHolderCode() : plannedCourseDataObject.getCourseDetails().getCode();
                    String credit = plannedCourseDataObject.getPlaceHolderCredit() != null ? plannedCourseDataObject.getPlaceHolderCredit() : plannedCourseDataObject.getCourseDetails().getCredit();
                    if (termNotes.get(plannedTerm.getQtrYear()) != null) {
                        termNotes.get(plannedTerm.getQtrYear()).add(String.format("<%s><label>%s</label> (%s): %s</%s>", listType.getListItemElementName(), code, credit, plannedCourseDataObject.getNote(), listType.getListItemElementName()));
                    } else {
                        List<String> displayValues = new ArrayList<String>();
                        displayValues.add(String.format("<%s><label>%s</label> (%s): %s</%s>", listType.getListItemElementName(), code, credit, plannedCourseDataObject.getNote(), listType.getListItemElementName()));
                        termNotes.put(plannedTerm.getQtrYear(), displayValues);
                    }

                }
            }
        }

        StringBuffer sb = new StringBuffer();
        for (String key : termNotes.keySet()) {
            sb = sb.append(String.format("<%s><h5>%s</h5>", listType.getListElementName(), key));
            for (String element : termNotes.get(key)) {
                sb = sb.append(element);
            }
            sb = sb.append(String.format("</%s>", listType.getListElementName()));
        }

        return sb.toString();
    }
}
