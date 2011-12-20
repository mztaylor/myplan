package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFacet {

    protected static final String FACET_KEY_DELIMITER = ";";

    // TODO: This can probably be a HashMap<String, String> now that the number of matches isn't being stored.
    protected List<FacetItem> facetItems;

    protected String unknownFacetKey = "Unknown";

    protected String unknownFacetDisplayName = "Unknown";

    protected boolean showUnknownKey = false;

    public AbstractFacet() {
        this.facetItems = new ArrayList<FacetItem>();
    }

    public List<FacetItem> getFacetItems() {
        //  Put the list in a predictable order.
        Collections.sort(facetItems);

        if(showUnknownKey && facetItems.size() > 0) {
            FacetItem unkownFacet = new FacetItem();
            unkownFacet.setKey(FACET_KEY_DELIMITER + unknownFacetKey + FACET_KEY_DELIMITER);
            unkownFacet.setDisplayName(unknownFacetDisplayName);
            facetItems.add(unkownFacet);
        }

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

    public String getUnknownFacetKey() {
        return unknownFacetKey;
    }

    public void setUnknownFacetKey(String unknownFacetKey) {
        this.unknownFacetKey = unknownFacetKey;
    }

    public String getUnknownFacetDisplayName() {
        return unknownFacetDisplayName;
    }

    public void setUnknownFacetDisplayName(String unknownFacetDisplayName) {
        this.unknownFacetDisplayName = unknownFacetDisplayName;
    }

    public boolean isShowUnknownKey() {
        return showUnknownKey;
    }

    public void setShowUnknownKey(boolean showUnknownKey) {
        this.showUnknownKey = showUnknownKey;
    }

}