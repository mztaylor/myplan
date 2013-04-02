package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 3/2/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedSectionsPropertyEditor extends PropertyEditorSupport {

    private final static int MAX_SECTIONS = 4;

    private boolean moveDialog = false;

    private boolean copyDialog = false;

    private boolean deleteDialog = false;

    public boolean isMoveDialog() {
        return moveDialog;
    }

    public void setMoveDialog(boolean moveDialog) {
        this.moveDialog = moveDialog;
    }

    public boolean isCopyDialog() {
        return copyDialog;
    }

    public void setCopyDialog(boolean copyDialog) {
        this.copyDialog = copyDialog;
    }

    public boolean isDeleteDialog() {
        return deleteDialog;
    }

    public void setDeleteDialog(boolean deleteDialog) {
        this.deleteDialog = deleteDialog;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    /**
     * returns the delete Plan with sections alert text
     * if there are more than 4 sections planned then returns "All sections will be deleted as well."
     * otherwise if there are more than one section planned and less than 4 sections
     * returns "Section A, AC, B and C will be deleted as well." text.
     *
     * @return
     */
    @Override
    public String getAsText() {
        PlanForm planForm = (PlanForm) super.getValue();
        List<ActivityOfferingItem> plannedSections = planForm.getPlanActivities();
        String atpId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getParameter("atpId");
        StringBuffer sb = new StringBuffer();
        List<String> sections = new ArrayList<String>();
        for (ActivityOfferingItem activity : plannedSections) {
            if (activity.getPlanItemId() != null && activity.getAtpId().equalsIgnoreCase(atpId)) {
                sections.add(activity.getCode());
            }

        }
        int counter = 0;
        if (sections.size() >= MAX_SECTIONS) {
            if (isDeleteDialog()) {
                sb.append("All sections ");
            }else{
                sb.append("All your planned sections");
            }
        } else {
            for (String section : sections) {
                if (counter == 0) {
                    if (isDeleteDialog()) {
                        sb.append(String.format("Section %s", section));
                    } else {
                        sb.append(String.format("Your planned section %s", section));
                    }
                } else if (counter == sections.size() - 1) {
                    sb.append(String.format(" and %s", section));
                } else {
                    sb.append(String.format(", %s", section));
                }
                counter++;
            }
        }
        if (sb.length() > 0) {
            if (isDeleteDialog()) {
                sb.append(" will be deleted as well.");
            } else if (isMoveDialog()) {
                sb.append(" will not be moved with the course.");
            } else if (isCopyDialog()) {
                sb.append(" will not be copied with the course.");
            }
        }
        return sb.toString();
    }
}
