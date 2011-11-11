package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CurriculumFacet extends AbstractFacet {

    public CurriculumFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseInfo course, CourseSearchItem courseSearchItem) {
        String subjectArea = course.getSubjectArea();

        boolean isNew = true;

        for (FacetItem i : facetItems) {
            if (i.getKey().equals(i)) {
                i.setCount(i.getCount() + 1);
                isNew = false;
                break;
            }
        }

        String key = course.getSubjectArea();

        if (isNew) {
            String displayName = key;
            // TODO: Use the Org Service to lookup curriculum name based on key.
            facetItems.add(new FacetItem(key, displayName));
        }
        courseSearchItem.setCurriculumFacetKey(key);
    }
}
