package edu.uw.myplan.trng.course.util;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class CreditsFacet extends AbstractFacet {

    private static final String keyDelimiter = ";";

    public CreditsFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseInfo course, CourseSearchItem item) {
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
        String displayName = null;

        //  If not credits info was set then setup for an "Unknown" facet.
        if (credits == null || credits.equals("")) {
            credits = "u";
            displayName = "Unknown";
        }

        //  Parse the credits string and make a list of keys.
        List<String> keys = new ArrayList<String>();
        if (credits.contains("-")) {
            //  Parse the range and create keys at one credit increments.
            String k[] = credits.split("-");
            int lower = Integer.valueOf(k[0]);
            int upper = Integer.valueOf(k[1]);
            for (int i = lower; i <= upper; i++) {
                keys.add(String.valueOf(i));
            }
        } else if (credits.contains(",")) {
            //  Remove the spaces.
            credits = credits.replace(" ", "");
            //  Tokenize and add to the list.
            keys.addAll(Arrays.asList(credits.split(",")));
        } else {
            //  Assume this was a fixed credit value.
            keys.add(credits);
        }

        for (String key : keys) {
            if (checkIfNewFacetKey(key + keyDelimiter)) {
                FacetItem fItem = new FacetItem();
                //  Use the key as the display name if it wasn't set to "Unknown" above.
                if (displayName == null) {
                    displayName = key;
                }
                fItem.setKey(key + keyDelimiter);
                fItem.setDisplayName(displayName);
                fItem.setCount(1);
                facetItems.add(fItem);
            }
        }

        //  Convert the list back into a string which can be matched against on the client.
        StringBuilder kb = new StringBuilder();
        for (String k : keys) {
            kb.append(k).append(keyDelimiter);
        }
        item.setCreditsFacetKey(kb.toString());
    }
}
