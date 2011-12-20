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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {

        StringBuilder facet = new StringBuilder();

        if (null == course.getTermInfoList() || 0 == course.getTermInfoList().size()) {
            String key = FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER;
            facet.append(key);
            setShowUnknownKey(true);
        } else {
            for (AtpTypeInfo term : course.getTermInfoList()) {
                String termStr =   term.getName().substring(0, 1).toUpperCase() + term.getName().substring(1);
                String key = FACET_KEY_DELIMITER + termStr  + FACET_KEY_DELIMITER;
                facet.append(key);

                if (isNewFacetKey( key )) {
                    FacetItem fItem = new FacetItem();
                    String displayName = null;
                    displayName = key;
                    fItem.setKey(key);
                    fItem.setDisplayName(termStr);
                    facetItems.add(fItem);
                }
            }
        }

        course.setTermsFacetKey(facet.toString());
    }
}
