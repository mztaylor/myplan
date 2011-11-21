package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFacet {

    List<FacetItem> facetItems;

    static final String FACET_KEY_DELIMITER = ";";
    static final String UNKNOWN_FACET_KEY = "u";
    static final String UNKNOWN_FACET_DISPLAY_NAME = "Unknown";

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
     * Checks if the facet key is new. If not the count is incremented.
     * @param key
     * @return True if the facet key exists. Otherwise, false.
     */
    protected boolean checkIfNewFacetKey(String key) {
        boolean isNew = true;

        for (FacetItem item : facetItems)
        {
            if (item.getKey().equals(key)) {
                item.setCount(item.getCount() + 1);
                isNew = false;
                break;
            }
        }

        return isNew;
    }
}