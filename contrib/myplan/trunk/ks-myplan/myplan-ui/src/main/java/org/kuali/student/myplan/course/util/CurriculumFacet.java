package org.kuali.student.myplan.course.util;

import javax.xml.namespace.QName;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CurriculumFacet extends AbstractFacet {

    public CurriculumFacet() {
        super();
    }

    private HashSet<String> curriculumFacetSet = new HashSet<String>();

    @Override
    public List<FacetItem> getFacetItems() {
        String[] list = curriculumFacetSet.toArray( new String[0] );
        Arrays.sort( list );

        for( String display : list ) {
            FacetItem item = new FacetItem();
            String key = FACET_KEY_DELIMITER + display + FACET_KEY_DELIMITER;
            item.setKey( key );
            item.setDisplayName( display );
            facetItems.add( item );
        }

        return facetItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course)
    {
        // TODO: Use the Org Service to lookup curriculum name based on key.
        // For now just use the serviceArea code as the displayName.
        String subject = course.getSubject();

        if (subject == null || subject.equals("")) {
            subject = unknownFacetKey;
        }

        curriculumFacetSet.add( subject );

        String key = FACET_KEY_DELIMITER + subject + FACET_KEY_DELIMITER;

        //  Code the item with the facet key.
        Set<String> keys = new HashSet<String>();
        keys.add(key);
        course.setCurriculumFacetKeys(keys);
    }
}
