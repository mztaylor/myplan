package org.kuali.rice.krad.uif.layout.extension;

import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.element.Action;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.layout.TableLayoutManager;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.r2.common.util.constants.LuiServiceConstants;

/**
 * Layout manager for controlling and optimizing section details presentation.
 */
public class CourseSectionDetailsLayoutManager extends TableLayoutManager {

    private static final long serialVersionUID = 3056313875988089648L;

    private static final Logger LOG = Logger
            .getLogger(CourseSectionDetailsLayoutManager.class);

    @Override
    public void buildLine(View view, Object model,
                          CollectionGroup collectionGroup, List<Field> lineFields,
                          List<FieldGroup> subCollectionFields, String bindingPath,
                          List<Action> actions, String idSuffix, Object currentLine,
                          int lineIndex) {
        super.buildLine(view, model, collectionGroup, lineFields,
                subCollectionFields, bindingPath, actions, idSuffix,
                currentLine, lineIndex);

        // This logic replaces onDocumentReadyScript entries previously in
        // CourseDetailsUI.xml by assigning the correct CSS class to table rows
        // at rendering time, rather than relying on the browser to query for
        // and assign classes on document ready.
        if (lineIndex != -1 && (currentLine instanceof ActivityOfferingItem)) {
            ActivityOfferingItem aoi = (ActivityOfferingItem) currentLine;
            List<String> rowCss = getRowCssClasses();
            rowCss.remove(rowCss.size() - 1); // super adds a row, remove it
            StringBuilder r1css = new StringBuilder();
            //r1css.append("start");
            r1css.append(aoi.isPrimary() ? " courseActivities--primary" : " courseActivities--secondary");
            r1css.append(aoi.getPlanItemId() != null ? " courseActivities--planned" : "");
            // TODO:  MYPLAN-2139 Update after mapping is changed
            //if (!LuiServiceConstants.LUI_AO_STATE_OFFERED_KEY.equals(aoi.getStateKey()))
            boolean offered = "active".equals(aoi.getStateKey());
            if (!offered) {
                r1css.append(" courseActivities--inactive");
            }

            rowCss.add(r1css.toString());

            StringBuilder r2css = new StringBuilder();
            if (!aoi.isPrimary()) {
                r2css.append("collapsible collapsible--secondary");
            } else {
                r2css.append("collapsible collapsible--primary");
            }

            r2css.append(aoi.getPlanItemId() != null ? " courseActivities--planned" : "");

            if (offered) {
                rowCss.add(r2css.toString());
                rowCss.add(r2css.toString());
            }
            if (LOG.isDebugEnabled())
                LOG.debug("AO luiId " + aoi.getCode() + " lineIndex = "
                        + lineIndex + " css(1) " + r1css.toString()
                        + " css(2,3) " + r2css.toString());
        }
    }

    /**
     * This method is written to override the default colSpan calculation done
     * in TableLayoutManager. If this LayoutManager is used, it will rely on the
     * row span and col spans set in the XML and not try to calculate the last
     * column col span on its own
     * <p/>
     * Temporary fix for KULRICE-9215
     *
     * @Author kmuthu Date: 3/28/13
     */
    @Override
    protected int calculateNumberOfRows(List<? extends Field> items) {
        int rowCount = 0;

        // check flag that indicates only one row should be created
        if (isSuppressLineWrapping()) {
            return 1;
        }

        if (items.size() % getNumberOfDataColumns() > 0) {
            rowCount = ((items.size() / getNumberOfDataColumns()) + 1);
        } else {
            rowCount = items.size() / getNumberOfDataColumns();
        }
        return rowCount;
    }
}