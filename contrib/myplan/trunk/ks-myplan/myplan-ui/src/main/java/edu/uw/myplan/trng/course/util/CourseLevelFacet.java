package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

/**
*  Logic for building list of Course Level FacetItems and coding CourseSearchItems.
*/
public class CourseLevelFacet extends AbstractFacet {

    public CourseLevelFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseInfo course, CourseSearchItem item) {
        String key = course.getLevel();

        boolean isUnknown = false;

        if (key == null || key.equals("")) {
            isUnknown = true;
            key = UNKNOWN_FACET_KEY;
        }

        //  If it's a new facet key then create a new FacetItem.
        if (isNewFacetKey(key + FACET_KEY_DELIMITER)) {
            FacetItem fItem = new FacetItem();
            //  The display name and the key are the same in this case.
            fItem.setKey(key + FACET_KEY_DELIMITER);
            String displayName = null;
            if (isUnknown) {
                displayName = UNKNOWN_FACET_DISPLAY_NAME;
            } else {
                displayName = key;
            }
            fItem.setDisplayName(displayName);
            facetItems.add(fItem);
        }
        //  Code the item with the facet key.
        item.setCourseLevelFacetKey(key + FACET_KEY_DELIMITER);
    }
}
