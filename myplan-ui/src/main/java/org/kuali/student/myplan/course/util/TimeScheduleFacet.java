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
    public void process(CourseSearchItem course) {
        /*
         * Time schedule contains multiple items so use a delimiter to separate keys.
         */
        String times = course.getScheduledTime();

        if (times == null || times.equals("")) {
            times = UNKNOWN_FACET_DISPLAY_NAME;
        }

        //  Parse the times string and make a list of keys.
        String[] names = times.replace(" ", "").split(",");

        StringBuilder facet = new StringBuilder();
        for (String name : names) {
            String key = null;
            if( UNKNOWN_FACET_DISPLAY_NAME.equals( name )) {
                key = UNKNOWN_FACET_KEY;
            } else {
                key = name;
            }

            key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

            if (isNewFacetKey(key)) {
                FacetItem item = new FacetItem();
                item.setKey(key);
                item.setDisplayName(name);
                facetItems.add(item);
            }
            facet.append(key);
        }

        //  Convert the list back into a string which can be matched against on the client.
        course.setTimeScheduleFacetKey(facet.toString());
    }
}
