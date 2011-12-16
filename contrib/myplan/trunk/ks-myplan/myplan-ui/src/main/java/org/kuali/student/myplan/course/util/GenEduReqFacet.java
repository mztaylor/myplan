package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class GenEduReqFacet extends AbstractFacet {

    public GenEduReqFacet() {
        super();
        unknownFacetKey =  "None";
        unknownFacetDisplayName = "None";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem item) {

        String genEdString = item.getGenEduReq();

        //  If no gen edu req info was set then setup for an "Unknown" facet.
        if (genEdString == null || genEdString.equals(CourseSearchItem.EMPTY_RESULT_VALUE_KEY) || genEdString.equals("")) {
            item.setGenEduReqFacetKey(FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER);
            return;
        }

        /*
         *  TODO: UW SPECIFIC
         */
        //  Remove white space before tokenizing.
        genEdString = genEdString.replaceAll("\\s+", "");
        String k[] = genEdString.split(",");
        List<String> keys = new ArrayList<String>(Arrays.asList(k));
        for (String key : keys)
        {
            if (isNewFacetKey( FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER))
            {
                FacetItem fItem = new FacetItem();
                String displayName = null;
                displayName = key;
                fItem.setKey(FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER);
                fItem.setDisplayName(displayName);
                facetItems.add(fItem);
            }
        }

        //  Convert the list back into a string which can be matched against on the client.
        StringBuilder kb = new StringBuilder();
        for (String key : keys)
        {
            kb.append(FACET_KEY_DELIMITER).append(key).append(FACET_KEY_DELIMITER);
        }
        item.setGenEduReqFacetKey(kb.toString());
    }
}
