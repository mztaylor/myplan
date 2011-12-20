package org.kuali.student.myplan.course.util;

import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class ScheduledTermsFacet extends AbstractFacet {

    public ScheduledTermsFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {

        StringBuilder facetKeys = new StringBuilder();

        if (null == course.getScheduledTermsList() || 0 == course.getScheduledTermsList().size()) {
            String key = FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER;
            facetKeys.append(key);
            setShowUnknownKey(true);
        } else {
            for (String t : course.getScheduledTermsList()) {
                String key = FACET_KEY_DELIMITER + t + FACET_KEY_DELIMITER;
                String displayName = t;
                facetKeys.append(key);
                if (isNewFacetKey(key)) {
                    FacetItem fItem = new FacetItem();
                    fItem.setKey(key);
                    fItem.setDisplayName(displayName);
                    facetItems.add(fItem);
                }
            }
        }
        //
        course.setScheduledFacetKey(facetKeys.toString());
    }
}
