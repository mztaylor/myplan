package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class TimeScheduleFacet extends AbstractFacet {

    public TimeScheduleFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem item) {
        /*
         * Time schedule contains multiple items so use a delimiter to separate keys.
         */
        String times = item.getScheduledTime();
        boolean isUnknown = false;

        //  If not credits info was set then setup for an "Unknown" facet.
        if (times == null || times.equals("")) {
            isUnknown = true;
            times = UNKNOWN_FACET_KEY;
        }

        //  Parse the times string and make a list of keys.
        List<String> keys = new ArrayList<String>();

        //  Parse the credits string and make a list of keys.
        if (times.contains(",")) {
            //  Remove the spaces.
            times = times.replace(" ", "");
            //  Tokenize and add to the list.
            keys.addAll(Arrays.asList(times.split(",")));
        } else {
            //  Assume this was a fixed credit value.
            keys.add(times);
        }

        for (String key : keys)
        {
            if (isNewFacetKey(key + FACET_KEY_DELIMITER)) {
                FacetItem fItem = new FacetItem();
                //  Use the key as the display name if it wasn't set to "Unknown" above.
                String displayName = null;
                if (isUnknown) {
                    displayName = UNKNOWN_FACET_DISPLAY_NAME;
                } else {
                    displayName = key;
                }
                fItem.setKey(key + FACET_KEY_DELIMITER);
                fItem.setDisplayName(displayName);
                facetItems.add(fItem);
            }
        }

        //  Convert the list back into a string which can be matched against on the client.
        StringBuilder kb = new StringBuilder();
        for (String k : keys)
        {
            kb.append(k).append(FACET_KEY_DELIMITER);
        }
        item.setTimeScheduleFacetKey(kb.toString());
    }
}
