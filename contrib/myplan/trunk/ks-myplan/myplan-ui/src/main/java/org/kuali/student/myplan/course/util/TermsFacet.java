package org.kuali.student.myplan.course.util;

import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class TermsFacet extends AbstractFacet {

    public TermsFacet() {
        super();
        super.setShowUnknownKey(true);
    }

    /**
     * Put the terms in a predictable order.
     * @return
     */
    @Override
    public List<FacetItem> getFacetItems() {
        //  Call getFacetItems to have the Unknown facet item added.
        Collections.sort(super.getFacetItems(), new TermsFacetItemComparator());
        return facetItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {

        //  The Set of facet keys which pertain to this course.
        Set<String> facetKeys = new HashSet<String>();

        //  Terms
        if (null == course.getTermInfoList() || 0 == course.getTermInfoList().size()) {
            String key = FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER;
            facetKeys.add(key);
        } else {
            for (AtpTypeInfo term : course.getTermInfoList()) {
                //  Title-case the term name.
                String termName =   term.getName().substring(0, 1).toUpperCase() + term.getName().substring(1);
                String key = FACET_KEY_DELIMITER + termName  + FACET_KEY_DELIMITER;

                //  If an FacetItem doesn't exist for this key then create one and add it to the Facet.
                if (isNewFacetKey( key )) {
                    facetItems.add(new FacetItem(key, termName));
                }

                facetKeys.add(key);
            }
        }

        //  Scheduled terms.
        if (null == course.getScheduledTermsList() || 0 == course.getScheduledTermsList().size()) {
            String key = FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER;
            facetKeys.add(key);
        } else {
            for (String t : course.getScheduledTermsList()) {
                String key = FACET_KEY_DELIMITER + t + FACET_KEY_DELIMITER;
                String displayName = t;
                if (isNewFacetKey(key)) {
                    facetItems.add(new FacetItem(key, displayName));
                }
                facetKeys.add(key);
            }
        }

        //  Add the set of keys to the courseSearchItem.
        course.setTermsFacetKeys(facetKeys);
    }

    /**
     * Ordering for Terms Facet items.
     */
    class TermsFacetItemComparator implements Comparator<FacetItem> {

        @Override
        public int compare(FacetItem fi1, FacetItem fi2) {
            //  Unknown is always last.
            if (fi1.getKey().equals(FACET_KEY_DELIMITER + TermsFacet.this.unknownFacetKey + FACET_KEY_DELIMITER)) {
                return 1;
            }

            if (fi2.getKey().equals(FACET_KEY_DELIMITER + TermsFacet.this.unknownFacetKey + FACET_KEY_DELIMITER)) {
                return -1;
            }

            boolean isYear1 = fi1.getKey().matches(".*\\d{4}" + FACET_KEY_DELIMITER + "$");
            boolean isYear2 = fi2.getKey().matches(".*\\d{4}" + FACET_KEY_DELIMITER + "$");
            if (isYear1 && isYear2) {
                //  TODO: For now just ignore the year.
                String termKey1 = fi1.getKey().replaceAll(FACET_KEY_DELIMITER, "").toUpperCase();
                termKey1 = termKey1.replaceAll(" \\d{4}", "");
                String termKey2 = fi2.getKey().replaceAll(FACET_KEY_DELIMITER, "").toUpperCase();
                termKey2 = termKey2.replaceAll(" \\d{4}", "");
                return TermsFacet.TermOrder.valueOf(termKey1).compareTo(TermsFacet.TermOrder.valueOf(termKey2));
            }

            if (isYear1 && ! isYear2) {
                return -1;
            }

            if ( ! isYear1 && isYear2) {
                return 1;
            }

            if ( ! isYear1 &&  ! isYear2) {
                String termKey1 = fi1.getKey().replaceAll(FACET_KEY_DELIMITER, "").toUpperCase();
                String termKey2 = fi2.getKey().replaceAll(FACET_KEY_DELIMITER, "").toUpperCase();
                return TermsFacet.TermOrder.valueOf(termKey1).compareTo(TermsFacet.TermOrder.valueOf(termKey2));
            }
            return 0;
        }
    }

    private enum TermOrder {
          AUTUMN,
          WINTER,
          SPRING,
          SUMMER;
    }
}
