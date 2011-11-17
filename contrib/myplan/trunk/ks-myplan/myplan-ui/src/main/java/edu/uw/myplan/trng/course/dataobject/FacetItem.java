package edu.uw.myplan.trng.course.dataobject;

/**
 *  Facet results record.
 */
public class FacetItem implements Comparable<FacetItem> {

    /* The key which will be used for filtering in the main grid of search results. */
    private String key;

    /* The name which will be displayed */
    private String displayName;

    /* Number of CourseSearchItems which match this facet. */
    private int count;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int compareTo(FacetItem other) {
        if (other == null) {
            return 1;
        }
        return this.getDisplayName().compareTo(other.getDisplayName());
    }
}