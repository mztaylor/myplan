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

        //  Make sure credit info exists before processing it.
        if (credits == null || credits.equals("")) {
            credits = "Unknown";
        }

        List<String> keys = new ArrayList<String>();
        //  Parse the credits string and make a list of keys.
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
            if (checkIfNewFacetKey(key)) {
                FacetItem fItem = new FacetItem();
                //  The display name and the key are the same in this case.
                fItem.setKey(key);
                fItem.setDisplayName(key);
                fItem.setCount(1);
                facetItems.add(fItem);
            }
        }
        //  Convert the list back into a string which can be matched against on the client.
        StringBuilder kb = new StringBuilder();
        for (String k : keys) {
            kb.append(k).append(";");
        }
        item.setCreditsFacetKey(kb.toString());
    }
}
