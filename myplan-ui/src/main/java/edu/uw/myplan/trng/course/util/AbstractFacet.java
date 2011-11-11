package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFacet {

    List<FacetItem> facetItems;

    public AbstractFacet() {
        this.facetItems = new ArrayList<FacetItem>();
    }

    public List<FacetItem> getFacetItems() {
        return facetItems;
    }

    /**
     * Submit a CourseSearchItem to add new FacetItems or update counts for existing FacetItems. Also,
     * codes the CourseSearchItem by adding the FacetItem key to the CourseSearchItem.
     *
     * @param course
     * @param item A CourseSearchItem which will be parsed and code.
     */
    public abstract void process(CourseInfo course, CourseSearchItem item);
}