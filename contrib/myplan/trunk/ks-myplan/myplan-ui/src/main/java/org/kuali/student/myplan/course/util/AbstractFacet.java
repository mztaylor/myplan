package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFacet {

    // TODO: This can probably be a HashMap<String, String> now that the number of matches isn't being stored.
    List<FacetItem> facetItems;

    static final String FACET_KEY_DELIMITER = ";";
    static final String UNKNOWN_FACET_KEY = "Unknown";
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
     * @param item A CourseSearchItem which will be parsed and coded.
     */
    public abstract void process(CourseSearchItem item);

    /**
     * Checks if the facet key is new.
     * @param key
     * @return True if the facet key exists. Otherwise, false.
     */
    protected boolean isNewFacetKey(String key) {
        boolean isNew = true;

        for (FacetItem item : facetItems)
        {
            if (item.getKey().equals(key)) {
                isNew = false;
                break;
            }
        }

        return isNew;
    }
}