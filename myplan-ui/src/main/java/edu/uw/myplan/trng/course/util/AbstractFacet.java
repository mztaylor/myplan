package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFacet {

    List<FacetItem> facetItems;

    public AbstractFacet() {
        this.facetItems = new ArrayList<FacetItem>();
    }

    public List<FacetItem> getFacetItems() {
        //  Put the list in a predictable order.
        Collections.sort(facetItems);
        return facetItems;
    }

    /**
     * Submit a CourseSearchItem to add new FacetItems or update counts for existing FacetItems. Also,
     * codes the CourseSearchItem by adding the FacetItem key to the CourseSearchItem.
     *
     * @param course
     * @param item A CourseSearchItem which will be parsed and coded.
     */
    public abstract void process(CourseInfo course, CourseSearchItem item);

    /**
     * Checks if the facet key is new or not. If not a new facet key, increments the count for that facet.
     * @param key
     * @return
     */
    protected boolean checkIfNewFacetKey(String key) {
        boolean isNew = true;

        for (FacetItem item : facetItems) {
            if (item.getKey().equals(key)) {
                item.setCount(item.getCount() + 1);
                isNew = false;
                break;
            }
        }

        return isNew;
    }
}