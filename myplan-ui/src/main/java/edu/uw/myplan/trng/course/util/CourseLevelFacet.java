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
        //  If it's a new facet key then create a new FacetItem.
        if (checkIfNewFacetKey(key)) {
            FacetItem fItem = new FacetItem();
            //  The display name and the key are the same in this case.
            fItem.setKey(key);
            fItem.setDisplayName(key);
            fItem.setCount(fItem.getCount() + 1);
            facetItems.add(fItem);
        }
        //  Code the item with the facet key.
        item.setCourseLevelFacetKey(key);
    }
}
