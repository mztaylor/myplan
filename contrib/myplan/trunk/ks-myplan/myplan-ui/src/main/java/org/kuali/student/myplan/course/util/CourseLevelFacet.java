package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

/**
*  Logic for building list of Course Level FacetItems and coding CourseSearchItems.
*/
public class CourseLevelFacet extends AbstractFacet {

    public CourseLevelFacet() {
        super();
        super.setShowUnknownKey(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {
        String displayName = course.getLevel();
        String key = null;

        if (displayName == null || displayName.equals("")) {
            displayName = unknownFacetDisplayName;
            key = unknownFacetKey;
        } else {
            key = displayName;
        }

        key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

        //  If it's a new facet key then create a new FacetItem.
        if (isNewFacetKey( key )) {
            FacetItem fItem = new FacetItem();
            //  The display name and the key are the same in this case.
            fItem.setKey(key);
            fItem.setDisplayName(displayName);
            facetItems.add(fItem);
        }
        //  Code the item with the facet key.
        course.setCourseLevelFacetKey(key);
    }
}
