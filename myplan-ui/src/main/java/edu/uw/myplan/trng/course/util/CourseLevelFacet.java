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
        if (checkIfNewFacetKey(course.getLevel())) {
            FacetItem fItem = new FacetItem();
            fItem.setDisplayName(course.getLevel());
            fItem.setKey(course.getLevel());
            facetItems.add(fItem);
        }
    }
}
