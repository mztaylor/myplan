package org.kuali.student.myplan.course.util;


import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class CreditsFacet extends AbstractFacet {

    public CreditsFacet() {
        super();
    }

    /**
     * The credits facet list needs to be in numeric order rather than string order, so
     *
     * @return A list of FacetItems.
     */
    @Override
    public List<FacetItem> getFacetItems() {
        //  Put the list integer order.
        Collections.sort(facetItems, new CreditsComparator());
        return facetItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem item) {
        /*
         * In this case the course/item may match more than one facet item, so
         * first the credits string which was set by the controller must be parsed
         * to determine the key(s). Possible formats for the credit string are ...
         *
         *      Fixed: 2
         *      Multiple: 2, 3, 4
         *      Variable: 2-4
         */
        String credits = item.getCredit();
        boolean isUnknown = false;

        //  If not credits info was set then setup for an "Unknown" facet.
        if (credits == null || credits.equals("")) {
            isUnknown = true;
            credits = UNKNOWN_FACET_KEY;
        }

        /*
         *  Parse the credits string and make a list of keys.
         *
         *  TODO: UW SPECIFIC
         *
         */
        List<String> keys = new ArrayList<String>();
        if (credits.contains("-")) {
            /*
             * Parse the range. The lower and/or the upper values in the range can be float values, so round the
             * lower and upper values down, then create keys for the whole numbers. So, the range 1.5-3.5 would result
             * in this set of keys [1, 2, 3].
             */
            String k[] = credits.split("-");
            Float actualLower = Float.valueOf(k[0]);
            Float actualUpper = Float.valueOf(k[1]);
            int upper = actualUpper.intValue();
            for (int i = actualLower.intValue(); i <= upper; i++) {
                keys.add(String.valueOf(i));
            }
        } else if (credits.contains(",")) {
            //  Remove the spaces.
            credits = credits.replace(" ", "");
            //  Tokenize and add to the list.
            String k[] = credits.split(",");
            for (String key : k) {
                keys.add(trimCredit(key));
            }
        } else {
            //  Assume this was a fixed credit value.
            keys.add(trimCredit(credits));
        }

        for (String key : keys)
        {
            if (isNewFacetKey( FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER))
//            if (isNewFacetKey( key + FACET_KEY_DELIMITER))
            {
                FacetItem fItem = new FacetItem();
                String displayName = null;
                //  Use the key as the display name if it wasn't set to "Unknown" above.
                if (isUnknown) {
                    displayName = UNKNOWN_FACET_DISPLAY_NAME;
                } else {
                    displayName = key;
                }
                fItem.setKey(FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER);
//                fItem.setKey( key + FACET_KEY_DELIMITER);
                fItem.setDisplayName(displayName);
                facetItems.add(fItem);
            }
        }

        //  Convert the list back into a string which can be matched against on the client.
        StringBuilder kb = new StringBuilder();
        for (String k : keys)
        {
//            kb.append(k).append(FACET_KEY_DELIMITER);
            kb.append(FACET_KEY_DELIMITER).append(k).append(FACET_KEY_DELIMITER);
        }
        item.setCreditsFacetKey(kb.toString());
    }

    private String trimCredit(String credit) {
        if (credit.contains(".")) {
            credit = credit.substring(0, credit.indexOf("."));
        }
        return credit;
    }

    /**
     * Custom comparator for credits keys as they need to be sorted by numeric value
     * rather than their string value.
     */
    class CreditsComparator implements Comparator {
        public int compare(Object o1, Object o2){
            FacetItem facetItem1 = (FacetItem) o1;
            FacetItem facetItem2 = (FacetItem) o2;

            String unknownKey = UNKNOWN_FACET_KEY + FACET_KEY_DELIMITER;

            if(unknownKey.equals(facetItem1.getKey())) {
               return 1;
            }

            if(unknownKey.equals(facetItem2.getKey())) {
                return -1;
            }
            //  The trailing ; needs to be stripped off before converting to a Float.
            Float cv1 = Float.valueOf(facetItem1.getKey().replace(FACET_KEY_DELIMITER, ""));
            Float cv2 = Float.valueOf(facetItem2.getKey().replace(FACET_KEY_DELIMITER, ""));

            return cv1.compareTo(cv2);
        }

    }
}
